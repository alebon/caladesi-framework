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
import org.elasticsearch.index.query.FilterBuilders

class ElasticEmbeddedSuiteSpec extends Specification with ElasticTestKit {

  sequential

  "Caladesi Elastic (Embedded) Record" should {
    "be able to store records in index" in ElasticEmbeddedTestContext {

      val recordDynamicProperties = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties._uuid.set(UUID.randomUUID())
      recordDynamicProperties.dynamicProperties.set(Map("property1" -> "test1", "property2" -> "test2", "property3" -> "test with words"))
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
        //println("=*=" + entry.asInstanceOf[TermsFacet.Entry].getTerm)
      })

      // One doc with empty term
      termsFacet.getMissingCount must_==(1)

      // terms "one" and "two"
      termsFacet.getTotalCount must_==(3)

      val filterResult = ElasticEmbeddedStringRecord.queryFiltered(Map(ElasticEmbeddedStringRecord.tagField -> "tag1"))
      filterResult.size must_==(2)

      val filterResult1 = ElasticEmbeddedStringRecord.queryFilteredWithFacets(
        Map(ElasticEmbeddedStringRecord.tagField -> "tag1",
          ElasticEmbeddedStringRecord.facetField -> "two"
        ),
        List(ElasticEmbeddedStringRecord.facetField)
      )
      filterResult1._1.size must_==(1)
      filterResult1._2.getFacets()
        .get(ElasticEmbeddedStringRecord.facetField.name + "Facet").asInstanceOf[TermsFacet].getEntries.toArray.foreach(term => {

        val facetTerm = term.asInstanceOf[TermsFacet.Entry]
        if (facetTerm.getTerm.toString.equals("two")) {
          facetTerm.getCount must_==(1)
        }
      })

      // Test dynamic properties
      val recordDPUuid = recordDynamicProperties._uuid.get.toString
      val foundRecordDP = ElasticEmbeddedDynamicPropsRecord.findById(recordDPUuid)

      foundRecordDP must_!=(None)
      foundRecordDP.get.dynamicProperties.get.size must_==(3)

      val recordDynamicProperties0 = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties0._uuid.set(UUID.randomUUID())
      recordDynamicProperties0.dynamicProperties.set(Map("property1" -> 2, "property2" -> "test2", "property3" -> "test with words 2"))
      recordDynamicProperties0.save

      val recordDynamicProperties2 = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties2._uuid.set(UUID.randomUUID())
      recordDynamicProperties2.dynamicProperties.set(Map("property1" -> 13, "property2" -> "test13", "property3" -> "test with words 13"))
      recordDynamicProperties2.save

      val searchBuilder = ElasticEmbeddedDynamicPropsRecord.searchBuilder

      val rangeFilter = FilterBuilders.rangeFilter("property1")
      rangeFilter.from(0)
      rangeFilter.to(14)

      val filter = FilterBuilders.andFilter()
      filter.add(rangeFilter)

      searchBuilder.setFilter(rangeFilter)

      println(searchBuilder.toString)

      true must_==(true)
    }

    /**"be able to execute range requests" in ElasticEmbeddedTestContext {

      val recordDynamicProperties = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties._uuid.set(UUID.randomUUID())
      recordDynamicProperties.dynamicProperties.set(Map("property1" -> 2, "property2" -> "test2", "property3" -> "test with words 2"))
      recordDynamicProperties.save

      val recordDynamicProperties2 = ElasticEmbeddedDynamicPropsRecord.create
      recordDynamicProperties2._uuid.set(UUID.randomUUID())
      recordDynamicProperties2.dynamicProperties.set(Map("property1" -> 13, "property2" -> "test13", "property3" -> "test with words 13"))
      recordDynamicProperties2.save

      val searchBuilder = ElasticEmbeddedDynamicPropsRecord.searchBuilder

      val rangeFilter = FilterBuilders.rangeFilter("property1")
      rangeFilter.from(0)
      rangeFilter.to(14)

      val filter = FilterBuilders.andFilter()
      filter.add(rangeFilter)

      searchBuilder.setFilter(rangeFilter)

      println(searchBuilder.toString)

      true must_==(true)
    }*/
  }
}
