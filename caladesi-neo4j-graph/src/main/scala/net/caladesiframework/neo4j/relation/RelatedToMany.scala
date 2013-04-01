/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.neo4j.relation

import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.field.Field
import net.caladesiframework.neo4j.entity.Entity
import collection.mutable.HashMap

class RelatedToMany[EntityType <: Neo4jGraphEntity](implicit m:Manifest[EntityType])
  extends Field[HashMap[Long, EntityType]] with Relation {

  override lazy val defaultValue : HashMap[Long, EntityType] = HashMap()

  override val optional = false

  /**
   * Init the field with default value
   *
   * @param ownerEntity
   */
  def this(ownerEntity: Entity, relation: String)(implicit m:Manifest[EntityType]) = {
    this()
    owner = ownerEntity
    set(defaultValue)
    RELATION_NAME = relation
  }

  /**
   * Set the field with value
   *
   * @param ownerEntity
   * @param value
   */
  def this(ownerEntity: Entity, value: HashMap[Long, EntityType], relation: String)(implicit m:Manifest[EntityType]) = {
    this()
    owner = ownerEntity
    set(value)
    RELATION_NAME = relation
  }

  /**
   * Add the entity
   *
   * @param value
   * @return
   */
  def put(value: EntityType) = {
    if (!value.hasInternalId()) {
      throw new Exception("Please save the related entities before assigning to RelatedToMany")
    }

    if (!this.value.contains(value.getUnderlyingNode.getId)) {
      this.value.put(value.getUnderlyingNode.getId, value)
    }
  }

  /**
   * Remove the entity
   *
   * @param value
   * @return
   */
  def remove(value: EntityType) = {
    if (!value.hasInternalId()) {
      throw new Exception("Can't remove not persisted entity")
    }

    if (this.value.contains(value.getUnderlyingNode.getId)) {
      this.value.remove(value.getUnderlyingNode.getId)
    }
  }

  def has(key: Long) = {
    this.value.contains(key)
  }

  def targetClazz = m.erasure.newInstance().asInstanceOf[EntityType]
}