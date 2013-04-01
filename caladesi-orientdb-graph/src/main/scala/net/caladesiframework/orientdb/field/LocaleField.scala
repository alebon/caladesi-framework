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

import java.util.{Locale}
import net.caladesiframework.orientdb.entity.Entity

class LocaleField extends Field[Locale] {

  val defaultValue = Locale.getDefault

  override val optional = false

  /**
   * Init with default value
   *
   * @param ownerEntity
   */
  def this(ownerEntity: Entity) = {
    this()
    owner = ownerEntity
    set(defaultValue)
  }

  /**
   * Init with given value
   *
   * @param ownerEntity
   * @param value
   */
  def this(ownerEntity: Entity, value: Locale) = {
    this()
    owner = ownerEntity
    set(value)
  }

  override def valueToDB = this.value.toString
}
