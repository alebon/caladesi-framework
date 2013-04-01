/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.document

import org.specs2.mutable.Specification
import testkit.{SimpleOrientDocument, OrientDocumentTestKit}

class OrientDocumentCrudSpec extends Specification with OrientDocumentTestKit {

  sequential


  "Caladesi Oriendb Record" should {
    "be able to create records" in OrientEmbeddedTestContext {

      val document = SimpleOrientDocument.create

      document.getClass.getSimpleName must_==("SimpleOrientDocument")

    }
  }

}
