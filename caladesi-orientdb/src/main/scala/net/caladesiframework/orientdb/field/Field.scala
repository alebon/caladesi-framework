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

package net.caladesiframework.orientdb.field

import net.caladesiframework.orientdb.entity.Entity

trait Field[T] {
  var value: T = defaultValue

  var owner: Entity = null

  val optional: Boolean = true

  def me = this

  def set(value: T) {
    this.value = value
    // Add field to owner entity
    if (!owner.fields.contains(this)) {
      owner attach me
    }
  }

  def defaultValue: T

  /**
   * Determines the name of the field for DB
   *
   * @return
   */
  def name = {
    this.getClass.getName.split('$').last
  }

  def is = this.value
}
