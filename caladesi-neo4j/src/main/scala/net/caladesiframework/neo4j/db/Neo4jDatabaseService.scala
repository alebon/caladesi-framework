/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.neo4j.db

import org.neo4j.graphdb.GraphDatabaseService

/**
 * Interface for GraphDatabaseService
 *
 */
trait Neo4jDatabaseService {
  def graphDatabase: GraphDatabaseService
}

/**
 * standard DatabaseService implementation
 * for GraphDatabaseService
 */
case class DatabaseServiceImpl(graphDatabase: GraphDatabaseService) extends Neo4jDatabaseService
