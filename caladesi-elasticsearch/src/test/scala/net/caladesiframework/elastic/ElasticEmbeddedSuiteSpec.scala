/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.elastic

import org.specs2.mutable.Specification
import net.caladesiframework.elastic.testkit.ElasticTestKit
import net.caladesiframework.elastic.testkit.embedded.ElasticEmbeddedStringRecord
import java.util.UUID

class ElasticEmbeddedSuiteSpec extends Specification with ElasticTestKit {

  sequential

  "Caladesi Elastic (Embedded) Record" should {
    "be able to store records in index" in ElasticEmbeddedTestContext {

      val record = ElasticEmbeddedStringRecord.createRecord
      record._uuid.set(UUID.randomUUID())
      record.stringField.set("A text with some words")
      record.save

      ElasticEmbeddedStringRecord.deleteAll

      val record2 = ElasticEmbeddedStringRecord.createRecord
      record2._uuid.set(UUID.randomUUID())
      record2.stringField.set("A text with some words 2")
      record2.save

      val recordUuid = record2._uuid.get.toString
      val foundRecord = ElasticEmbeddedStringRecord.findById(recordUuid)

      ElasticEmbeddedStringRecord.count must_== 1
      foundRecord.get._uuid.get.toString must_==(recordUuid)

      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "xyz").size must_==(0)
      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "with words").size must_==(1)
      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "some").size must_==(1)
    }
  }
}
