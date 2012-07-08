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

package net.caladesiframework.orientdb.graph

abstract class OrientGraphRepository[T <: OrientGraphEntity] (implicit m:scala.reflect.Manifest[T]) {

  /**
   * Creates a new entity (not persisted)
   *
   * @return
   */
  def create : T = {
    m.erasure.newInstance().asInstanceOf[T]
  }

  /**
   * Saves a new entity to db or updates if already saved
   *
   * @param entity
   * @return
   */
  def persist(entity: T) = {
    throw new Exception("Not implemented yet")
  }

  /**
   * Removes entity from db
   *
   * @param entity
   * @return
   */
  def delete(entity: T) = {
    throw new Exception("Not implemented yet")
  }

}
