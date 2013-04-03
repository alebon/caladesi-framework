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
import testkit.{OrientMemoryStringRecord, OrientMemoryBooleanRecord, SimpleOrientDocument, OrientDocumentTestKit}

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
      document.save

      val document2 = SimpleOrientDocument.create
      document2.save

      SimpleOrientDocument.countClass() must_==(2)

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

  "Caladesi Oriendb Record with Boolean fields" should {
    "save and load boolean values properly" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create
      booleanRecord.booleanField.set(true)
      booleanRecord.save

      true must_==(true)
    }

    "return the correct default value for a BooleanField" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create
      booleanRecord.save

      booleanRecord.booleanFieldWithDefault.get must_==(true)
    }

    "save and load optional boolean value properly" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create
      booleanRecord.optionalBooleanField.set(true)
      booleanRecord.save

      booleanRecord.optionalBooleanField.getOrElse(false) must_==(true)
    }

    "save and load optional (not set) boolean value properly" in OrientMemoryTestContext {

      val booleanRecord = OrientMemoryBooleanRecord.create
      booleanRecord.save

      booleanRecord.optionalBooleanField.getOrElse(false) must_==(false)
    }
  }

  "Caladesi Oriendb Record with String fields" should {
    "save and load string values properly" in OrientMemoryTestContext {

      val stringRecord = OrientMemoryStringRecord.create
      stringRecord.stringField.set("string-field-test")
      stringRecord.save

      true must_==(true)
    }

    "return the correct default value for a StringField" in OrientMemoryTestContext {

      val stringRecord = OrientMemoryStringRecord.create
      stringRecord.save

      stringRecord.stringFieldWithCustomDefault.get must_==("customDefault")
    }

    "save and load optional string value properly" in OrientMemoryTestContext {

      val stringRecord = OrientMemoryStringRecord.create
      stringRecord.optionalStringField.set("this-is-optional")
      stringRecord.save

      stringRecord.optionalStringField.getOrElse("alternative-value") must_==("this-is-optional")
    }

    "save and load optional (not set) string value properly" in OrientMemoryTestContext {

      val stringRecord = OrientMemoryStringRecord.create
      stringRecord.save

      stringRecord.optionalStringField.getOrElse("alternative-value") must_==("alternative-value")
    }
  }

}
