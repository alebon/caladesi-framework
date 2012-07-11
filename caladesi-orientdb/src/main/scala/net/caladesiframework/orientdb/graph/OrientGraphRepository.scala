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

import entity.GraphEntity
import net.caladesiframework.orientdb.repository.CRUDRepository
import repository.{GraphRepository}
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.record.impl.ODocument

abstract class OrientGraphRepository[T <: GraphEntity] (implicit m:scala.reflect.Manifest[T])
  extends GraphRepository[T] with CRUDRepository[T] {

  // @TODO Inject by configuration
  private val graphDB = new OGraphDatabase("remote:127.0.0.1/db")
  private val userName = "admin"
  private val password = "admin"

  // Override to rename repository
  def repositoryIdentifier = "DEFAULT_REPOSITORY"
  def repositoryEntityIdentifier = "DEFAULT_ENTITY"

  /**
   * Creates main repository node (id not present)
   */
  def init() = {
    graphDB.open(userName, password)

    // Init the main repository node
    var root = graphDB.getRoot(repositoryIdentifier) match {
      case null =>
        graphDB.createVertexType("OrientGraphRepository")
        val rootVertex = graphDB.createVertex("OrientGraphRepository")

        graphDB.setRoot(repositoryIdentifier, rootVertex)
        graphDB.getRoot(repositoryIdentifier)
        println("Created main repository node")
      case r : ODocument => r
      case _ =>
        throw new Exception("Unexpected behaviour while repo initialization, please report a bug")
    }

    graphDB.close()
  }

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
  def update(entity: T) = {
    throw new Exception("Not implemented yet")
  }

  /**
   * Saves all given entities or updates the if already present
   *
   * @param list
   * @return
   */
  def update(list: List[T]) = {
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

  /**
   * Returns the overall count of the entities in this repository
   *
   * @return
   */
  def count = {
    throw new Exception("Not implemented yet")
  }

}
