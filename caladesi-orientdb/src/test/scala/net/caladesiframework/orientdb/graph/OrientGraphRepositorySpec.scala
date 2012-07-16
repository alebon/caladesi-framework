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
import testkit.{OrientDatabaseTestKit, TestEntity}
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import java.util

class OrientGraphRepositorySpec extends SpecificationWithJUnit
  with OrientDatabaseTestKit {

  sequential

  "OrientGraph Repository" should {
    "create DB in memory" in {
      checkOrientDBIsRunning

      //val db : OGraphDatabase = new OGraphDatabase("memory:db")
      val db : OGraphDatabase = new OGraphDatabase("remote:127.0.0.1/db")

      //if (!db.exists()) {
        //db.create()
      //} else {
        db.open("admin", "admin")
      //}

      db.begin(TXTYPE.NOTX)
      db.declareIntent(new OIntentMassiveInsert())
      var countSize = 0
      val maxItems = 10

      val start = System.currentTimeMillis()
      try {
        val graph = db.createVertex()
        //graph.setClassName("OGraph")
        graph.field("id", 0)
        graph.field("name", "rootNode")
        graph.save

        val repoVertex = db.createVertex()
        repoVertex.field("name", "TestRepository")
        repoVertex.save

        val repoConnection = db.createEdge(graph, repoVertex)
        repoConnection.field("name", "TEST_REPOSITORY")
        repoConnection.save

        val vertex = db.createVertex()
        for (i <- 1 to maxItems) {
          vertex.reset

          //vertex.setClassName("TestEntity")
          vertex.field("entityCount", i)
          vertex.field("price", 1.60 + i)
          vertex.field("name", "Product Test Bla bli Lorem ipsum dolor" + i)
          vertex.field("lastUpdate", new util.Date().toString)

          val vertexEdge = db.createEdge(repoVertex, vertex)
          vertexEdge.field("rel", "TEST_ENTITY")

          vertex.save

          countSize += vertex.getSize
        }
        val end = System.currentTimeMillis()



        println("Insertion time for " + maxItems + " vertices: " + (end-start) + " ms")
        println("Overall size for " + maxItems + ": " + (countSize/1024) + " KByte")
        println("DB Size: " + (db.getSize/1024) + " KByte")


      } catch {
        case e:Exception =>
          println(e.getMessage)
      } finally {
        //db.drop()
        db.close()
      }

      true must_== true
    }

    "create entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() {}
      val entityInstance = repo.create

      entityInstance.isInstanceOf[TestEntity] must_== true
    }

    "save entities and set the internal id" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() {}
      repo.init

      val preCount = repo.count

      val testEntity = new TestEntity
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")
      testEntity.doubleField.set(1.337)
      testEntity.intField.set(1337)

      repo.update(testEntity)

      val resultEntity = repo.findByUuid(testEntity.uuid.is)

      repo.count must_== preCount + 1
      testEntity.hasInternalId must_==(true)

      resultEntity.getInternalId must_==(testEntity.getInternalId)
    }

    "delete entities by uuid properly" in  {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() {}
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

      try {
        repo.findByUuid(testEntity.uuid.is)
      } catch {
        case e:Exception =>
          deletionResult = true
        case _ =>
      }

      true must_==(deletionResult)
    }

    "update a list of entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() {}
      repo.init

      val preCount = repo.count
      var entityList = List[TestEntity]()

      for (i <- 0 to 9) {
        val e = repo.create

        e.uuid.set(util.UUID.randomUUID())
        e.stringField.set("This is the name of the test entity from list test " + i)
        e.doubleField.set(1.337 + i)
        e.intField.set(1337 + i)
        entityList = e :: entityList
      }

      repo.update(entityList)

      repo.count must_==(preCount + 10)
    }

    "update existing entities properly" in {
      checkOrientDBIsRunning

      val repo = new OrientGraphRepository[TestEntity]() {}
      repo.init

      val testEntity = repo.create
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")

      repo.update(testEntity)

      testEntity.stringField.set("Updated string field of the test entity")
      repo.update(testEntity)

      var resultEntity : TestEntity = null
      try {
        resultEntity = repo.findByUuid(testEntity.uuid.is)
      } catch {
        case _ =>
          throw new Exception("Test failed")
      }

      testEntity.stringField.is must_==(resultEntity.stringField.is)
    }
  }
}
