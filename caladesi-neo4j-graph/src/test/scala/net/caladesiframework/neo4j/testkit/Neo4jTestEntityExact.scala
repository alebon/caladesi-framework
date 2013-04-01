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
import net.caladesiframework.neo4j.field.StringField
import net.caladesiframework.neo4j.index.UniqueIndexed

class Neo4jTestEntityExact(init:Boolean = true) extends Neo4jGraphEntity {

  object code extends StringField(this) with UniqueIndexed

  def this() = {
    this(true)

    uuid.name
    code.name
  }

}

object Neo4jTestEntityExact extends Neo4jTestEntityExact
