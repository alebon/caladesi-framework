package net.caladesiframework.neo4j.testkit.record

import net.caladesiframework.neo4j.record.{Neo4jMetaEntity, Neo4jEntity}

class TestEntity extends Neo4jEntity[TestEntity] {

  def meta = TestEntity


}

object TestEntity extends TestEntity with Neo4jMetaEntity[TestEntity] {

}
