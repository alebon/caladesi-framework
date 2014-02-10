package net.caladesiframework.neo4j.entity

import org.specs2.mutable.SpecificationWithJUnit
import net.caladesiframework.neo4j.testkit.Neo4jDatabaseTestKit
import net.caladesiframework.neo4j.record.Neo4jEntity
import net.caladesiframework.neo4j.testkit.record.TestEntity

class Neo4jRecordSpec extends SpecificationWithJUnit
  with Neo4jDatabaseTestKit {

  sequential

  "Neo4j Records" should {


    "init itself properly" in {

      val entity = TestEntity.createRecord

      true must_==(true)
    }
  }

}
