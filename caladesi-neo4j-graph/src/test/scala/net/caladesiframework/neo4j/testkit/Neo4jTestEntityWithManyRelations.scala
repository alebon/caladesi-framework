/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.neo4j.testkit

import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.relation.RelatedToMany

class Neo4jTestEntityWithManyRelations(init: Boolean = true) extends Neo4jGraphEntity {

  object relationSet extends RelatedToMany[Neo4jTestEntity](this, "MANY")

  def this() = {
    this(true)

    uuid.name
    relationSet.name
  }

}

object Neo4jTestEntityWithManyRelations extends Neo4jTestEntityWithManyRelations
