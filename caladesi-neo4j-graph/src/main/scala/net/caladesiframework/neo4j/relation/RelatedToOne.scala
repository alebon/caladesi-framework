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

package net.caladesiframework.neo4j.relation

import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.field.Field
import net.caladesiframework.neo4j.entity.Entity

class RelatedToOne[EntityType <: Neo4jGraphEntity](implicit tag: scala.reflect.ClassTag[EntityType])
  extends Field[EntityType] with Relation {

  override lazy val defaultValue : EntityType =
      tag.runtimeClass.newInstance().asInstanceOf[EntityType]

  override val optional = false

  /**
   * Init the field with default value
   *
   * @param ownerEntity
   */
  def this(ownerEntity: Entity, relation: String)(implicit tag: scala.reflect.ClassTag[EntityType]) = {
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
  def this(ownerEntity: Entity, value: EntityType, relation: String)(implicit tag: scala.reflect.ClassTag[EntityType]) = {
    this()
    owner = ownerEntity
    set(value)
    RELATION_NAME = relation
  }
}
