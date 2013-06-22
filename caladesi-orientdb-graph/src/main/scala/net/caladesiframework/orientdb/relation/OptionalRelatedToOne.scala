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

package net.caladesiframework.orientdb.relation

import net.caladesiframework.orientdb.field.Field
import net.caladesiframework.orientdb.graph.entity.OrientGraphEntity
import net.caladesiframework.orientdb.entity.Entity

class OptionalRelatedToOne[EntityType <: OrientGraphEntity](implicit tag: scala.reflect.ClassTag[EntityType])
  extends Field[EntityType] with Relation {

  override lazy val defaultValue : EntityType =
    tag.runtimeClass.newInstance().asInstanceOf[EntityType]

  override val optional = true

  protected var shouldReset = false

  /**
   * Init the field with default value
   *
   * @param ownerEntity
   */
  def this(ownerEntity: Entity, relation: String)(implicit tag: scala.reflect.ClassTag[EntityType]) = {
    this()
    owner = ownerEntity
    set(defaultValue)
    relationName = relation
  }

  /**
   * Set the field with value
   *
   * @param ownerEntity
   * @param value
   */
  def this(ownerEntity: Entity, value: EntityType, relation: String)(implicit tag: scala.reflect.ClassTag[EntityType]) = {
    this()
    owner = ownerEntity
    set(value)
    relationName = relation
  }

  /**
   * Internally, marks the relation to be removed
   */
  def reset = this.shouldReset = true

  def markedToBeRemoved = this.shouldReset

  /**
   * Since this is an optional value
   *
   * @return
   */
  def isOpt : Option[EntityType] = {
    if (this.value.hasInternalId()) {
      Some(this.value)
    } else {
      None
    }
  }

  /**
   * Special handling for optional field
   *
   * @return
   */
  override def is = {
    if (this.value.hasInternalId()) {
      this.value
    } else {
      throw new Exception("Empty field value - you are using an optional field, try isOpt instead!")
    }
  }

}
