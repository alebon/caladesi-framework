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

package net.caladesiframework.orientdb.graph.testkit

import net.caladesiframework.orientdb.graph.entity.{UUIDPk, OrientGraphEntity}
import net.caladesiframework.orientdb.relation.RelatedToOne
import net.caladesiframework.orientdb.field.OptionalStringField
import net.caladesiframework.orientdb.index.{UniqueIndexed, FulltextIndexed}

class TestEntityWithRelations(init: Boolean = true) extends OrientGraphEntity with UUIDPk {

  object testEntity extends RelatedToOne[TestEntity](this, "HAS")

  def this() = {
    this(true)

    uuid.name
    testEntity.name
  }
}

object TestEntityWithRelations extends TestEntityWithRelations {}
