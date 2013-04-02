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
    "be able to create records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      document.getClass.getSimpleName must_==("SimpleOrientDocument")

    }
  }

  "Caladesi Orientdb Record" should {
    "have the correct defined amount of fields" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.fields.count(p => true) must_==(1)

    }
  }

  "Caladesi Orientdb Record" should  {
    "have a correct collection name" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.collectionName must_==("SimpleOrientDocument")

    }
  }

  "Caladesi Orientdb Record" should {
    "return the correct count before insertion of records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.count() must_==(0)

    }
  }

  "Caladesi Orientdb Record" should {


    "return the correct count after insertion of records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create
      document.save()

      SimpleOrientDocument.count() must_==(1)

    }
  }

  "Caladesi Orientdb Record" should {
    "return the correct count after deletion of records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create
      document.save

      document.delete

      SimpleOrientDocument.count() must_==(0)

    }
  }

}
