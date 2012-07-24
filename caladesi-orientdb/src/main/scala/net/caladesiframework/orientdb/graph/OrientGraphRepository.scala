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

import entity.{OrientGraphEntity, GraphEntity}
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
import net.caladesiframework.orientdb.document.entity.Document
import util.Locale

abstract class OrientGraphRepository[EntityType <: OrientGraphEntity] (implicit m:scala.reflect.Manifest[EntityType])
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
  def init = connected(implicit db => {
    db.getVertexType(repositoryEntityClass) match {
      case clazz: OClass => // Everything fine, no update needed
      case _ =>
        db.createVertexType(repositoryEntityClass, "OGraphVertex")
    }
  })

  /**
   * Finds entities by given field and value
   *
   * TODO: Replace "transactional" with "connected"
   *
   * @param field
   * @param value
   * @tparam FieldType
   */
  def find[FieldType](field: Field[FieldType], value: FieldType) : List[EntityType] = connected(implicit db => {
    val qry = "SELECT FROM " + repositoryEntityClass + " WHERE " + field.name + " = '" + value.toString + "'"
    println(qry)
    val result = db.queryBySql(qry)

    var list : List[EntityType] = Nil
    for (vertex: ODocument <- result) {
      list = transformToEntity(vertex) :: list
    }

    list
  })

  /**
   * Creates a fresh node and assigns the vertex data to it
   *
   * @param vertex
   * @return
   */
  private def transformToEntity(vertex: ODocument) : EntityType = {
    return setEntityFields(create, vertex)
  }

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
        val vertex: ODocument =  entity.hasInternalId() match {
          case true => entity.getUnderlyingVertex
          case false => db.createVertex(repositoryEntityClass)
        }

        // Set the new fields and save
        setVertexFields(vertex, entity)
        vertex.save

        vertex
      })
      entity.setUnderlyingVertex(vertex)

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
        entity.setUnderlyingVertex(vertex)
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

    if (!entity.hasInternalId()) {
      throw new Exception("Not found vertex with given uuid")
    }
    db.removeVertex(entity.getUnderlyingVertex)
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

  /**
   * Drops all entities in the repository (use with care)
   */
  def drop : Unit = {
    transactional(implicit db => {

      val documents : util.List[ODocument] = db.queryBySql("SELECT FROM " + repositoryEntityClass + " LIMIT 100")
      documents foreach {
        doc => {
          db.removeVertex(doc)
        }
      }

    })

    if (count >= 1) {
      this.drop
    }
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
          case field: LongField =>
            vertex.field(field.name, field.is)
          case field:LocaleField =>
            vertex.field(field.name, field.is.toString)
          case field:DateTimeField =>
            vertex.field(field.name, field.valueToDB)
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
          case field: LongField =>
            field.set(vertex.field(field.name))
          case field: LocaleField =>
            field.set(new Locale(vertex.field(field.name)))
          case field: DateTimeField =>
            field.valueFromDB(vertex.field(field.name))
          case _ =>
            throw new Exception("Not supported Field")
        }
      }
    }
    // Combine both
    entity.setUnderlyingVertex(vertex)
    entity
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

  /**
   * Opens the db, performs execution and closes connection
   *
   * @param f
   * @tparam T
   * @return
   */
  def connected[T <: Any](f: OGraphDatabase => T) : T = {
    val db = OGraphDatabasePool.global()
      .acquire(dbType + ":127.0.0.1/" + dbName, dbUser, dbPassword)

    try {
      val ret = f(db)
      return ret
    } catch {
      case e:Exception =>
        throw new Exception("Failure during execution in connected mode: " + e.getMessage)
    } finally {
      db.close()
    }
  }
}
