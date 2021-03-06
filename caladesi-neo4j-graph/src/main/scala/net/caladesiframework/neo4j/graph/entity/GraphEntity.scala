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

package net.caladesiframework.neo4j.graph.entity

import net.caladesiframework.neo4j.entity.Entity

abstract class GraphEntity extends Entity with UUIDPk {

  private var internalId : String = null

  /**
   * Init the entity with given internalId
   * (For loading from DB)
   *
   * @param internalId
   */
  def this(internalId : String) = {
    this()
    this.internalId = internalId
  }

  /**
   * Is the GraphEntity represented in the graph
   *
   * @return
   */
  def hasInternalId() = {
    null != this.internalId
  }

  /**
   * Sets internal id
   *
   * @param id
   */
  def assignInternalId(id: String) = {
    this.internalId = id
  }

  /**
   * Returns the internal id assigned from graph
   *
   * @return
   */
  def getInternalId = {
    if (!hasInternalId()) {
      throw new Exception("No internal id")
    }
    this.internalId
  }
}

/**
 * Prototype
 */
object GraphEntity extends GraphEntity {}
