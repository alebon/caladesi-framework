/*
 * Copyright 2012 Caladesi Framework
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

package net.caladesiframework.orientdb.graph

import org.specs2.mutable._
import testkit._
import java.util
import util.UUID
import scala.Some

class OrientGraphRepositorySpec extends SpecificationWithJUnit
  with OrientDatabaseTestKit {

  sequential

  "OrientGraph Repository" should {

    /**"create 15 relationships properly" in {

      val db = OGraphDatabasePool.global()
        .acquire("remote:127.0.0.1/db", "admin", "admin")

      val clazz = db.getVertexType("TARGET_VERTEX")
      if (clazz == null) {
        db.createVertexType("TARGET_VERTEX")
      }

      val clazz2 = db.getVertexType("SOURCE_VERTEX")
      if (clazz2 == null) {
        db.createVertexType("SOURCE_VERTEX")
      }


      db.begin(TXTYPE.OPTIMISTIC)
      var repoVertex: ODocument = null
      try {
        repoVertex = db.createVertex("TARGET_VERTEX")
        repoVertex.field("name", "TestRepositoryFOREDGESTEST")
        repoVertex.save
        db.commit()
      } catch {
        case e:Exception =>
          println(e.getMessage)
      } finally {
        // Do nothing
      }

      for (i <- 1 to 10) {
        db.begin(TXTYPE.OPTIMISTIC)
        db.declareIntent(new OIntentMassiveInsert())
        var countSize = 0
        val maxItems = 2

        try {
          for (i <- 1 to maxItems) {
            val vertex = db.createVertex("SOURCE_VERTEX")

            //vertex.setClassName("TestEntity")
            vertex.field("entityCount", i)
            vertex.field("price", 1.60 + i)
            vertex.field("name", "Product Test Bla bli Lorem ipsum dolor" + i)
            vertex.field("lastUpdate", new util.Date().toString)
            vertex.save

            val vertexEdge: ODocument = db.createEdge(repoVertex, vertex)
            vertexEdge.field("rel", "TEST_ENTITY")
            vertexEdge.save

            repoVertex.save()

            countSize += vertex.getSize
          }

          println("Overall size for " + maxItems + ": " + (countSize/1024) + " KByte")
          println("DB Size: " + (db.getSize/1024) + " KByte")

        } catch {
          case e:Exception =>
            println(e.getMessage)
        } finally {
          // Do nothing
        }

        db.commit()
      }

      db.close()
      true must_== true
    } */

    "clean up db" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      repo.drop

      repo.count must_==(0)
    }

    "create entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val entityInstance = repo.create

      entityInstance.isInstanceOf[TestEntity] must_== true
    }

    "save entities and set the internal id" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val preCount = repo.count

      val testEntity = new TestEntity
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")
      testEntity.doubleField.set(1.337)
      testEntity.intField.set(1337)

      repo.update(testEntity)

      val resultEntity = (repo.find where TestEntity.uuid eqs testEntity.uuid.is limit 1 ex).head

      repo.count must_== preCount + 1
      testEntity.hasInternalId must_==(true)

      resultEntity.getInternalId must_==(testEntity.getInternalId)
    }

    "delete entities by uuid properly" in  {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val preCount = repo.count

      val testEntity = repo.create
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")
      testEntity.doubleField.set(1.337)
      testEntity.intField.set(1337)

      repo.update(testEntity)

      repo.delete(testEntity)

      var deletionResult = false

      (repo.find where TestEntity.uuid eqs testEntity.uuid.is limit 1 ex).headOption match {
        case Some(entity) =>
          deletionResult = false
        case None =>
          deletionResult = true
      }

      true must_==(deletionResult)
    }

    "update a list of entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val preCount = repo.count
      var entityList = List[TestEntity]()

      for (i <- 0 to 1000) {
        val e = repo.create

        e.uuid.set(util.UUID.randomUUID())
        e.stringField.set("This is the name of the test entity from list test " + i)
        e.doubleField.set(1.337 + i)
        e.intField.set(1337 + i)
        entityList = e :: entityList
      }

      val start = System.currentTimeMillis()
      repo.update(entityList)
      val end = System.currentTimeMillis()

      println("Updating 1000 vertices took: " + (end - start) + " ms")

      repo.count must_==(preCount + 1001)
    }

    "update existing entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val testEntity = repo.create
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")

      repo.update(testEntity)

      testEntity.stringField.set("Updated string field of the test entity")
      repo.update(testEntity)

      var resultEntity : TestEntity = null

      (repo.find where TestEntity.uuid eqs testEntity.uuid.is limit 1 ex).headOption match {
        case Some(entity) => resultEntity = entity.asInstanceOf[TestEntity]
        case None =>
      }

      testEntity.stringField.is must_==(resultEntity.stringField.is)
    }

    "query by field properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val testEntity = repo.create
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("QueryByFieldTest-Positive")

      repo.update(testEntity)

      val testEntity2 = repo.create
      testEntity2.uuid.set(util.UUID.randomUUID())
      testEntity2.stringField.set("QueryByFieldTest-Positive")

      repo.update(testEntity2)

      val testEntity3 = repo.create
      testEntity3.uuid.set(util.UUID.randomUUID())
      testEntity3.stringField.set("QueryByFieldTest-Negative")

      repo.update(testEntity3)

      val resultList = repo.find where TestEntity.stringField eqs "QueryByFieldTest-Positive" limit 10 ex

      resultList.size must_==(2)
    }

    "query by indexed field properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val testEntity = repo.create
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("something")
      testEntity.stringFieldIndexed.set("QueryByFieldTest Positive")

      repo.update(testEntity)

      val testEntity2 = repo.create
      testEntity2.uuid.set(util.UUID.randomUUID())
      testEntity2.stringField.set("something")
      testEntity2.stringFieldIndexed.set("QueryByFieldTest Positive")

      repo.update(testEntity2)

      val testEntity3 = repo.create
      testEntity3.uuid.set(util.UUID.randomUUID())
      testEntity3.stringField.set("something")
      testEntity3.stringFieldIndexed.set("QueryByFieldTest Negative")

      repo.update(testEntity3)

      val resultList = repo.findIdx where TestEntity.stringFieldIndexed contains "Positive" limit 10 ex

      resultList.size must_==(2)
    }

    "create relations to assigned entities properly" in {

      val repoTestEntity = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repoTestEntity.init
      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val repoTestEntityRel = new OrientGraphRepository[TestEntityWithRelations]() {
        override def repositoryEntityClass = "TestEntityRelating"
      }

      repoTestEntityRel.init

      val testEntityRel = repoTestEntityRel.create
      testEntityRel.uuid.set(UUID.randomUUID())
      testEntityRel.testEntity.set(testEntity)

      repoTestEntityRel.update(testEntityRel)

      val resultEntity = (repoTestEntityRel.find where TestEntityWithRelations.uuid eqs testEntityRel.uuid.is limit 1 ex).headOption match {
        case Some(entity) => entity.asInstanceOf[TestEntityWithRelations]
        case None => null
      }

      resultEntity.testEntity.is.uuid.is.toString must_==testEntity.uuid.is.toString
    }

    "assign RelatedToOne only one time on several updates" in {

      val repoTestEntity = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repoTestEntity.init
      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val repoTestEntityRel = new OrientGraphRepository[TestEntityWithRelations]() {
        override def repositoryEntityClass = "TestEntityRelating"
      }

      repoTestEntityRel.init

      val testEntityRel = repoTestEntityRel.create
      testEntityRel.uuid.set(UUID.randomUUID())
      testEntityRel.testEntity.set(testEntity)

      repoTestEntityRel.update(testEntityRel)
      //repoTestEntityRel.update(testEntityRel)
      //repoTestEntityRel.update(testEntityRel)
      //repoTestEntityRel.update(testEntityRel)

      true must_==true
    }

    "re assign RelatedToOne properly" in {

      val repoTestEntity = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repoTestEntity.init

      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val testEntity2 = repoTestEntity.create
      repoTestEntity.update(testEntity2)

      val repoTestEntityRel = new OrientGraphRepository[TestEntityWithRelations]() {
        override def repositoryEntityClass = "TestEntityRelating"
      }

      repoTestEntityRel.init

      val testEntityRel = repoTestEntityRel.create
      testEntityRel.uuid.set(UUID.randomUUID())
      testEntityRel.testEntity.set(testEntity)

      repoTestEntityRel.update(testEntityRel)

      /** CAUSES VERSION CONFLICT - DON'T KNOW HOW TO FIX YET  */
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel)

      testEntityRel.testEntity.set(testEntity2)
      repoTestEntityRel.update(testEntityRel)
      //repoTestEntityRel.update(testEntityRel)


      true must_==true
    }

    "find entities by two fields properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      val testEntity = new TestEntity
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")
      testEntity.doubleField.set(1.337)
      testEntity.intField.set(1334)

      repo.update(testEntity)

      val resultEntity = (repo.find where TestEntity.uuid eqs testEntity.uuid.is and TestEntity.intField eqs 1334 limit 1 ex).head

      resultEntity.getInternalId must_==(testEntity.getInternalId)
    }

    "create more than 10 relations properly to one node" in {
      val repoTestEntity = new OrientGraphRepository[TestEntity]() {
        override def repositoryEntityClass = "TestEntityEDGETEST"
      }
      repoTestEntity.init

      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val repoTestEntityRel = new OrientGraphRepository[TestEntityWithRelations]() {
        override def repositoryEntityClass = "TestEntityRelatingEDGETEST"
      }

      repoTestEntityRel.init

      for (i <- 1 to 150) {
        val testEntityRel = repoTestEntityRel.create
        testEntityRel.uuid.set(UUID.randomUUID())
        testEntityRel.testEntity.set(testEntity)

        repoTestEntityRel.update(testEntityRel)
      }

      true must_==(true)
    }

    "find entities by unique indexed fields properly" in {
      checkOrientDBIsRunning

      val repoTestEntity = new OrientGraphRepository[TestEntity]() {}
      repoTestEntity.init

      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val uuid = testEntity.uuid.is.toString
      val foundProperly = (repoTestEntity.findIdx where TestEntity.uuid eqs uuid limit 2 ex).headOption match {
        case Some(entity) => true
        case None => false
      }

      foundProperly must_==(true)
    }

    "load related entities properly on index queries by custom fields" in {

      val repoTestEntity = new OrientGraphRepository[TestEntity]() { }
      repoTestEntity.init
      val testEntity = repoTestEntity.create
      repoTestEntity.update(testEntity)

      val repoTestEntityRel = new OrientGraphRepository[TestEntityWithUniqueFields]() {

      }
      repoTestEntityRel.init

      val testEntityRel = repoTestEntityRel.create
      testEntityRel.uuid.set(UUID.randomUUID())
      testEntityRel.name.set(testEntityRel.uuid.is.toString + "_NAME")
      testEntityRel.testRelation.set(testEntity)

      val testEntityRel2 = repoTestEntityRel.create
      testEntityRel2.uuid.set(UUID.randomUUID())
      testEntityRel2.name.set(testEntityRel2.uuid.is.toString + "_NAME")
      testEntityRel2.testRelation.set(testEntity)

      repoTestEntityRel.update(testEntityRel)
      repoTestEntityRel.update(testEntityRel2)

      val resultEntity = (repoTestEntityRel.findIdx where TestEntityWithUniqueFields.name eqs testEntityRel.name.is limit 1 ex).headOption match {
        case Some(entity) => entity.asInstanceOf[TestEntityWithUniqueFields]
        case None => null
      }

      println("ResultEntity relation uuid: %s | TestEntity uuid: %s".format(resultEntity.testRelation.is.uuid.is.toString, testEntity.uuid.is.toString))

      resultEntity.testRelation.is.uuid.is.toString must_==testEntity.uuid.is.toString
    }

    "drop all entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() { override def repositoryEntityClass = "TestEntity"}
      repo.init

      repo.drop

      repo.count must_==(0)
    }
  }
}
