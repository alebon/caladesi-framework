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

package net.caladesiframework.neo4j.repository

import collection.mutable.HashMap
import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.graph.repository.Neo4jGraphRepository

object RepositoryRegistry {

  // Private map ClassName -> Repository
  private val map = new HashMap[String, Any]()

  /**
   * Register a repository to access entities
   *
   * @param repository
   * @tparam EntityType
   * @return
   */
  def register[EntityType <: Neo4jGraphEntity](repository: Neo4jGraphRepository[EntityType]) = {
    println("Registering " + repository.create.clazz)
    map.put(repository.create.clazz, repository)
  }

  /**
   * Is repository registered for the given entity?
   *
   * @return
   */
  def contains(key: String): Boolean = {
    map.contains(key)
  }

  /**
   * Remove repository from registry
   *
   * @param repository
   * @tparam EntityType
   * @return
   */
  def remove[EntityType <: Neo4jGraphEntity](repository: Neo4jGraphRepository[EntityType]) = {
    map.remove(repository.create.clazz)

    //this.contains(t.getClass.getName) match {
    //  case true => map.remove(t.getClass.getName)
    //  case _ => // Ok, fine, nothing to remove
    //}
  }

  /**
   * Get the repository for given entity type to work with
   *
   * @tparam EntityType
   * @return
   */
  def get[EntityType <: Neo4jGraphEntity](key: String): Neo4jGraphRepository[EntityType] = {
    map.get(key) match {
      case Some(value) =>
        map.get(key).get.asInstanceOf[Neo4jGraphRepository[EntityType]]
      case None => throw new Exception("No repository for " + key + " specified")
    }
  }

  def dumpRegisteredServices = {
    println(map.flatMap(repo => repo.toString()))
  }
}
