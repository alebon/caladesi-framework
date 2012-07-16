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
import com.orientechnologies.orient.core.db.graph.{OGraphDatabasePool, OGraphDatabase}
import com.orientechnologies.orient.core.record.impl.ODocument
import net.caladesiframework.orientdb.field._
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.record.ORecord

abstract class OrientGraphRepository[EntityType <: GraphEntity] (implicit m:scala.reflect.Manifest[EntityType])
  extends GraphRepository[EntityType] with CRUDRepository[EntityType] {

  // @TODO Inject by configuration
  def dbType = "remote"
  def dbUser = "admin"
  def dbName = "db"
  def dbPassword = "admin"

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
  def init = transactional (implicit db => {
    db.getVertexType(repositoryEntityClass) match {
      case clazz: OClass => // Everything fine, no update needed
      case _ =>
        db.createVertexType(repositoryEntityClass, "OGraphVertex")
    }
  })

  /**
   * Finds an entity by given uuid
   *
   * @param id
   * @return
   */
  def findByUuid(id: util.UUID): EntityType = transactional(implicit db => {
    val result = db.queryBySql("SELECT FROM " + repositoryEntityClass + " WHERE _uuid = '" + id.toString + "'")

    if (result.size == 0) {
      throw new Exception("Not found")
    }

    val document : ODocument = result.head

    val entity = this.create
    setEntityFields(entity, document)

    entity
  })

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

      val vertex = transactional[ODocument](implicit db => {

        // Check for right VertexType present
        db.getVertexType(repositoryEntityClass) match {
          case null => throw new Exception("Please run repository initialization before updating entities")
          case clazz: OClass => // Everything is fine
        }

        // Decide by internal id: create or update
        var vertex: ODocument = null
        if (entity.hasInternalId()) {
          vertex = db.queryBySql[ODocument]("SELECT FROM " +
            repositoryEntityClass + " WHERE _uuid = '" + entity.uuid.is.toString + "'").head
        } else {
          vertex = db.createVertex(repositoryEntityClass)
        }

        // Set the new fields and save
        setVertexFields(vertex, entity)
        vertex.save

        vertex
      })
      entity.assignInternalId(vertex.getRecord.getIdentity.toString)

      entity
  }

  /**
   * Saves all given entities or updates the if already present
   *
   * @param list
   * @return
   */
  def update(list: List[EntityType]) = transactional(implicit db => {
    db.declareIntent(new OIntentMassiveInsert())

    // Check for right VertexType present
    db.getVertexType(repositoryEntityClass) match {
      case null => throw new Exception("Please run repository initialization before updating entities")
      case clazz: OClass => // Everything is fine
    }

    var vertex : ODocument = null
    list foreach {
      entity => {
        vertex = db.createVertex(repositoryEntityClass)
        setVertexFields(vertex, entity)

        vertex.save
        entity.assignInternalId(vertex.getRecord.getIdentity.toString)
      }
    }

    db.declareIntent(null)

    list
  })

  /**
   * Removes entity from db
   *
   * @param entity
   * @return
   */
  def delete(entity: EntityType) = transactional[Boolean]( implicit db => {
    val result = db.queryBySql("SELECT FROM " + repositoryEntityClass + " WHERE _uuid = '" + entity.uuid.is.toString + "'")

    if (result.size == 0) {
      throw new Exception("Not found vertex with given uuid")
    }
    db.removeVertex(result.head)
    true
  })

  /**
   * Returns the overall count of the entities in this repository
   *
   * @return
   */
  def count = transactional(implicit db => {
    var count : Long = 0

    val result = db.queryBySql[Long]("SELECT COUNT(*) FROM " + repositoryEntityClass)
    count = result.head.toString.replace("}", "").replace("{", "").split(":").last.toLong

    count
  })

  def drop = {
    throw new Exception("Not implemented yet")
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

  /**
   * Opens the db, performs execution
   *
   * @param f
   * @tparam T
   * @return
   */
  def transactional[T <: Any](f: OGraphDatabase => T) : T = {
    val db = OGraphDatabasePool.global()
      .acquire(dbType + ":127.0.0.1/" + dbName, dbUser, dbPassword)

    try {
      val transaction = synchronized { db.begin() }
      val ret = f(db)
      transaction.commit()

      return ret
    } catch {
      case e:Exception =>
        db.rollback()
        throw new Exception("Failure during execution: " + e.getMessage)
    } finally {
      db.close()
    }
  }
}
