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

import entity.GraphEntity
import net.caladesiframework.orientdb.repository.CRUDRepository
import repository.{GraphRepository}
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import com.orientechnologies.orient.core.record.impl.ODocument
import net.caladesiframework.orientdb.field._
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util

abstract class OrientGraphRepository[EntityType <: GraphEntity] (implicit m:scala.reflect.Manifest[EntityType])
  extends GraphRepository[EntityType] with CRUDRepository[EntityType] {

  // @TODO Inject by configuration
  private val graphDB = new OGraphDatabase("remote:127.0.0.1/db")
  private val userName = "admin"
  private val password = "admin"

  implicit def dbWrapper(db: OGraphDatabase) = new {
    def queryBySql[T](sql: String, params: AnyRef*): List[T] = {
      val params4Java = params.toArray
      val result: java.util.List[T] = db.query(new OSQLSynchQuery(sql), params4Java:_*)
      result.asScala.toList
    }
  }

  // Override to rename
  def repositoryEntityClass = "OGraphEntity"

  /**
   * Creates the correct VertexType if missing
   */
  def init = {
    graphDB.open(userName, password)

    graphDB.getVertexType(repositoryEntityClass) match {
      case clazz: OClass => // Everything fine, no update needed
      case _ =>
        graphDB.createVertexType(repositoryEntityClass, "OGraphVertex")
    }

    graphDB.close()
  }

  def findByUuid(id: util.UUID): EntityType = {
    graphDB.open(userName, password)

    val result = graphDB.queryBySql("SELECT FROM " + repositoryEntityClass + " WHERE _uuid = '" + id.toString + "'")

    if (result.size == 0) {
      throw new Exception("Not found")
    }
    val document : ODocument = result.head
    graphDB.close()

    val entity = this.create
    setEntityFields(entity, document)

    entity
  }

  /**
   * Creates a new entity (not persisted)
   *
   * @return
   */
  def create : EntityType = {
    m.erasure.newInstance().asInstanceOf[EntityType]
  }

  /**
   * Saves a new entity to db or updates if already saved
   *
   * @param entity
   * @return
   */
  def update(entity: EntityType) = {
    graphDB.open(userName, password)

    // Check for right VertexType present
    graphDB.getVertexType(repositoryEntityClass) match {
      case null => throw new Exception("Please run repository initialization before updating entities")
      case clazz: OClass => // Everything is fine
    }

    val vertex = graphDB.createVertex(repositoryEntityClass)
    setVertexFields(vertex, entity)
    vertex.save

    entity.assignInternalId(vertex.getRecord.getIdentity.toString)
    graphDB.close()

    entity
  }

  /**
   * Saves all given entities or updates the if already present
   *
   * @param list
   * @return
   */
  def update(list: List[EntityType]) = {
    open
    graphDB.declareIntent(new OIntentMassiveInsert())

    // Check for right VertexType present
    graphDB.getVertexType(repositoryEntityClass) match {
      case null => throw new Exception("Please run repository initialization before updating entities")
      case clazz: OClass => // Everything is fine
    }

    val vertex = graphDB.createVertex(repositoryEntityClass)
    list foreach {
      entity => {
        vertex.reset
        vertex.field(OGraphDatabase.LABEL, repositoryEntityClass)
        setVertexFields(vertex, entity)

        vertex.save
        entity.assignInternalId(vertex.getRecord.getIdentity.toString)
      }
    }

    graphDB.declareIntent(null)
    close

    list
  }

  /**
   * Removes entity from db
   *
   * @param entity
   * @return
   */
  def delete(entity: EntityType) = {
    throw new Exception("Not implemented yet")
  }

  /**
   * Returns the overall count of the entities in this repository
   *
   * @return
   */
  def count = {
    var count : Long = 0

    graphDB.open(userName, password)

    val result = graphDB.queryBySql[Long]("SELECT COUNT(*) FROM " + repositoryEntityClass)
    count = result.head.toString.replace("}", "").replace("{", "").split(":").last.toLong

    graphDB.close()
    count
  }

  def drop = {
    throw new Exception("Not implemented yet")
  }

  /**
   * Open db
   *
   * @return
   */
  private def open() = {
    graphDB.open(userName, password)
  }

  /**
   * Close DB connection
   */
  private def close() = {
    graphDB.close()
  }

  /**
   * Copy fields to vertex
   *
   * @param vertex
   * @param entity
   */
  private def setVertexFields(vertex: ODocument, entity: EntityType) = {
    entity.fields foreach {
      fieldObj => {
        //@TODO More generic approach
        fieldObj match {
          case field: IntField =>
            vertex.field(field.name, field.is)
          case field: StringField =>
            vertex.field(field.name, field.is)
          case field: DoubleField =>
            vertex.field(field.name, field.is)
          case field: UuidField =>
            vertex.field(field.name, field.is.toString)
          case _ =>
            throw new Exception("Not supported Field")
        }
      }
    }
  }

  /**
   * Copy vertex fields to entity
   *
   * @param entity
   * @param vertex
   */
  private def setEntityFields(entity: EntityType, vertex: ODocument) = {
    entity.fields foreach {
      fieldObj => {
        fieldObj match {
          case field: IntField =>
            field.set(vertex.field(field.name))
          case field: StringField =>
            field.set(vertex.field(field.name))
          case field: DoubleField =>
            field.set(vertex.field(field.name))
          case field: UuidField =>
            field.set(util.UUID.fromString(vertex.field(field.name)))
          case _ =>
            throw new Exception("Not supported Field")
        }
      }
    }
    // Set id
    entity.assignInternalId(vertex.getIdentity.toString)
  }

}
