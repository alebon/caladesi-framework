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

import org.neo4j.graphdb.Node

abstract class Neo4jGraphEntity extends GraphEntity {

  private var underlyingNode: Node = _

  def getUnderlyingNode: Node = return this.underlyingNode

  /**
   * Assigns the node
   *
   * @param node
   */
  def setUnderlyingNode(node: Node) = {
    underlyingNode = node
    assignInternalId(node.getId.toString)
  }

}
