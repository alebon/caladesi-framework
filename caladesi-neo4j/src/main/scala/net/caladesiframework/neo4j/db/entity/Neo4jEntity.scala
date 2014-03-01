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
package net.caladesiframework.neo4j.db.entity

import net.caladesiframework.neo4j.db.index.IndexSource
import net.caladesiframework.field._
import org.neo4j.graphdb.{Direction, Label, Node}
import scala.Some
import java.util.{Calendar, UUID}


trait Neo4jEntity[EntityType] extends IndexSource[EntityType] {
  self: EntityType =>

  def meta: Neo4jMetaEntity[EntityType]

  private [this] var dbRecord: Option[Node] = None

  def internalId = {
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

    meta.withTransaction(implicit db => {

      val node = this.dbRecord match {
        case Some(record) => record
        case _ => val node = db.createNode(new Label {
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

  def find(uuid: String): Option[EntityType] = {
    meta.withTransaction[Option[EntityType]](implicit ds => {
      //findSingleByIndex(this.getFieldByName("_uuid").get.asInstanceOf[Field[_,_] with IndexedField], uuid) match {
      //  case Some(node) =>
      //    Some(this.meta.createEntityFromNode(node, 1))
      //  case None =>
      //    None
      //}

      None
    })
  }

  def create = meta.createRecord

  protected def getFieldByName(name: String): Option[Field[_,_]] = {
    this.getClass.getDeclaredFields foreach {
      field => {
        field.getType.getMethods foreach {

          // If field contains a method for initialization, call it and add field to meta fields
          method => {
            if  (method.getName.equals("initField")) {
              field.setAccessible(true)
              val m = this.getClass.getMethod(field.getName.replace("$module", ""))

              val fieldObj: Field[_, EntityType] = m.invoke(this).asInstanceOf[Field[_, EntityType]]

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
  def delete = meta.withSyncTransaction[Boolean]( implicit ds => {
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

        //removeFromIndex(this)
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
        val fieldObj: Field[_, EntityType] = m.invoke(this).asInstanceOf[Field[_, EntityType]]

        fieldObj match {
          case f: OptionalStringField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalStringField[EntityType]].getOrElse(""))
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalBooleanField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalBooleanField[EntityType]].getOrElse(false).asInstanceOf[java.lang.Boolean])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalIntField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalIntField[EntityType]].getOrElse(0).asInstanceOf[java.lang.Integer])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalLongField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalLongField[EntityType]].getOrElse(0L).asInstanceOf[java.lang.Long])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalDoubleField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalDoubleField[EntityType]].getOrElse(0.0).asInstanceOf[java.lang.Double])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalUuidField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalUuidField[EntityType]].getOrElse(UUID.randomUUID()).toString)
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: OptionalDateTimeField[EntityType] =>
            if (f.hasValue) {
              node.setProperty(fieldName, fieldObj.asInstanceOf[OptionalDateTimeField[EntityType]].getOrElse(Calendar.getInstance()).getTimeInMillis.asInstanceOf[java.lang.Long])
            } else {
              if (node.hasProperty(fieldName)) {node.removeProperty(fieldName)}
            }

          case f: StringField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[StringField[EntityType]].get)

          case f: BooleanField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[BooleanField[EntityType]].get.asInstanceOf[java.lang.Boolean])

          case f: IntField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[IntField[EntityType]].get.asInstanceOf[java.lang.Integer])

          case f: LongField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[LongField[EntityType]].get.asInstanceOf[java.lang.Long])

          case f: DoubleField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[DoubleField[EntityType]].get.asInstanceOf[java.lang.Double])

          case f: UuidField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[UuidField[EntityType]].get.toString)

          case f: DateTimeField[EntityType] =>
            node.setProperty(fieldName, fieldObj.asInstanceOf[DateTimeField[EntityType]].get.getTimeInMillis.asInstanceOf[java.lang.Long])

          case _ => throw new RuntimeException("Unhandled field!")
        }
      }
    }

  }

}
