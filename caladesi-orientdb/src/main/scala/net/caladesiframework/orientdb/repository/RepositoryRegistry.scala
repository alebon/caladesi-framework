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

package net.caladesiframework.orientdb.repository

import collection.mutable.HashMap
import net.caladesiframework.orientdb.graph.OrientGraphRepository
import net.caladesiframework.orientdb.graph.entity.OrientGraphEntity

object RepositoryRegistry {

  // Private map ClassName -> Repository
  private val map = new HashMap[String, Any]()

  /**
   * Register a repository to access entities
   *
   * @param repository
   * @param t
   * @tparam EntityType
   * @return
   */
  def register[EntityType <: OrientGraphEntity](repository: OrientGraphRepository[EntityType])(implicit t: Manifest[EntityType]) = {
    map.put(t.getClass.getName, repository)
  }

  /**
   * Is repository registered for the given entity?
   *
   * @param entity
   * @param t
   * @tparam EntityType
   * @return
   */
  def contains[EntityType <: OrientGraphEntity](entity: EntityType)(implicit t: Manifest[EntityType]): Boolean = {
    //return map.contains(t.getClass.toString)
    true
  }

  /**
   * Remove repository from registry
   *
   * @param repository
   * @param t
   * @tparam EntityType
   * @return
   */
  def remove[EntityType <: OrientGraphEntity](repository: OrientGraphRepository[EntityType])(implicit t: Manifest[EntityType]) = {
    map.remove(t.getClass.getName)

    //this.contains(t.getClass.getName) match {
    //  case true => map.remove(t.getClass.getName)
    //  case _ => // Ok, fine, nothing to remove
    //}
  }

  /**
   * Get the repository for given entity type to work with
   *
   * @param entity
   * @param t
   * @tparam EntityType
   * @return
   */
  def get[EntityType <: OrientGraphEntity](entity: EntityType)(implicit t: Manifest[EntityType]): OrientGraphRepository[EntityType] = {
    map.get(t.getClass.toString).asInstanceOf[OrientGraphRepository[EntityType]]
  }
}
