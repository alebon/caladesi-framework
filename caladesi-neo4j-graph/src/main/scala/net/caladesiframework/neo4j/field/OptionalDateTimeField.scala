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

package net.caladesiframework.neo4j.field

import net.caladesiframework.neo4j.entity.Entity
import java.util.Calendar

class OptionalDateTimeField extends Field[Option[Calendar]] {

  override lazy val defaultValue: Option[Calendar] = None

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
   * Set the field with value
   *
   * @param ownerEntity
   * @param value
   */
  def this(ownerEntity: Entity, value: Option[Calendar]) = {
    this()
    owner = ownerEntity
    set(value)
  }

  override def valueToDB = this.is match {
    case Some(calendar) => calendar.getTimeInMillis
    case None => ""
  }

  override def valueFromDB(value: Any) = {
    if (!value.toString.isEmpty) {
      val date = Calendar.getInstance()
      date.setTimeInMillis(value.asInstanceOf[Long])
      this.set(Some(date))
    } else {
      this.set(None)
    }
  }
}