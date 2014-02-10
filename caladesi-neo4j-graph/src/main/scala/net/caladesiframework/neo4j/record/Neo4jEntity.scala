/*
 * Copyright 2014 Caladesi Framework
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
package net.caladesiframework.neo4j.record

import net.caladesiframework.record.Record
import org.neo4j.graphdb.{Direction, Label, Node}
import net.caladesiframework.document._
import scala.Some
import java.util.{Calendar, UUID}
import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.index.{IndexedField, Index, IndexManager}


trait Neo4jEntity[RecordType] extends Record[RecordType]
  with Index {
  self: RecordType =>

  def meta: Neo4jMetaEntity[RecordType]

  private [this] var dbRecord: Option[Node] = None

  override def internalId = {
    if (!dbRecord.isEmpty) {
      Some(dbRecord.get.getId.toString)
    } else {
      None
    }
  }

  def getUnderlyingNode = dbRecord

  def applyDbRecord(node: Node) = {
    this.dbRecord = Some(node)
  }

  /**
   * Persists the record
   *
   * @return
   */
  def save = {

    meta.inSyncTrx(implicit db => {

      val node = this.dbRecord match {
        case Some(record) => record
        case _ => val node = db.graphDatabase.createNode(new Label {
            def name(): String = meta.label
          })
          this.dbRecord = Some(node)
          node
      }

      // Copy fields from record to DB Node
      fieldsToDb(node)

      true
    })
  }

  def find(uuid: String): Option[RecordType] = {
    meta.inSyncTrx[Option[RecordType]](implicit ds => {
      findSingleByIndex(this.getFieldByName("_uuid").get.asInstanceOf[Field[_,_] with IndexedField], uuid) match {
        case Some(node) =>
          Some(this.meta.transformToEntity(node, 1))
        case None =>
          None
      }
    })
  }

  protected def getFieldByName(name: String): Option[Field[_,_]] = {
    this.getClass.getDeclaredFields foreach {
      field => {
        field.getType.getMethods foreach {

          // If field contains a method for initialization, call it and add field to meta fields
          method => {
            if  (method.getName.equals("initField")) {
              field.setAccessible(true)
              val m = this.getClass.getMethod(field.getName.replace("$module", ""))

              val fieldObj: Field[_, RecordType] = m.invoke(this).asInstanceOf[Field[_, RecordType]]

              // Return the field if it matches the name
              if (fieldObj.name != null && fieldObj.name.equals(name)) {
                return Some(fieldObj)
              }
            }
          }
        }

      }
    }
    None
  }

  /**
   * Removes entity from db
   *
   * @return
   */
  override def delete = meta.inSyncTrx[Boolean]( implicit ds => {
    this.dbRecord match {
      case Some(node) =>

        // DO NOT remove incoming relations, this will break data integrity (user has to check before deletion)
        def relationsIncoming = node.getRelationships(Direction.INCOMING)
        if (relationsIncoming.iterator().hasNext) {
          throw new Exception("Can not delete entity, still has incoming connections")
        }

        // Remove all outgoing relations
        val relationsOutgoing = node.getRelationships(Direction.OUTGOING)
        while (relationsOutgoing.iterator().hasNext) {
          relationsOutgoing.iterator().next().delete()
        }

        removeFromIndex(this)
        node.delete()
        true

      case None => // Do nothing
        false
    }
})

  private def fieldsToDb(node: Node) = {

    meta.fields foreach {
      metaField => {
        val fieldName = metaField._1

        val m = this.getClass.getMethod(metaField._1)
        val fieldObj: Field[_, RecordType] = m.invoke(this).asInstanceOf[Field[_, RecordType]]

        fieldObj match {
          case f: OptionalStringField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalStringField[RecordType]].getOrElse(""))
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalBooleanField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalBooleanField[RecordType]].getOrElse(false).asInstanceOf[java.lang.Boolean])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalIntField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalIntField[RecordType]].getOrElse(0).asInstanceOf[java.lang.Integer])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalLongField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalLongField[RecordType]].getOrElse(0L).asInstanceOf[java.lang.Long])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalDoubleField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalDoubleField[RecordType]].getOrElse(0.0).asInstanceOf[java.lang.Double])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalUuidField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalUuidField[RecordType]].getOrElse(UUID.randomUUID()).toString)
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalDateTimeField[RecordType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalDateTimeField[RecordType]].getOrElse(Calendar.getInstance()).getTimeInMillis.asInstanceOf[java.lang.Long])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: StringField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[StringField[RecordType]].get)

          case f: BooleanField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[BooleanField[RecordType]].get.asInstanceOf[java.lang.Boolean])

          case f: IntField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[IntField[RecordType]].get.asInstanceOf[java.lang.Integer])

          case f: LongField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[LongField[RecordType]].get.asInstanceOf[java.lang.Long])

          case f: DoubleField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[DoubleField[RecordType]].get.asInstanceOf[java.lang.Double])

          case f: UuidField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[UuidField[RecordType]].get.toString)

          case f: DateTimeField[RecordType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[DateTimeField[RecordType]].get.getTimeInMillis.asInstanceOf[java.lang.Long])

          case _ => throw new RuntimeException("Unhandled field!")
        }
      }
    }

  }

}
