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
import embedded.OrientEmbeddedBooleanRecord

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

}
