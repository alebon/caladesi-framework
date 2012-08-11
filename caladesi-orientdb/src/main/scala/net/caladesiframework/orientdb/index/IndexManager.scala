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

trait IndexManager extends OrientGraphDbWrapper {

  def checkFieldIndex(field: Field[AnyRef] with FulltextIndexed)(implicit db: OGraphDatabase) = {

    val documents : List[ODocument] =  db.queryBySql[ODocument]("select flatten(indexes) from cluster:0")

    val indexName = field.owner.clazz + "_" + field.name

    var matches = false
    documents foreach {
      document => { if (document.field("name").equals(indexName)) { matches = true }
      }
    }

    if (!matches) {
      db.getMetadata.getIndexManager.createIndex(indexName,
        OClass.INDEX_TYPE.FULLTEXT.toString,
        new OSimpleKeyIndexDefinition(OType.STRING), null, null)
    }
  }

}
