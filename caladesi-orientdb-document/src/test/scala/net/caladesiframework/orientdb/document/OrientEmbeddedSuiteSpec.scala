/*
 * Copyright 2013 Caladesi Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.caladesiframework.orientdb.document

import org.specs2.mutable.Specification
import testkit._
import net.caladesiframework.orientdb.document.testkit.embedded.{OrientEmbeddedStringRecord, OrientEmbeddedBooleanRecord}

class OrientEmbeddedSuiteSpec extends Specification with OrientDocumentTestKit {

  sequential


  "Caladesi Oriendb (Embedded) Record" should {
    "be able to create records" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create

      document.getClass.getSimpleName must_==("OrientEmbeddedBooleanRecord")

    }

    "have the correct defined amount of fields" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create

      OrientEmbeddedBooleanRecord.fields.count(p => true) must_==(3)

    }

    "have a correct collection name" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create

      OrientEmbeddedBooleanRecord.collectionName must_==("OrientEmbeddedBooleanRecord")

    }

    "return the correct count before insertion of records" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create

      OrientEmbeddedBooleanRecord.count() must_==(0)

    }

    "return the correct count after insertion of records" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create
      document.save

      val document2 = OrientEmbeddedBooleanRecord.create
      document2.save

      OrientEmbeddedBooleanRecord.countClass() must_==(2)

    }

    "return the correct count after deletion of records" in OrientEmbeddedTestContext {

      val document = OrientEmbeddedBooleanRecord.create
      document.save

      document.delete

      OrientEmbeddedBooleanRecord.count() must_==(0)

    }
  }

  "Caladesi Oriendb (Embedded) Record with Boolean fields" should {
    "save and load boolean values properly" in OrientEmbeddedTestContext {

      val booleanRecord = OrientEmbeddedBooleanRecord.create
      booleanRecord.booleanField.set(true)
      booleanRecord.save

      true must_==(true)
    }

    "return the correct default value for a BooleanField" in OrientEmbeddedTestContext {

      val booleanRecord = OrientEmbeddedBooleanRecord.create
      booleanRecord.save

      booleanRecord.booleanFieldWithDefault.get must_==(true)
    }

    "save and load optional boolean value properly" in OrientEmbeddedTestContext {

      val booleanRecord = OrientEmbeddedBooleanRecord.create
      booleanRecord.optionalBooleanField.set(true)
      booleanRecord.save

      booleanRecord.optionalBooleanField.getOrElse(false) must_==(true)
    }

    "save and load optional (not set) boolean value properly" in OrientEmbeddedTestContext {

      val booleanRecord = OrientEmbeddedBooleanRecord.create
      booleanRecord.save

      booleanRecord.optionalBooleanField.getOrElse(false) must_==(false)
    }
  }

  "Caladesi Oriendb (Embedded) Record with String fields" should {
    "save and find records by string values properly" in OrientEmbeddedTestContext {

      val stringRecord = OrientEmbeddedStringRecord.create
      stringRecord.stringField.set("tag1")
      stringRecord.save

      val stringRecord2 = OrientEmbeddedStringRecord.create
      stringRecord2.stringField.set("tag2")
      stringRecord2.save

      val foundRecords = OrientEmbeddedStringRecord.find.where(OrientEmbeddedStringRecord.stringField).eqs("tag1").ex
      val foundRecords2 = OrientEmbeddedStringRecord.find.where(OrientEmbeddedStringRecord.stringField).eqs("tag2").ex
      val foundRecords3 = OrientEmbeddedStringRecord.find
        .where(OrientEmbeddedStringRecord.stringField).eqs("tag2")
        .and(OrientEmbeddedStringRecord.stringField).eqs("tag1").ex

      foundRecords.size must_==(1)
      foundRecords.head.stringField.get must_== ("tag1")

      foundRecords2.size must_==(1)
      foundRecords3.size must_==(0)
      OrientEmbeddedStringRecord.count() must_==(2)
    }
  }

}
