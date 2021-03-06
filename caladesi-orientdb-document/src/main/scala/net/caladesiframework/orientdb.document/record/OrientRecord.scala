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
import net.caladesiframework.field.Field
import net.caladesiframework.orientdb.document.field._
import scala.Some
import java.util.UUID

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

      val doc = this.dbRecord match {
        case Some(record) => record
        case _ => val document = new ODocument()
          document.setClassName(clazz.getName)
          this.dbRecord = Some(document)
          document
      }

      // Copy field from record to DB Document
      fieldsToDb(doc)

      // Save record to DB
      doc.save(true)
      true


    })
  }

  private def fieldsToDb(doc: ODocument) = {

    meta.fields foreach {
      metaField => {
        val fieldName = metaField._1

        val m = this.getClass.getMethod(metaField._1)
        val fieldObj: Field[_, RecordType] = m.invoke(this).asInstanceOf[Field[_, RecordType]]

        fieldObj match {
          case f: OptionalStringField[RecordType] =>
            if (f.hasValue) {
              doc.field(fieldName, fieldObj.asInstanceOf[OptionalStringField[RecordType]].getOrElse("undefined"))
            } else {
              if (doc.fieldNames().contains(fieldName)) {doc.removeField(fieldName)}
            }

          case f: OptionalBooleanField[RecordType] =>
            if (f.hasValue) {
              doc.field(fieldName, fieldObj.asInstanceOf[OptionalBooleanField[RecordType]].getOrElse(false).asInstanceOf[java.lang.Boolean])
            } else {
              if (doc.fieldNames().contains(fieldName)) {doc.removeField(fieldName)}
            }

          case f: OptionalUuidField[RecordType] =>
            if (f.hasValue) {
              doc.field(fieldName, fieldObj.asInstanceOf[OptionalUuidField[RecordType]].getOrElse(UUID.randomUUID()).toString)
            } else {
              if (doc.fieldNames().contains(fieldName)) {doc.removeField(fieldName)}
            }

          case f: StringField[RecordType] =>
            doc.field(fieldName, fieldObj.asInstanceOf[StringField[RecordType]].get)

          case f: BooleanField[RecordType] =>
            doc.field(fieldName, fieldObj.asInstanceOf[BooleanField[RecordType]].get.asInstanceOf[java.lang.Boolean])

          case f: UuidField[RecordType] =>
            doc.field(fieldName, fieldObj.asInstanceOf[UuidField[RecordType]].get.toString)

          case _ => throw new RuntimeException("Unhandled field!")
        }
      }
    }

  }

  def find(id: String): Option[RecordType] = meta.connected(implicit db => {
    None
  })

  def applyDbRecord(oDocument: ODocument) = {
    this.dbRecord = Some(oDocument)
  }


}
