/*
 * Copyright 2013 Caladesi Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.caladesiframework.elastic.record

import net.caladesiframework.elastic.provider.{Elastic, ElasticProvider}
import net.caladesiframework.document.Field
import net.caladesiframework.elastic.field.{DynamicPropertiesField, StringField, UuidField}
import java.util.UUID
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.search.SearchResponse
import scala.collection.immutable.HashMap
import scala.collection.mutable
import org.elasticsearch.search.facet.Facets

trait ElasticMetaRecord[RecordType] extends ElasticRecord[RecordType] {
  self: RecordType =>

  lazy val provider: ElasticProvider = {
    Elastic.getProvider(this.providerIdentifier) match {
      case Some(provider) => provider
      case None => throw new RuntimeException("No provider defined for %s".format(this.providerIdentifier.jndiName))
    }
  }

  def createRecord: RecordType = {
    val record = this.getClass.getSuperclass.newInstance().asInstanceOf[RecordType]
    provider.ensureIndex(this.indexName, this.itemTypeName)

    // Avoid iterating all fields
    //if (!initComplete) {
    initFields(record)
    //  initComplete = true
    //}

    record
  }

  private [this] val fieldMap: scala.collection.mutable.Map[String, Field[_, RecordType]]
    = new scala.collection.mutable.HashMap[String, Field[_, RecordType]]()

  def fields = this.fieldMap.toSeq

  private var initComplete: Boolean = false

  private def initFields(record: Any) = {
    record.getClass.getDeclaredFields foreach {
      field => {
        field.getType.getMethods foreach {

          // If field contains a method for initialization, call it and add field to meta fields
          method => {
            if  (method.getName.equals("initField")) {
              field.setAccessible(true)
              val m = record.getClass.getMethod(field.getName.replace("$module", ""))

              val fieldObj: Field[_, RecordType] = m.invoke(record).asInstanceOf[Field[_, RecordType]]

              // Apply default naming
              if (fieldObj.name == null) {
                fieldObj.applyName(field.getName.replace("$module", ""))
              }

              fieldObj.initField
              attach(field.getName.replace("$module", ""), fieldObj)
            }
          }
        }

      }
    }
  }

  /**
   * Adds the field to the meta fields
   *
   * @param field
   * @return
   */
  private def attach(key: String, field: Field[_, RecordType]) = {
    if (this.fieldMap.get(key).isEmpty) {
      this.fieldMap.put(key, field)
    }
  }

  /**
   * Finds a record by id
   *
   * @param id
   * @return
   */
  def findById(id: String): Option[RecordType] = {
    val response = provider.getItemById(indexName, itemTypeName, id)

    if(response.isSourceEmpty) {
      return None
    }

    val record = getRecord(response.getSourceAsMap.asInstanceOf[java.util.HashMap[String, AnyRef]])
    Some(record)
  }

  /**
   * Returns al count for this record type
   *
   * @return
   */
  def count: Long = provider.countAll(indexName, itemTypeName)

  /**
   * Clears all items
   *
   * @return
   */
  def deleteAll = {
    provider.deleteIndex(indexName)
  }

  /**
   * Search a specific field
   *
   * @param field
   * @param queryTerm
   * @return
   */
  def query(field: Field[_, _], queryTerm : String): List[RecordType] = {

    val response = provider.executeFuzzyQuery(field.name, queryTerm, this.indexName, this.itemTypeName)
    val result: List[RecordType] = response.getHits.getHits.map(hit => {
      getRecord(hit.getSource.asInstanceOf[java.util.HashMap[String, AnyRef]])
    }).toList


    result
  }

  /**
   * Performs a query based on given field filters
   * (Field1 -> value1, Field2 -> value2)
   *
   * @param filterMap map
   * @return
   */
  def queryFiltered(filterMap: HashMap[Field[_, _], String]): List[RecordType] = {
    val response = provider.executeFilterQuery(this.indexName, this.itemTypeName, filterMap.map(entry => (entry._1.name, entry._2)))
    val result: List[RecordType] = response.getHits.getHits.map(hit => {
      getRecord(hit.getSource.asInstanceOf[java.util.HashMap[String, AnyRef]])
    }).toList

    result
  }

  /**
   * Performs a query based on given field filters and returns the requested facets for the query
   * Filtered fields: (Field1 -> value1, Field2 -> value2)
   * Facets: (Field2, Field3)
   *
   * @param filterMap map
   * @param facets list
   * @return
   */
  def queryFilteredWithFacets(filterMap: HashMap[Field[_, _], String], facets: List[Field[_,_]]): (List[RecordType], Facets) = {
    val response = provider.executeFacetFilterQuery(this.indexName,
      this.itemTypeName, filterMap.map(entry => (entry._1.name, entry._2)),
      facets.map(facetField => facetField.name))

    val result: List[RecordType] = response.getHits.getHits.map(hit => {
      getRecord(hit.getSource.asInstanceOf[java.util.HashMap[String, AnyRef]])
    }).toList

    (result, response.getFacets)
  }

  /**
   * Returns TemsFacets for given field (More or less, wrapper)
   *
   * @param field Field
   * @return
   */
  def getFacetsForField(field: Field[_, _]): TermsFacet = {
    provider.executeFacetForFieldQuery(field.name, this.indexName, this.itemTypeName)
  }

  /**
   * Returns record by given fields
   *
   * @param fields
   * @return
   */
  protected def getRecord(fields: java.util.HashMap[String, AnyRef]): RecordType = {
    val record = create

    meta.fields.map(
      metaField => {
        val fieldName = metaField._1

        val m = this.getClass.getMethod(metaField._1)
        val fieldObj: Field[_, RecordType] = m.invoke(record).asInstanceOf[Field[_, RecordType]]
        val indexedValue = fields.get(fieldName).asInstanceOf[String]

        fieldObj match {

          case f: UuidField[RecordType] =>
            fieldObj.asInstanceOf[UuidField[RecordType]].set(UUID.fromString(indexedValue))

          case f: StringField[RecordType] =>
            fieldObj.asInstanceOf[StringField[RecordType]].set(indexedValue)

          case f: DynamicPropertiesField[RecordType] =>
            val values = new mutable.HashMap[String, AnyRef]()
            fields.keySet().toArray().foreach(key => {
              if (key.asInstanceOf[String].startsWith(f.name)) {
                values.put(key.asInstanceOf[String].replace(f.name, ""), fields.get(key.asInstanceOf[String]))
              }
            })
            fieldObj.asInstanceOf[DynamicPropertiesField[RecordType]].set(values.toMap)

          case _ => throw new RuntimeException("Unhandled field!")
        }

      })

    record
  }
}
