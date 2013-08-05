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

import net.caladesiframework.record.Record
import net.caladesiframework.elastic.{DefaultElasticProviderIdentifier, ElasticProviderIdentifier}
import net.caladesiframework.elastic.field.{DynamicPropertiesField, StringField, UuidField}
import net.caladesiframework.document.Field
import org.elasticsearch.common.xcontent.{XContentFactory, XContentBuilder}
import net.caladesiframework.elastic.field.analyzer.NotAnalyzed

trait ElasticRecord[RecordType] extends Record[RecordType] {
  self: RecordType =>

  object _uuid extends UuidField(this) with NotAnalyzed

  def indexName = "%s_index".format(self.getClass.getSimpleName).toLowerCase.replace("$_", "").replace("_", "")
  def itemTypeName = "%s_item".format(self.getClass.getSimpleName).toLowerCase.replace("$_", "").replace("_", "")

  def meta: ElasticMetaRecord[RecordType]

  def providerIdentifier: ElasticProviderIdentifier = DefaultElasticProviderIdentifier

  def save = {
    // Initial class creation
    if (meta.isIndexCreationNeeded) {
      meta.initIndex
    }

    meta.provider.addItem(indexName, itemTypeName, _uuid.get.toString, toDbValues(), toMappingUpdate() )
    true
  }

  /**
   * Deletes the item its called on
   * @return
   */
  def delete = {
    meta.provider.removeItem(indexName, itemTypeName, _uuid.get.toString)
    true
  }

  /**
   * Transform field value into JsonFormat
   * @return
   */
  protected def toDbValues(): XContentBuilder = {

    import org.elasticsearch.common.xcontent.XContentFactory._

    val builder = jsonBuilder().startObject()

    meta.fields foreach {
      metaField => {
        val fieldName = metaField._1

        val m = this.getClass.getMethod(metaField._1)
        val fieldObj: Field[_, RecordType] = m.invoke(this).asInstanceOf[Field[_, RecordType]]

        fieldObj match {

          case f: UuidField[RecordType] =>
            builder.field(fieldName, fieldObj.asInstanceOf[UuidField[RecordType]].get.toString)

          case f: StringField[RecordType] =>
            builder.field(fieldName, fieldObj.asInstanceOf[StringField[RecordType]].get.toString)

          case f:DynamicPropertiesField[RecordType] =>
            fieldObj.asInstanceOf[DynamicPropertiesField[RecordType]].get.foreach(entry => {
              builder.field(fieldName + entry._1, entry._2.toString)
            })

          case _ => throw new RuntimeException("Unhandled field!")
        }
      }
    }

    builder.endObject()
  }

  /**
   * Transform field definitions into mapping update JsonFormat
   * @return
   */
  protected def toMappingUpdate(): Option[XContentBuilder] = {

    val builder = XContentFactory.jsonBuilder().startObject()
      .startObject(this.itemTypeName).startObject("properties")

    var hasDynamicProperties = false

    meta.fields foreach {
      metaField => {
        val fieldName = metaField._1

        val m = this.getClass.getMethod(metaField._1)
        val fieldObj: Field[_, RecordType] = m.invoke(this).asInstanceOf[Field[_, RecordType]]

        fieldObj match {

          // We create only mapping updates for dynamic properties
          case f:DynamicPropertiesField[RecordType] =>
            fieldObj.asInstanceOf[DynamicPropertiesField[RecordType]].get.foreach(entry => {
              builder.startObject(fieldName + entry._1).field("type", "string").field("index", "not_analyzed").endObject()
            })
            hasDynamicProperties = true

          case _ => // Ignore this field
        }
      }
    }

    builder.endObject()

    if (hasDynamicProperties) {
      //println("BUILDING DYNAMIC PROPERTIES INDEX MAPPING: " + builder.string())
      return Some(builder)
    } else {
      return None
    }
  }

  def internalId = Some(_uuid.get.toString)

  def find(id: String) = None

  def create = meta.createRecord

}
