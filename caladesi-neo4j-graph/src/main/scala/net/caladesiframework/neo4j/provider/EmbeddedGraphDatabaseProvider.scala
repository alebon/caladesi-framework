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

package net.caladesiframework.neo4j.provider

import org.neo4j.kernel.EmbeddedGraphDatabase
import net.caladesiframework.neo4j.db.{DatabaseServiceImpl, Neo4jDatabaseService}
import java.util.{HashMap => jMap}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.{GraphDatabaseSettings, GraphDatabaseFactory}
import org.neo4j.graphdb.config.Setting

/**
 * provides a specific Database Service
 * in this case an embedded database service
 */
trait EmbeddedGraphDatabaseServiceProvider extends GraphDatabaseServiceProvider {

  /**
   * directory where to store the data files
   */
  def neo4jStoreDir: String

  /**
   * setup configuration parameters
   * @return Map[String, String] configuration parameters
   */
  def configParams = Map[String, String]()

  /**
   * using an instance of an embedded graph database
   */
  val ds: Neo4jDatabaseService = {

    val databaseService = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(neo4jStoreDir)
      .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")


    DatabaseServiceImpl(
      databaseService.newGraphDatabase()
    )
  }

  /**
   * Shutdown hook
   *
   * @param gds
   */
  protected def registerShutdownHook(gds: GraphDatabaseService) = {
    Runtime.getRuntime.addShutdownHook(
      new Thread() {
        override def run () = {
          println("Running shutdown hook")
          gds.shutdown()
        }
      }
    )
  }

}
