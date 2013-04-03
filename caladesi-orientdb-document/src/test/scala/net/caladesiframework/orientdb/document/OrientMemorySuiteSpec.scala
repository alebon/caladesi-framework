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
import testkit._
import java.util.UUID

class OrientMemorySuiteSpec extends Specification with OrientDocumentTestKit {

  sequential


  "Caladesi Oriendb Record" should {
    "be able to create records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      document.getClass.getSimpleName must_==("SimpleOrientDocument")

    }

    "have the correct defined amount of fields" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.fields.count(p => true) must_==(1)

    }

    "have a correct collection name" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.collectionName must_==("SimpleOrientDocument")

    }

    "return the correct count before insertion of records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create

      SimpleOrientDocument.count() must_==(0)

    }

    "return the correct count after insertion of records" in OrientMemoryTestContext {

      val document = SimpleOrientDocument.create
      document.save

      val document2 = SimpleOrientDocument.create
      document2.save

      SimpleOrientDocument.countClass() must_==(2)

    }

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

  "Caladesi Oriendb Record with UUID fields" should {
    "save and load UUID values properly" in OrientMemoryTestContext {

      val uuid = UUID.randomUUID()
      val uuidRecord = OrientMemoryUuidRecord.create
      uuidRecord.uuid.set(uuid)
      uuidRecord.save

      uuidRecord.uuid.get.toString must_==(uuid.toString)
    }

    "save and load optional UUID value properly" in OrientMemoryTestContext {

      val uuid = UUID.randomUUID()
      val uuidRecord = OrientMemoryUuidRecord.create
      uuidRecord.optionalUuidField.set(uuid)
      uuidRecord.save

      uuidRecord.optionalUuidField.getOrElse(UUID.randomUUID()).toString must_==(uuid.toString)
    }

    "save and load optional (not set) UUID value properly" in OrientMemoryTestContext {

      val uuid = UUID.randomUUID()
      val uuidRecord = OrientMemoryUuidRecord.create
      uuidRecord.save

      uuidRecord.optionalUuidField.getOrElse(uuid).toString must_==(uuid.toString)
    }
  }

}
