/*
 * Copyright 2012 Caladesi Framework
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

package net.caladesiframework.neo4j.index

import net.caladesiframework.neo4j.field.Field
import net.caladesiframework.neo4j.db.Neo4jDatabaseService
import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import java.util.{HashMap => jMap}
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.IndexHits
import org.neo4j.index.lucene.QueryContext
import org.apache.lucene.queryParser.QueryParser.Operator
import org.apache.lucene.queryParser.QueryParser

trait IndexManager {

  /**
   * Check for index and create one if its missing
   *
   * @param field
   * @param ds
   * @return
   */
  def checkFieldIndex(field: Field[_] with IndexedField)(implicit ds: Neo4jDatabaseService) = {
    val indexName = NamingStrategy.indexName(field)

    if (!ds.graphDatabase.index().existsForNodes(indexName)) {

      val idx = ds.graphDatabase.index()

      // Create the index
      field match {
        case field: Field[_] with UniqueIndexed =>
          idx.forNodes(indexName)
        case field: Field[_] with FulltextIndexed =>
          val idxParams = new jMap[String, String]()
          idxParams.put("type", "fulltext")

          idx.forNodes(indexName, idxParams)
      }

    }
  }

  /**
   * Update index entries for the entity
   *
   * @param entity
   * @param ds
   * @return
   */
  def updateIndex(entity: Neo4jGraphEntity, reIndex: Boolean = true)(implicit ds: Neo4jDatabaseService)  = {
    val idx = ds.graphDatabase.index()

    entity.fields.asInstanceOf[List[Field[_]]] foreach {

      // Create the index
      field =>

        field match {
        case field: Field[_] with IndexedField =>
          val indexName = NamingStrategy.indexName(field)
          val nodeIdx = idx.forNodes(indexName)
          if (null != nodeIdx) {
            nodeIdx.remove(entity.getUnderlyingNode, field.name)
            nodeIdx.add(entity.getUnderlyingNode, field.name, field.is)
          }
        case _ =>
          // Ignore field
      }
    }
  }

  /**
   * Searched in index for node
   *
   * @param field
   * @param value
   * @param ds
   * @return
   */
  def findSingleByIndex(field: Field[_] with IndexedField, value: Any)(implicit ds: Neo4jDatabaseService): Option[Node] = {
    val indexName = NamingStrategy.indexName(field)
    val idxForNode = ds.graphDatabase.index().forNodes(indexName)

    idxForNode.query(field.name, QueryParser.escape(value.asInstanceOf[String])) match {
      case hits: IndexHits[Node] if (hits.size() > 0) =>
        Some(hits.next())
      case _ =>
        None
    }
  }

  /**
   * Searched in index for all nodes
   *
   * @param field
   * @param value
   * @param ds
   * @return
   */
  def findAllByIndex(field: Field[_] with IndexedField, value: Any)(implicit ds: Neo4jDatabaseService): List[Node] = {
    val indexName = NamingStrategy.indexName(field)
    val idxForNode = ds.graphDatabase.index().forNodes(indexName)
    var result: List[Node] = List()

    val hits = idxForNode.query(field.name, QueryParser.escape(value.asInstanceOf[String])).iterator()
    while(hits.hasNext) {
      val node: Node = hits.next()
      result = node :: result
    }

    result
  }

  def findAllByIndexSet(field: Field[_] with IndexedField, values: List[String])(implicit ds: Neo4jDatabaseService): List[Node] = {
    val indexName = NamingStrategy.indexName(field)
    val idxForNode = ds.graphDatabase.index().forNodes(indexName)
    var result: List[Node] = List()

    val query: QueryContext = new QueryContext( values.map(value => field.name + ":\"" + QueryParser.escape(value) + "\"" ).mkString(" "))
      .defaultOperator( Operator.OR )


    val hits = idxForNode.query(query).iterator()
    while(hits.hasNext) {
      val node: Node = hits.next()
      result = node :: result
    }

    result
  }


  /**
   * Drops field index for all fields
   *
   * @param entity
   * @param ds
   * @return
   */
  def dropIndex(entity: Neo4jGraphEntity)(implicit ds: Neo4jDatabaseService) = {

  }

  /**
   * Removes all entries for the entity
   *
   * @param entity
   * @param ds
   * @return
   */
  def removeFromIndex(entity: Neo4jGraphEntity)(implicit ds: Neo4jDatabaseService) = {
    entity.fields foreach {
      field => {
        field match {
          case field: Field[_] with IndexedField =>
            val indexName = NamingStrategy.indexName(field)
            val idxForNode = ds.graphDatabase.index().forNodes(indexName)
            idxForNode.remove(entity.getUnderlyingNode)
          case _ =>
            // ignore it
        }
      }
    }

  }

}

object NamingStrategy {

  def indexName(field: Field[_] with IndexedField): String = {
    field match {
      case field: Field[_] with FulltextIndexed =>
        "idx:fulltext_%s".format(field.owner.clazz)
      case field: Field[_] with UniqueIndexed =>
        "idx:exact_%s".format(field.owner.clazz)
    }

  }

}
