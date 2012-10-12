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
import net.caladesiframework.neo4j.relation.OptionalRelatedToOne

class Neo4jTestEntityWithOptionalRelation(init: Boolean = true)
  extends Neo4jGraphEntity {

  object optionalRelatedToOne extends OptionalRelatedToOne[Neo4jTestEntity](this, "OPTIONAL")

  def this() = {
    this(true)
    uuid.name
    optionalRelatedToOne.name
  }

}

object Neo4jTestEntityWithOptionalRelation extends Neo4jTestEntityWithOptionalRelation
