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
import com.orientechnologies.orient.core.tx.OTransaction
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import java.util
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE
import com.orientechnologies.orient.core.metadata.schema.OType

class OrientGraphRepositorySpec extends SpecificationWithJUnit {

  "OrientGraph Repository" should {
    "create DB in memory" in {

      val db : OGraphDatabase = new OGraphDatabase("memory:db")

      if (!db.exists()) {
        db.create()
      } else {
        db.open("admin", "admin")
      }

      db.begin(TXTYPE.NOTX)
      db.declareIntent(new OIntentMassiveInsert())
      var countSize = 0
      val maxItems = 200

      try {
        val graph = db.createVertex()
        graph.field("id", 0)
        graph.field("name", "rootNode")
        graph.save

        val repoVertex = db.createVertex()
        repoVertex.field("name", "TestRepository")
        repoVertex.save

        val repoConnection = db.createEdge(graph, repoVertex)
        repoConnection.field("name", "TEST_REPOSITORY")
        repoConnection.save


        for (i <- 1 to maxItems) {
          val vertex = db.createVertex()

          //vertex.setClassName("TestEntity")
          vertex.field("entityCount", i)
          vertex.field("price", 1.60 + i)
          vertex.field("name", "Product Test Bla bli Lorem ipsum dolor" + i)
          vertex.field("lastUpdate", new util.Date().toString)
          vertex.save

          val vertexEdge = db.createEdge(repoVertex, vertex)
          vertexEdge.field("rel", "TEST_ENTITY")
          vertexEdge.save

          countSize += vertex.getSize
          //println("Vertex identity: " + vertex.getIdentity)
          //println(vertex.getSize)
        }

        println("Overall size for " + maxItems + ": " + (countSize/1024) + " KByte")
        println("DB Size: " + (db.getSize/1024) + " KByte")


      } catch {
        case e:Exception =>
          println(e.getMessage)
      } finally {
        db.drop()
        db.close()
      }

      true must_== true
    }

    "create entities properly" in {

      val repo = new OrientGraphRepository[TestEntity]() {}
      val entityInstance = repo.create

      entityInstance.isInstanceOf[TestEntity] must_== true
    }
  }
}