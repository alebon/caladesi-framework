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

  "Caladesi Elastic (Embedded) Record" should {
    "be able to store records in index" in ElasticEmbeddedTestContext {

      val record = ElasticEmbeddedStringRecord.createRecord
      record._uuid.set(UUID.randomUUID())
      record.stringField.set("test-value for string field")
      record.save


      val recordUuid = record._uuid.get.toString
      val foundRecord = ElasticEmbeddedStringRecord.findById(recordUuid)

      ElasticEmbeddedStringRecord.count must_== 1
      foundRecord.get._uuid.get.toString must_==(recordUuid)
    }
  }

}
