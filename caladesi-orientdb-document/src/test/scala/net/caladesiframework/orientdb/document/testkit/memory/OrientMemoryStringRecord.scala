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

package net.caladesiframework.orientdb.document.testkit.memory

import net.caladesiframework.orientdb.document.record.{OrientMetaRecord, OrientRecord}
import net.caladesiframework.orientdb.document.field.{OptionalStringField, StringField}

class OrientMemoryStringRecord extends OrientRecord[OrientMemoryStringRecord] {

  def meta = OrientMemoryStringRecord

  object stringField extends StringField(this)

  object stringFieldWithCustomDefault extends StringField(this, "customDefault")

  object optionalStringField extends OptionalStringField(this)

}

object OrientMemoryStringRecord extends OrientMemoryStringRecord with OrientMetaRecord[OrientMemoryStringRecord] {
  override def databaseName = "memoryDB"
}
