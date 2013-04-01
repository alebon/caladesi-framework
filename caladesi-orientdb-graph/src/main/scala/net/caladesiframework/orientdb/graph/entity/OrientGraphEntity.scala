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

package net.caladesiframework.orientdb.graph.entity

import com.orientechnologies.orient.core.record.impl.ODocument

abstract class OrientGraphEntity extends GraphEntity {

  private var underlyingVertex : ODocument = null

  /**
   * Returns the underlying document of the entity (including vertex data)
   *
   * @return
   */
  def getUnderlyingVertex : ODocument = return this.underlyingVertex

  /**
   * Assigns the vertex
   *
   * @param vertex
   */
  def setUnderlyingVertex(vertex: ODocument) = {
    underlyingVertex = vertex
    assignInternalId(vertex.getIdentity.toString)
  }

}
