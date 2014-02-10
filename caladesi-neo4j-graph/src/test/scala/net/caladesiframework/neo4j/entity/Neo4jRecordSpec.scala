package net.caladesiframework.neo4j.entity

import org.specs2.mutable.SpecificationWithJUnit
import net.caladesiframework.neo4j.testkit.Neo4jDatabaseTestKit

class Neo4jRecordSpec extends SpecificationWithJUnit
  with Neo4jDatabaseTestKit {

  sequential

  "Neo4j Records" should {


    "init itself properly" in {
      true must_==(true)
    }
  }

}
