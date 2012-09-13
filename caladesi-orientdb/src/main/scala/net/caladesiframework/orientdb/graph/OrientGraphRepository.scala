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

import entity.OrientGraphEntity
import net.caladesiframework.orientdb.repository.{RepositoryRegistry, CRUDRepository}
import repository.{EdgeHandler, GraphRepository}
import com.orientechnologies.orient.core.db.graph.{OGraphDatabasePool, OGraphDatabase}
import com.orientechnologies.orient.core.record.impl.ODocument
import net.caladesiframework.orientdb.field._
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert
import com.orientechnologies.orient.core.metadata.schema.OClass
import scala.collection.JavaConversions._
import java.util
import util.Locale
import net.caladesiframework.orientdb.query.{IndexQueryBuilder, QueryBuilder}
import net.caladesiframework.orientdb.relation.{Relation, RelatedToOne}
import com.orientechnologies.orient.core.tx.OTransaction
import net.caladesiframework.orientdb.index.{IndexedField, IndexManager}
import net.caladesiframework.orientdb.db.OrientConfiguration

abstract class OrientGraphRepository[EntityType <: OrientGraphEntity]
  (implicit m:scala.reflect.Manifest[EntityType], configuration: OrientConfiguration)
  extends GraphRepository[EntityType]
  with CRUDRepository[EntityType]
  with OrientGraphDbWrapper
  with EdgeHandler
  with IndexManager {

  // Override to rename
  def repositoryEntityClass = create.clazz

  /**
   * Creates the correct VertexType if missing
   */
  def init = {
    connected(implicit db => {
      // Check for Vertices
      db.getVertexType(repositoryEntityClass) match {
        case clazz: OClass => // Everything fine, no update needed
        case _ =>
          db.createVertexType(repositoryEntityClass, "OGraphVertex")
      }

      create.fields foreach {
        fieldObj => {
          // Check for missing edge types
          fieldObj match {
            case field:RelatedToOne[OrientGraphEntity] =>
              checkEdgeType(field)
            case _ => // Ignore it
          }

          // Check for missing indexes
          fieldObj match {
            case field: Field[_] with IndexedField =>
              checkFieldIndex(field)
            case _ => // Ignore
          }

        }
      }
    })

    RepositoryRegistry.register(this)
  }

  /**
   * Finds entities by constructed query
   */
  def find : QueryBuilder = {
    new QueryBuilder(create, this)
  }

  /**
   * Finds entities by query to index
   * @return
   */
  def findIdx: IndexQueryBuilder = {
    new IndexQueryBuilder(create, this)
  }

  /**
   * Executes a string query (drop any custom query in here)
   *
   * @param qry
   * @return
   */
  def execute(qry: String, params: AnyRef*): List[EntityType] = connected(implicit db => {

    val result = db.queryBySql(qry, params:_*)

    var list : List[EntityType] = Nil
    for (vertex: ODocument <- result) {
      list = transformToEntity(vertex, 1) :: list
    }

    list
  })

  /**
   * Creates a fresh node and assigns the vertex data to it
   *
   * @param vertex
   * @return
   */
  def transformToEntity(vertex: ODocument, depth: Int = 0)(implicit db: OGraphDatabase) : EntityType = {
    // Check if its a index query result
    if (vertex.field("rid") != null) {
      return setEntityFields(create, vertex.field("rid"), depth)
    }
    return setEntityFields(create, vertex, depth)
  }

  /**
   * Creates a new entity (not persisted)
   *
   * @return
   */
  def create : EntityType = {
    m.erasure.newInstance().asInstanceOf[EntityType]
  }

  def createFromVertex(vertex: ODocument, depth: Int = 0)(implicit db: OGraphDatabase): EntityType = {
    transformToEntity(vertex, depth)
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

        var existing = false
        // Decide by internal id: create or update
        val vertex: ODocument =  entity.hasInternalId() match {
          case true => //db.load[ODocument](entity.getUnderlyingVertex.getIdentity)
            //println("before: " + entity.getUnderlyingVertex.getIdentity.toString())
            existing = true
            entity.getUnderlyingVertex
            //db.load[ODocument](entity.getUnderlyingVertex.getIdentity)

          case false => db.createVertex(repositoryEntityClass)
        }

        // Set the new fields and save
        setVertexFields(vertex, entity)
        if (existing) {
          val check = db.load[ODocument](entity.getUnderlyingVertex.getIdentity)
          //println("after: " + entity.getUnderlyingVertex.getIdentity.toString())

          if (check.getVersion == vertex.getVersion) {
            vertex.save
          } else {
            vertex.reload()
            vertex.save()
          }

        } else {
          vertex.save
        }

        vertex
      })
      entity.setUnderlyingVertex(vertex)

      connected(implicit db => {
        updateIndex(entity)
        entity.getUnderlyingVertex.reload()
      })

      entity
  }

  /**
   * Saves all given entities or updates them if already present
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
        updateIndex(entity)
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
    updateIndex(entity, reIndex = false)
    db.removeVertex(entity.getUnderlyingVertex)
    true
  })

  /**
   * Returns the overall count of the entities in this repository
   *
   * @return
   */
  def count = connected(implicit db => {
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
    } else {
      connected(implicit dbName => {
        dropIndex(create)
      })
    }

  }

  /**
   * Copy fields to vertex
   *
   * @param vertex
   * @param entity
   */
  protected def setVertexFields(vertex: ODocument, entity: EntityType)(implicit db: OGraphDatabase) = {
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
          case field:Relation =>
            handleRelation(vertex, field)
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
  protected def setEntityFields(entity: EntityType, vertex: ODocument, depth: Int = 0)(implicit db: OGraphDatabase) = {
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
            if (vertex.field(field.name) == null) {
              throw new Exception(vertex.toJSON)
            }
            field.set(util.UUID.fromString(vertex.field(field.name)))
          case field: LongField =>
            field.set(vertex.field(field.name))
          case field: LocaleField =>
            field.set(new Locale(vertex.field(field.name)))
          case field: DateTimeField =>
            field.valueFromDB(vertex.field(field.name))
          case field:RelatedToOne[OrientGraphEntity] =>
            if (depth > 0) {
              // Traverse the relations
              loadRelation(field.asInstanceOf[Field[AnyRef] with Relation], vertex, depth - 1)
            }
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
      .acquire(configuration.databaseType + ":" + configuration.host.server.location + "/" + configuration.database,
      configuration.user, configuration.password)

    try {
      val transaction = synchronized { db.begin(OTransaction.TXTYPE.OPTIMISTIC) }
      val ret = f(db)
      transaction.commit()

      return ret
    } catch {
      case e:Exception =>
        db.rollback()
        throw new Exception("Failure during execution: " + e.getMessage + " STACKTRACE: " + e.getStackTraceString)
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
      .acquire(configuration.databaseType + ":" + configuration.host.server.location + "/" + configuration.database,
        configuration.user, configuration.password)

    try {
      val ret = f(db)
      return ret
    } catch {
      case e:Exception =>
        throw new Exception("Failure during execution in connected mode: " + e.getMessage + " - Stacktrace:" + e.getStackTraceString)
    } finally {
      db.close()
    }
  }
}
