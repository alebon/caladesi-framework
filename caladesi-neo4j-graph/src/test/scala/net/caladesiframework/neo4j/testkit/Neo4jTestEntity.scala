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

package net.caladesiframework.neo4j.testkit

import net.caladesiframework.neo4j.graph.entity.{UUIDPk, Neo4jGraphEntity}
import net.caladesiframework.neo4j.field.StringField
import net.caladesiframework.neo4j.index.FulltextIndexed

class Neo4jTestEntity(init:Boolean = true) extends Neo4jGraphEntity with UUIDPk {

  object title extends StringField(this) with FulltextIndexed

  def this() = {
    this(true)

    uuid.name
    title.name
  }

}

object Neo4jTestEntity extends Neo4jTestEntity
