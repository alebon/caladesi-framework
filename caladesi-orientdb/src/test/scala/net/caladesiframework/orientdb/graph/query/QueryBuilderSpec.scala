package net.caladesiframework.orientdb.graph.query

import org.specs2.mutable.SpecificationWithJUnit
import net.caladesiframework.orientdb.graph.testkit.TestEntity
import net.caladesiframework.orientdb.query.QueryBuilder
import net.caladesiframework.orientdb.graph.OrientGraphRepository
import net.caladesiframework.orientdb.db.{OrientHost, OrientConfiguration}

class QueryBuilderSpec extends SpecificationWithJUnit {

  implicit val configuration = OrientConfiguration(host = OrientHost())

  "QueryBuilder" should {
    "assemble simple queries properly" in {

      val repo = new OrientGraphRepository[TestEntity]() {override def repositoryEntityClass = "TestEntity"}

      val testEntity = new TestEntity()
      val sutQb = new QueryBuilder(TestEntity, repo)

      sutQb where testEntity.doubleField eqs 0.0 and testEntity.intField eqs 1 skip 5 limit 5

      sutQb.qryTemp must_==("select from TestEntity where doubleField = ? and intField = ? skip 5 limit 5")
    }
  }

}
