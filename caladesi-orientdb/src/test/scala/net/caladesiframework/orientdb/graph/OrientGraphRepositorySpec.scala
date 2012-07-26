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
import testkit.{TestEntityWithRelations, OrientDatabaseTestKit, TestEntity}
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import java.util
import util.UUID

class OrientGraphRepositorySpec extends SpecificationWithJUnit
  with OrientDatabaseTestKit {

  sequential

  "OrientGraph Repository" should {
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

      val resultEntity = (repo.find where TestEntity.uuid eqs testEntity.uuid.is.toString limit 1 ex).head

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
      try {
        resultEntity = repo.findByUuid(testEntity.uuid.is)
      } catch {
        case _ =>
          throw new Exception("Test failed")
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

      true must_==true
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
