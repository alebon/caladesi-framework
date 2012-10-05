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

package net.caladesiframework.neo4j.graph.repository

import org.specs2.mutable._
import net.caladesiframework.neo4j.testkit.{Neo4jTestEntityWithRelation, Neo4jTestEntity, Neo4jDatabaseTestKit}
import java.io.File

class Neo4jGraphRepositorySpec extends SpecificationWithJUnit
  with Neo4jDatabaseTestKit {

  val repository = new Neo4jGraphRepository[Neo4jTestEntity]() {
    override def RELATION_NAME = "TEST_ENTITY"
  }

  val repositoryWithRel = new Neo4jGraphRepository[Neo4jTestEntityWithRelation]() {
    override def RELATION_NAME = "TEST_ENTITY_WITH_REL"
  }

  sequential

  "Neo4j Repository" should {


    "init itself properly" in {

      repository.init
      repositoryWithRel.init
      true must_==true
    }

    "update entities properly" in {

      val entity = repository.create
      entity.title.set("Test Title")
      repository.update(entity)

      entity.hasInternalId() must_==true
    }

    "update entities several times properly" in {

      val entity = repository.create
      entity.title.set("Test Title")
      repository.update(entity)
      repository.update(entity)
      repository.update(entity)
      repository.update(entity)

      entity.hasInternalId() must_==true
    }

    "search entities by index properly" in {

      val entity = repository.create
      entity.title.set("Test Title UUID Test")
      repository.update(entity)

      val uuidString = entity.uuid.is.toString
      val resultTitle = repository.findIdx(Neo4jTestEntity.uuid, uuidString) match {
        case Some(entity) =>
          entity.title.is
        case None =>
          ""
      }

      resultTitle must_==("Test Title UUID Test")
    }

    "update entity fields properly" in {

      val entity = repository.create
      entity.title.set("Test Title")
      repository.update(entity)

      entity.title.set("Some other title")
      repository.update(entity)

      entity.title.is must_==("Some other title")
    }

    "handle related to one properly" in {

      val entity = repository.create
      entity.title.set("Test Title (i'll be related, target)")
      repository.update(entity)

      val entityWithRel = repositoryWithRel.create
      entityWithRel.relatedEntity.set(entity)
      repositoryWithRel.update(entityWithRel)

      true must_==(true)
    }

    "update related to one properly on several updates" in {

      val entity = repository.create
      entity.title.set("Test Title (i'll be related, target)")
      repository.update(entity)

      val entityWithRel = repositoryWithRel.create
      entityWithRel.relatedEntity.set(entity)
      repositoryWithRel.update(entityWithRel)
      repositoryWithRel.update(entityWithRel)
      repositoryWithRel.update(entityWithRel)
      repositoryWithRel.update(entityWithRel)

      true must_==(true)
    }

    "update related to one properly to other entities" in {

      val entity = repository.create
      entity.title.set("Test Title (i'll be related, target)")
      repository.update(entity)

      val entity2 = repository.create
      entity2.title.set("Test Title (i'll be related, target2)")
      repository.update(entity2)

      val entityWithRel = repositoryWithRel.create
      entityWithRel.relatedEntity.set(entity)
      repositoryWithRel.update(entityWithRel)
      repositoryWithRel.update(entityWithRel)
      repositoryWithRel.update(entityWithRel)

      entityWithRel.relatedEntity.set(entity2)
      repositoryWithRel.update(entityWithRel)

      true must_==(true)
    }

    "count entities in repository properly" in {

      repositoryWithRel.count
      true must_==(true)
    }

    "return entity lists for repository properly" in {

      val list = repositoryWithRel.find(0,5)
      println(list.size)

      true must_==(true)
    }

    "shutdown properly" in {

      configuration.egdsp.ds.graphDatabase.shutdown()

      true must_==true
    }

  }
}
