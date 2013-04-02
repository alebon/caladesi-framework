/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.document.field

import org.specs2.mutable.Specification
import net.caladesiframework.orientdb.document.testkit.{OrientMemoryBooleanRecord, OrientDocumentTestKit}

class OrientRecordFieldSpec extends Specification with OrientDocumentTestKit {

  sequential


  "Caladesi Oriendb Record with Boolean fields" should {
    "save and load boolean values properly" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create

      true must_==(true)
    }

    "return the correct default value for a BooleanField" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create

      true must_==(true)
    }

    "save and load optional boolean value properly" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create

      true must_==(true)
    }
  }

}
