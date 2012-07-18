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

import net.caladesiframework.orientdb.graph.entity.{OrientGraphEntity}
import net.caladesiframework.orientdb.field._

class TestEntity(init: Boolean = true) extends OrientGraphEntity {

  object stringField extends StringField(this)
  object intField extends IntField(this)
  object doubleField extends DoubleField(this)

  object optionalStringField extends OptionalStringField(this)
  object optionalUuidField extends OptionalUuidField(this)

  def this() = {
    this(true)

    stringField.name
    intField.name
    doubleField.name

    optionalStringField.name
    optionalUuidField.name
  }
}
