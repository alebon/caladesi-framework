/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.neo4j.provider

import org.neo4j.kernel.EmbeddedGraphDatabase
import net.caladesiframework.neo4j.db.{DatabaseServiceImpl, Neo4jDatabaseService}
import java.util.{HashMap => jMap}
import org.neo4j.graphdb.GraphDatabaseService

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
    import collection.JavaConversions.mapAsJavaMap



    DatabaseServiceImpl(
      new EmbeddedGraphDatabase(neo4jStoreDir, new jMap[String, String](configParams))
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