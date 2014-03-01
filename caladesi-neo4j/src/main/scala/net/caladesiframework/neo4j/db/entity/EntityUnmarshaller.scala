package net.caladesiframework.neo4j.db.entity

import org.neo4j.graphdb.Node

trait EntityUnmarshaller[EntityType] extends Neo4jEntity[EntityType] with NodeUnmarshaller[EntityType] {
  self: EntityType =>

  override def unmarshal(node: Node): EntityType = {

    /**
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
    }*/
    val entity = this.meta.createRecord

    entity
  }


}
