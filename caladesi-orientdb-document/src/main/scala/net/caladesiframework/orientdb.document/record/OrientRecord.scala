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

package net.caladesiframework.orientdb.document.record

import net.caladesiframework.record.Record

abstract class OrientRecord[RecordType](implicit mf: Manifest[RecordType]) extends Record[RecordType] {
  self: RecordType =>

  def meta: OrientMetaRecord[RecordType]

  def create = meta.createRecord

  def delete = false

  def save(record: RecordType) = false

  def find(id: String) = None
}
