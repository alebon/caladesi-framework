/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.graph.testkit

import net.caladesiframework.orientdb.graph.entity.{UUIDPk, OrientGraphEntity}
import net.caladesiframework.orientdb.relation.RelatedToOne

class TestEntityWithMultipleRelations(init: Boolean = true) extends OrientGraphEntity with UUIDPk {

  object testEntityFirst extends RelatedToOne[TestEntity](this, "FIRST")

  object testEntitySecond extends RelatedToOne[TestEntity](this, "SECOND")

  def this() = {
    this(true)

    uuid.name
    testEntityFirst.name
    testEntitySecond.name
  }
}

object TestEntityWithMultipleRelations extends TestEntityWithMultipleRelations
