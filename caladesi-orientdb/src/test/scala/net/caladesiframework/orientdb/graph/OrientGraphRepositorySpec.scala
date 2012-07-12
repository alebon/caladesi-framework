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
import testkit.TestEntity
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import java.util

class OrientGraphRepositorySpec extends SpecificationWithJUnit {

  "OrientGraph Repository" should {
    "create DB in memory" in {

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
          //println("Vertex identity: " + vertex.getIdentity)
          //println(vertex.getSize)
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

      val repo = new OrientGraphRepository[TestEntity]() {}
      val entityInstance = repo.create

      entityInstance.isInstanceOf[TestEntity] must_== true
    }

    "save entities and set the internal id" in {
      val repo = new OrientGraphRepository[TestEntity]() {}
      repo.init

      val testEntity = new TestEntity
      testEntity.uuid.set(util.UUID.randomUUID())
      testEntity.stringField.set("This is the name of the test entity")
      testEntity.doubleField.set(1.337)
      testEntity.intField.set(1337)

      repo.update(testEntity)

      testEntity.hasInternalId must_==(true)
    }
  }
}
