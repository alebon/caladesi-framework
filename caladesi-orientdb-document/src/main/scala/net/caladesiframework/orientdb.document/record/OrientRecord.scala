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
import com.orientechnologies.orient.core.record.impl.ODocument

trait OrientRecord[RecordType] extends Record[RecordType] {
  self: RecordType =>

  def meta: OrientMetaRecord[RecordType]

  def create = meta.createRecord

  def delete = {
    meta.transactional(implicit db => {
      db.delete(this.dbRecord.get)
      true
    })
  }

  private [this] var dbRecord: Option[ODocument] = None

  override def internalId = {
    if (!dbRecord.isEmpty) {
      Some(dbRecord.get.getIdentity.toString)
    } else {
      None
    }
  }

  def save = {

    // Initial class creation
    val clazz = meta.connected(implicit db => {
      db.getMetadata.getSchema.getOrCreateClass(meta.collectionName)
    })



    meta.transactional(implicit db => {
      if (this.internalId().isEmpty) {
        // Record is detached, Create new record in DB
        val doc = new ODocument(clazz)

        // Force create
        doc.save()

        // Bind record to dbRecord
        dbRecord = Some(doc)
      } else {
        // Update dbRecord
      }

      true
    })
  }

  def find(id: String) = None


}
