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
import net.caladesiframework.orientdb.field.StringField
import net.caladesiframework.orientdb.index.UniqueIndexed
import net.caladesiframework.orientdb.relation.RelatedToOne

class TestEntityWithUniqueFields(init: Boolean = true) extends OrientGraphEntity with UUIDPk {

  object name extends StringField(this) with UniqueIndexed

  object testRelation extends RelatedToOne[TestEntity](this, "TEST_REL")

  def this() = {
    this(true)

    uuid.name
    name.name
    testRelation.name
  }

}

object TestEntityWithUniqueFields extends TestEntityWithUniqueFields
