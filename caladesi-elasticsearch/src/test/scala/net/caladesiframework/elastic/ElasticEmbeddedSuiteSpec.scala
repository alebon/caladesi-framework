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

package net.caladesiframework.elastic

import org.specs2.mutable.Specification
import net.caladesiframework.elastic.testkit.ElasticTestKit
import net.caladesiframework.elastic.testkit.embedded.{ElasticEmbeddedDynamicPropsRecord, ElasticEmbeddedStringRecord}
import java.util.UUID
import org.elasticsearch.search.facet.terms.TermsFacet
import scala.collection.immutable.HashMap

class ElasticEmbeddedSuiteSpec extends Specification with ElasticTestKit {

  sequential

  "Caladesi Elastic (Embedded) Record" should {
    "be able to store records in index" in ElasticEmbeddedTestContext {

      val recordDynamicProperties = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties._uuid.set(UUID.randomUUID())
      recordDynamicProperties.dynamicProperties.set(Map("property1" -> "test1", "property2" -> "test2"))
      recordDynamicProperties.save

      val record = ElasticEmbeddedStringRecord.createRecord
      record._uuid.set(UUID.randomUUID())
      record.stringField.set("A text with some words")
      record.facetField.set("two")
      record.tagField.set("tag1")
      record.save

      ElasticEmbeddedStringRecord.deleteAll

      val record1 = ElasticEmbeddedStringRecord.createRecord
      record1._uuid.set(UUID.randomUUID())
      record1.stringField.set("A text with some words")
      record1.facetField.set("two")
      record1.tagField.set("tag1")
      record1.save

      val record2 = ElasticEmbeddedStringRecord.createRecord
      record2._uuid.set(UUID.randomUUID())
      record2.stringField.set("A text with some words 2")
      record2.facetField.set("one")
      record2.tagField.set("tag1")
      record2.save

      val record3 = ElasticEmbeddedStringRecord.createRecord
      record3._uuid.set(UUID.randomUUID())
      record3.stringField.set("Different text")
      record3.facetField.set("")
      record3.save

      val record4 = ElasticEmbeddedStringRecord.createRecord
      record4._uuid.set(UUID.randomUUID())
      record4.stringField.set("Different text 2")
      record4.facetField.set("two")
      record4.tagField.set("tag2")
      record4.save

      val recordUuid = record2._uuid.get.toString
      val foundRecord = ElasticEmbeddedStringRecord.findById(recordUuid)

      ElasticEmbeddedStringRecord.count must_== 4
      foundRecord.get._uuid.get.toString must_==(recordUuid)

      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "xyz").size must_==(0)
      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "with words").size must_==(2)
      ElasticEmbeddedStringRecord.query(ElasticEmbeddedStringRecord.stringField, "some").size must_==(2)

      val termsFacet: TermsFacet = ElasticEmbeddedStringRecord.getFacetsForField(ElasticEmbeddedStringRecord.facetField)
      termsFacet.getEntries.size() must_!=(0)

      termsFacet.getEntries.toArray foreach(entry => {
        println("=*=" + entry.asInstanceOf[TermsFacet.Entry].getTerm)
      })

      // One doc with empty term
      termsFacet.getMissingCount must_==(1)

      // terms "one" and "two"
      termsFacet.getTotalCount must_==(3)

      val filterResult = ElasticEmbeddedStringRecord.queryFiltered(HashMap(ElasticEmbeddedStringRecord.tagField -> "tag1"))
      filterResult.size must_==(2)

      val filterResult1 = ElasticEmbeddedStringRecord.queryFiltered(
        HashMap(ElasticEmbeddedStringRecord.tagField -> "tag1",
          ElasticEmbeddedStringRecord.facetField -> "two"
        )
      )
      filterResult1.size must_==(1)

      // Test dynamic properties
      val recordDPUuid = recordDynamicProperties._uuid.get.toString
      val foundRecordDP = ElasticEmbeddedDynamicPropsRecord.findById(recordDPUuid)

      foundRecordDP must_!=(None)
      foundRecordDP.get.dynamicProperties.get.size must_==(2)
    }
  }
}
