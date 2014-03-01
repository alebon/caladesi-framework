package net.caladesiframework.neo4j.db.entity

import org.specs2.mutable.SpecificationWithJUnit
import net.caladesiframework.neo4j.db.testkit.Neo4jTestKit

class GraphDatabaseSpec extends SpecificationWithJUnit
  with Neo4jTestKit {

  sequential

  "Simple test" in {



    true must_==(true)
  }

}
