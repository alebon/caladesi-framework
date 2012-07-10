package net.caladesiframework.orientdb.graph.field

import org.specs2.mutable._
import net.caladesiframework.orientdb.graph.entity.GraphEntity
import net.caladesiframework.orientdb.graph.testkit.TestEntity

class StringFieldSpec extends SpecificationWithJUnit {

  var testEntity : TestEntity = new TestEntity()

  "StringField" should {
    "accept and reuturn values properly" in {

      testEntity.stringField.set("This is a test")
      testEntity.stringField.is must_==("This is a test")
    }

    "be not optional" in {

      testEntity.stringField.optional must_==false
    }
  }
}
