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

import net.caladesiframework.neo4j.db.Neo4jDatabaseService

/**
 * Interface for a GraphDatabaseServiceProvider
 * must be implemented by and Graph Database Service Provider
 */
trait GraphDatabaseServiceProvider {
  val ds: Neo4jDatabaseService
}
