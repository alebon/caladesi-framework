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

package net.caladesiframework.orientdb.index

import net.caladesiframework.orientdb.field.Field
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import net.caladesiframework.orientdb.graph.OrientGraphDbWrapper
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.metadata.schema.{OType, OClass}
import com.orientechnologies.orient.core.index.OSimpleKeyIndexDefinition
import net.caladesiframework.orientdb.graph.entity.OrientGraphEntity
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.db.record.OIdentifiable

trait IndexManager extends OrientGraphDbWrapper {

  /**
   * Check for index and create one if its missing
   *
   * @param field
   * @param db
   * @return
   */
  def checkFieldIndex(field: Field[_] with IndexedField)(implicit db: OGraphDatabase) = {

    val documents : List[ODocument] =  db.queryBySql[ODocument]("select flatten(indexes) from cluster:0")

    val indexName = NamingStrategy.indexName(field)

    var matches = false
    documents foreach {
      document => { if (document.field("name").equals(indexName)) { matches = true }
      }
    }

    if (!matches) {
      // Select index type
      val indexType = field match {
        case index: UniqueIndexed => OClass.INDEX_TYPE.UNIQUE.toString
        case index: FulltextIndexed => OClass.INDEX_TYPE.FULLTEXT.toString
        case _ => throw new Exception("Not supported index type")
      }

      // Create index
      db.getMetadata.getIndexManager.createIndex(indexName,
        indexType,
        new OSimpleKeyIndexDefinition(OType.STRING), null, null)
    }
  }

  /**
   * Update index entries for the entity
   *
   * @param entity
   * @param db
   * @return
   */
  def updateIndex(entity: OrientGraphEntity, reIndex: Boolean = true)(implicit db: OGraphDatabase)  = {

    val vertex: OIdentifiable = entity.getUnderlyingVertex

    entity.fields foreach {
      field => {
        val index = db.getMetadata.getIndexManager.getIndex(NamingStrategy.indexName(field.asInstanceOf[Field[AnyRef]]))

        field match {
          case field: FulltextIndexed =>
            index.remove(vertex)
            if (reIndex) {
              index.put(field.asInstanceOf[Field[AnyRef]].value, vertex)
            }
          case _ =>
            // Ignore field
        }

        //index.lazySave()
      }
    }
  }

  /**
   * Drops field index for all fields
   *
   * @param entity
   * @param db
   * @return
   */
  def dropIndex(entity: OrientGraphEntity)(implicit db: OGraphDatabase) = {
    entity.fields foreach {
      field => {
        field match {
          case field: FulltextIndexed =>
            db.getMetadata.getIndexManager.dropIndex(NamingStrategy.indexName(field.asInstanceOf[Field[AnyRef]]))
          case _ =>
            // Ignore field
        }
      }
    }
  }

}

object NamingStrategy {

  def indexName(field: Field[_]): String = {
    field.owner.clazz + "_" + field.name
  }

}
