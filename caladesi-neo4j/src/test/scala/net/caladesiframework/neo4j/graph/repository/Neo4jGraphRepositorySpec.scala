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
import net.caladesiframework.neo4j.testkit._
import scala.Some

class Neo4jGraphRepositorySpec extends SpecificationWithJUnit
  with Neo4jDatabaseTestKit {

  val repository = new Neo4jGraphRepository[Neo4jTestEntity]() {
    override def RELATION_NAME = "TEST_ENTITY"
  }

  val repositoryWithRel = new Neo4jGraphRepository[Neo4jTestEntityWithRelation]() {
    override def RELATION_NAME = "TEST_ENTITY_WITH_REL"
  }

  val repositoryWithExactIndex = new Neo4jGraphRepository[Neo4jTestEntityExact]() {
    override def RELATION_NAME = "TEST_ENTITY_EXACT"
  }

  val repositoryWithOptRel = new Neo4jGraphRepository[Neo4jTestEntityWithOptionalRelation]() {
    override def RELATION_NAME = "TEST_ENTITY_OPTIONAL"
  }

  val repositoryWithManyRel = new Neo4jGraphRepository[Neo4jTestEntityWithManyRelations]() {
    override def RELATION_NAME = "TEST_ENTITY_MANY"
  }


  sequential

  "Neo4j Repository" should {


    "init itself properly" in {

      repository.init
      repositoryWithRel.init
      repositoryWithExactIndex.init
      repositoryWithOptRel.init
      repositoryWithManyRel.init
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

    "load related entities properly" in {

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

      val relatedUuid = repositoryWithRel.findIdx(Neo4jTestEntityWithRelation.uuid, entityWithRel.uuid.is.toString) match {
        case Some(entity) =>
          entity.relatedEntity.is.uuid.is.toString
        case None =>
          ""
      }

      relatedUuid must_==(entity2.uuid.is.toString)
    }

    "remove entities properly (entity and index cleanup)" in {

      val entity = repository.create
      entity.title.set("DELETIONTEST")
      repository.update(entity)

      val uuidString = entity.uuid.is.toString
      val titleString = entity.title.is

      repository.delete(entity)

      val deletedEntity = repository.findIdx(Neo4jTestEntity.uuid, uuidString) match {
        case Some(entity) =>
          "Not Empty"
        case None =>
          ""
      }

      val deletedEntityByTitle = repository.findIdx(Neo4jTestEntity.title, titleString) match {
        case Some(entity) =>
          "Not Empty"
        case None =>
          ""
      }

      "" must_==(deletedEntity + deletedEntityByTitle)
    }

    "remove related entities properly" in {

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
      repositoryWithRel.delete(entityWithRel)

      true must_==(true)
    }

    "find entities by index IN query" in {

      val entity = repository.create
      entity.title.set("IndexTest 1")
      repository.update(entity)

      val entity2 = repository.create
      entity2.title.set("ISO-8859-1")
      repository.update(entity2)

      val entity3 = repository.create
      entity3.title.set("IndexTest 3")
      repository.update(entity3)

      val count = repository.findIdxAll(Neo4jTestEntity.title, List("IndexTest 1", "ISO-8859-1")).size

      repository.findIdxAll(Neo4jTestEntity.title, List("IndexTest 1", "ISO-8859-1", "IndexTest 3")) foreach {
        entity => repository.delete(entity)
      }

      count must_==2
    }

    "find entities by exact index properly" in {

      val entity = repositoryWithExactIndex.create
      entity.code.set("UTF-8")
      repositoryWithExactIndex.update(entity)

      val entity2 = repositoryWithExactIndex.create
      entity2.code.set("ISO-8859-1")
      repositoryWithExactIndex.update(entity2)

      val code = repositoryWithExactIndex.findIdx(Neo4jTestEntityExact.code, "UTF-8") match {
        case Some(entity) => entity.code.is
        case None => ""
      }

      repositoryWithExactIndex.findIdxAll(Neo4jTestEntityExact.code, List("UTF-8", "ISO-8859-1")) foreach {
        entity => repositoryWithExactIndex.delete(entity)
      }

      code must_==("UTF-8")
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

    "load optional related entities properly" in {

      val entity = repository.create
      entity.title.set("Test Title (i'll be related, target)")
      repository.update(entity)

      val entityWithOptRel = repositoryWithOptRel.create
      entityWithOptRel.optionalRelatedToOne.set(Some(entity))
      repositoryWithOptRel.update(entityWithOptRel)

      val result = repositoryWithOptRel.findIdx(Neo4jTestEntityWithOptionalRelation.uuid, entityWithOptRel.uuid.is.toString) match {
        case Some(loadedEntity) =>
          loadedEntity.optionalRelatedToOne.is.get.uuid.is.toString
        case None =>
          ""
      }
      result must_==(entity.uuid.is.toString)
    }

    "reset optional related entities properly" in {

      val entity = repository.create
      entity.title.set("Test Title (i'll be related, target)")
      repository.update(entity)

      val entityWithOptRel = repositoryWithOptRel.create
      entityWithOptRel.optionalRelatedToOne.set(Some(entity))
      repositoryWithOptRel.update(entityWithOptRel)

      entityWithOptRel.optionalRelatedToOne.clear
      repositoryWithOptRel.update(entityWithOptRel)

      val result = repositoryWithOptRel.findIdx(Neo4jTestEntityWithOptionalRelation.uuid, entityWithOptRel.uuid.is.toString) match {
        case Some(entity) =>
          if (entity.optionalRelatedToOne.is == None) {
            ""
          } else {
            "FAIL"
          }
        case None =>
          "FAILED TO LOAD ENTITY"
      }
      result must_==("")
    }

    "save and load many related entities properly" in {

      val entity = repository.create
      entity.title.set("Test Many (i'll be many related, target 1)")
      repository.update(entity)

      val entity2 = repository.create
      entity2.title.set("Test Many (i'll be many related, target 2)")
      repository.update(entity2)

      val entityWithManyRel = repositoryWithManyRel.create
      entityWithManyRel.relationSet.put(entity)
      entityWithManyRel.relationSet.put(entity2)
      repositoryWithManyRel.update(entityWithManyRel)

      val result = repositoryWithManyRel.findIdx(Neo4jTestEntityWithManyRelations.uuid, entityWithManyRel.uuid.is.toString) match {
        case Some(loadedEntity) =>
          loadedEntity.relationSet.is.size
        case None =>
          -1
      }
      result must_==(2)
    }

    "remove many related entities properly" in {

      val entity = repository.create
      entity.title.set("Test Many (i'll be many related, target 1)")
      repository.update(entity)

      val entity2 = repository.create
      entity2.title.set("Test Many (i'll be many related, target 2)")
      repository.update(entity2)

      val entityWithManyRel = repositoryWithManyRel.create
      entityWithManyRel.relationSet.put(entity)
      entityWithManyRel.relationSet.put(entity2)
      repositoryWithManyRel.update(entityWithManyRel)

      entityWithManyRel.relationSet.remove(entity)
      repositoryWithManyRel.update(entityWithManyRel)

      val result = repositoryWithManyRel.findIdx(Neo4jTestEntityWithManyRelations.uuid, entityWithManyRel.uuid.is.toString) match {
        case Some(loadedEntity) =>
          loadedEntity.relationSet.is.size
        case None =>
          -1
      }
      result must_==(1)
    }

    "shutdown properly" in {

      configuration.egdsp.ds.graphDatabase.shutdown()

      true must_==true
    }

  }
}
