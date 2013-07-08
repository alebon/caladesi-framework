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

package net.caladesiframework.neo4j.field

import net.caladesiframework.neo4j.entity.Entity

class OptionalIntField extends IntField {

  override lazy val defaultValue = 0

  override val optional = true

  /**
   * Init the field with default value
   *
   * @param ownerEntity
   */
  def this(ownerEntity: Entity) = {
    this()
    owner = ownerEntity
    set(defaultValue)
  }

  /**
   * Init the field with value
   *
   * @param ownerEntity
   * @param value
   */
  def this(ownerEntity: Entity, value: Int) = {
    this()
    owner = ownerEntity
    set(value)
  }
}
