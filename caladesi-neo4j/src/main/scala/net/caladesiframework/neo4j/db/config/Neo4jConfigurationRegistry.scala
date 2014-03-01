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

package net.caladesiframework.neo4j.db.config

import collection.mutable.HashMap

object Neo4jConfigurationRegistry {

  // Private map ClassName -> Repository
  private val map = new HashMap[String, Neo4jConfiguration]()

  /**
   * Register a config
   *
   * @return
   */
  def register(configuration: Neo4jConfiguration, name: String = "default") = {
    map.put(name, configuration)
  }

  /**
   * Loads a config by db identifier name
   *
   * @param name
   * @return
   */
  def loadByIdentifier(name: String): Neo4jConfiguration = {
    map.get(name) match {
      case Some(configuration) => configuration
      case None => throw new RuntimeException("No configuration found for key %s".format(name))
    }
  }

}
