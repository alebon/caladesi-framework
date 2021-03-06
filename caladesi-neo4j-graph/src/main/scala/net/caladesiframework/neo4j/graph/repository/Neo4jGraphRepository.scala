/*
 * Copyright 2013 Caladesi Framework
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

import java.util
import net.caladesiframework.neo4j.index.{IndexManager, IndexedField}
import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import net.caladesiframework.neo4j.db.Neo4jDatabaseService
import net.caladesiframework.neo4j.repository.{RepositoryRegistry, CRUDRepository}
import org.neo4j.graphdb.{NotFoundException, Direction, DynamicRelationshipType, Node}
import net.caladesiframework.neo4j.field._
import net.caladesiframework.neo4j.db.Neo4jConfiguration
import net.caladesiframework.neo4j.relation.{RelationManager, Relation}
import org.neo4j.kernel.impl.util.StringLogger
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.kernel.GraphDatabaseAPI
import org.neo4j.kernel.impl.core.NodeManager

abstract class Neo4jGraphRepository[EntityType <: Neo4jGraphEntity]
  (implicit tag: scala.reflect.ClassTag[EntityType], configuration: Neo4jConfiguration)
  extends GraphRepository[EntityType]
  with CRUDRepository[EntityType]
  with IndexManager
  with RelationManager {

  protected lazy val executionEngine = this.getExecutionEngine()

  // Override to rename
  def repositoryEntityClass = create.clazz

  // subReference Node <-- Entity OR referenceNode <-- subReference
  protected val REPOSITORY_RELATION : DynamicRelationshipType  = DynamicRelationshipType.withName( REPOSITORY_NAME )

  // subReference Node <-- Entity OR referenceNode <-- subReference
  protected val ENTITY_RELATION : DynamicRelationshipType  = DynamicRelationshipType.withName( RELATION_NAME )

  /**
   * Override this to define own relation name
   *
   * @return
   */
  def RELATION_NAME = "ENTITY"

  /**
   * Override this to define own relation name
   *
   * @return
   */
  def REPOSITORY_NAME = RELATION_NAME + "_SUB_REF"

  /**
   * Root Node of the graph
   */
  protected lazy val rootNode: Node = readTx(implicit ds => {
    var rootNode: Node = null
    try {
      rootNode = ds.graphDatabase.getNodeById(0)
    } catch {
      case e: NotFoundException =>
        rootNode = ds.graphDatabase.createNode()
      case t: Throwable =>
        throw t
    }

    rootNode
  })

  /**
   * Sub-Reference node of the repository
   */
  protected lazy val subReferenceNode: Node = {
    val rel = rootNode.getSingleRelationship(REPOSITORY_RELATION, Direction.OUTGOING)

    if (null == rel) {
      throw new Exception("Subreference node for %s repository is missing, please init repository properly".format(REPOSITORY_NAME))
    }

    rel.getEndNode
  }

  /**
   * Creates the correct SubRefs and Index if missing
   */
  def init = {
    writeTx(implicit ds => {

      // Check for Sub Reference Node existence
      val rel = rootNode.getSingleRelationship(REPOSITORY_RELATION, Direction.OUTGOING)
      if (null == rel) {
        // Create the sub ref node
        val node = ds.graphDatabase.createNode()
        rootNode.createRelationshipTo(node, REPOSITORY_RELATION)
      }

      create.fields foreach {
        fieldObj => {
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
   * Creates a fresh node and assigns the node data to it
   *
   * @param node
   * @return
   */
  def transformToEntity(node: Node, depth: Int = 1)(implicit ds: Neo4jDatabaseService) : EntityType = {
    return setEntityFields(create, node, depth)
  }

  /**
   * Creates a new entity (not persisted)
   *
   * @return
   */
  def create : EntityType = {
    tag.runtimeClass.newInstance().asInstanceOf[EntityType]
  }

  def createFromNode(node: Node, depth: Int = 1)(implicit db: Neo4jDatabaseService): EntityType = {
    transformToEntity(node, depth)
  }

  /**
   * Saves a new entity to db or updates if already saved
   *
   * @param entity
   * @return
   */
  def update(entity: EntityType) = {

    writeTx(implicit ds => {
      var node : Node = null
      if (!entity.hasInternalId()) {
        node = ds.graphDatabase.createNode()

        node.createRelationshipTo(subReferenceNode, ENTITY_RELATION)
        entity.setUnderlyingNode(node)
      } else {
        node = entity.getUnderlyingNode
      }

      setNodeFields(node, entity)
      updateIndex(entity)
    })

    entity
  }

  /**
   * Find entity by index entry
   *
   * @param field
   * @param value
   * @return
   */
  def findIdx(field: Field[_] with IndexedField, value: Any): Option[EntityType] = {
    readTx[Option[EntityType]](implicit ds => {
      findSingleByIndex(field, value) match {
        case Some(node) =>
          Some(transformToEntity(node, 1))
        case None =>
          None
      }
    })
  }

  /**
   *
   * @param field
   * @param value
   * @return
   */
  def findIdxAll(field: Field[_] with IndexedField, value: Any): List[EntityType] = {
    readTx[List[EntityType]](implicit ds => {
      findAllByIndex(field, value).map(node => transformToEntity(node))
    })
  }

  /**
   *
   * @param field
   * @param values
   * @return
   */
  def findIdxAll(field: Field[_] with IndexedField, values: List[String]): List[EntityType] = {
    readTx[List[EntityType]](implicit ds => {
      findAllByIndexSet(field, values).map(node => transformToEntity(node))
    })
  }

  /**
   * Get the list
   *
   * @param skip
   * @param limit
   * @return
   */
  def find(skip: Long = 0L, limit : Long = 10): List[EntityType] = readTx(implicit ds => {
    val result: org.neo4j.cypher.ExecutionResult = this.executionEngine.execute( "START n=node(%s) MATCH n<-[:%s]-entity RETURN entity SKIP %s LIMIT %s"
      .format(subReferenceNode.getId, ENTITY_RELATION.name, skip, limit) )

    //println("Executing query: " + "START n=node(%s) MATCH n<-[:%s]-entity RETURN entity SKIP %s LIMIT %s"
    //  .format(subReferenceNode.getId, ENTITY_RELATION.name, skip, limit))

    val nodes = result.columnAs[Node]("entity")

    var list: List[EntityType] = List()
    while (nodes.hasNext) {
      val node = nodes.next()
      list = transformToEntity(node) :: list
    }

    return list
  })

  /**
   * Saves all given entities or updates them if already present
   *
   * @param list
   * @return
   */
  def update(list: List[EntityType]) = writeTx(implicit db => {
    throw new Exception("Operation not supported yet")
  })

  /**
   * Removes entity from db
   *
   * @param entity
   * @return
   */
  def delete(entity: EntityType) = writeTx[Boolean]( implicit ds => {
    def node = entity.getUnderlyingNode

    // DO NOT remove incoming relations, this will break data integrity (user has to check before deletion)
    def relationsIncoming = node.getRelationships(Direction.INCOMING)
    if (relationsIncoming.iterator().hasNext) {
      throw new Exception("Can not delete entity, still has incoming connections")
    }

    // Remove all outgoing relations
    val relationsOutgoing = node.getRelationships(Direction.OUTGOING)
    while (relationsOutgoing.iterator().hasNext) {
      relationsOutgoing.iterator().next().delete()
    }

    removeFromIndex(entity)
    entity.getUnderlyingNode.delete()
    true
  })

  /**
   * Returns the overall count of the entities in this repository
   *
   * @return
   */
  def count: Long = readTx(implicit ds => {
    val engine = this.executionEngine
    val result: org.neo4j.cypher.ExecutionResult = engine.execute( "START n=node(%s) MATCH n<-[:%s]-entity RETURN count(entity) AS countAll"
      .format(subReferenceNode.getId, ENTITY_RELATION.name) )

    //val iterator = result.iterator().asScala

    if (result.hasNext) {
      val row = result.next()
      return row.get("countAll").get.asInstanceOf[Long]
    }

    0
  })

  /**
   * Drops all entities in the repository (use with care)
   */
  def drop : Unit = {
    writeTx(implicit db => {
      // @TODO drop all entities from repository
    })

  }

  /**
   * If possible, don't use this, reuse protected val executionEngine
   *
   * @return
   */
  protected def getExecutionEngine() : ExecutionEngine = {
    return new ExecutionEngine(this.configuration.egdsp.ds.graphDatabase, StringLogger.DEV_NULL)
  }

  /**
   * Copy fields to node
   *
   * @param node
   * @param entity
   */
  protected def setNodeFields(node: Node, entity: EntityType)(implicit ds: Neo4jDatabaseService) = {
    entity.fields foreach {
      fieldObj => {
        fieldObj match {
          case field: IntField =>
            node.setProperty(field.name, field.is.asInstanceOf[java.lang.Integer])
          case field: StringField =>
            node.setProperty(field.name, field.is)
          case field: UuidField =>
            node.setProperty(field.name, field.is.toString)
          case field: LongField =>
            node.setProperty(field.name, field.is.asInstanceOf[java.lang.Long])
          case field:DateTimeField =>
            node.setProperty(field.name, field.valueToDB.asInstanceOf[java.lang.Long])
          case field:BooleanField =>
            node.setProperty(field.name, field.is)
          case field: DoubleField =>
            node.setProperty(field.name, field.is)
          case field: OptionalDateTimeField =>
            field.value match {
              case Some(calendar) => node.setProperty(field.name, field.valueToDB.asInstanceOf[java.lang.Long])
              case None => node.removeProperty(field.name)
            }
          case field:Relation =>
            handleRelation(node, field)
          case _ =>
            throw new Exception("Not supported Field")
        }
      }
    }
  }

  /**
   * Copy node fields to entity
   *
   * @param entity
   * @param node
   */
  protected def setEntityFields(entity: EntityType, node: Node, depth: Int = 0)(implicit ds: Neo4jDatabaseService) = {
    entity.fields foreach {
      fieldObj => {
        fieldObj match {
          case field: IntField =>
            field.set(node.getProperty(field.name).toString.toInt)
          case field: StringField =>
            field.set(node.getProperty(field.name).asInstanceOf[String])
          case field: UuidField =>
            // Strict: uuid is required
            if (!node.hasProperty(field.name)) {
              throw new Exception("Uuid property on node is required")
            }
            field.set(util.UUID.fromString(node.getProperty(field.name).asInstanceOf[String]))
          case field: LongField =>
            field.set(node.getProperty(field.name).toString.toLong)
          case field: DateTimeField =>
            field.valueFromDB(node.getProperty(field.name))
          case field: BooleanField =>
            field.set(node.getProperty(field.name).toString.toBoolean)
          case field: DoubleField =>
            field.set(node.getProperty(field.name).asInstanceOf[Double])
          case field: OptionalDateTimeField =>
            if (node.hasProperty(field.name)) {
              field.valueFromDB(node.getProperty(field.name).asInstanceOf[Long])
            } else {
              field.set(None)
            }
          case field:Relation =>
            if (depth > 0) {
              // Load relation
              loadRelation(field.asInstanceOf[Field[Neo4jGraphEntity] with Relation], node, depth - 1)
            }
          case field: Field[_] =>
            throw new Exception("Not supported Field %s".format(field.name))
        }
      }
    }

    // Combine both
    entity.setUnderlyingNode(node)
    entity
  }

  /**
   * Opens the db, performs execution
   *
   * @param f
   * @tparam T
   * @return
   */
  def readTx[T <: Any](f: Neo4jDatabaseService => T) : T = {

    val transaction = this.configuration.egdsp.ds.graphDatabase.beginTx()

    try {
      val ret = f(this.configuration.egdsp.ds)
      transaction.success
      return ret
    } catch {
      case e:Exception =>
        transaction.failure
        throw new Exception("Failure during execution (%s) : %s - STACKTRACE: %s".format(e.getClass, e.getMessage, e.getStackTraceString))
    } finally {
      transaction.close()
    }
  }

  /**
   * Opens the db, performs execution
   *
   * @param f
   * @tparam T
   * @return
   */
  def writeTx[T <: Any](f: Neo4jDatabaseService => T) : T = {

    val transaction = synchronized {
      val transaction = this.configuration.egdsp.ds.graphDatabase.beginTx()

      // Acquire write LOCK?

      transaction
    }

    try {
      val ret = f(this.configuration.egdsp.ds)
      transaction.success
      return ret
    } catch {
      case e:Exception =>
        transaction.failure
        throw new Exception("Failure during execution (%s) : %s - STACKTRACE: %s".format(e.getClass, e.getMessage, e.getStackTraceString))
    } finally {
      transaction.close()
    }
  }

  /**def connected[T <: Any](f: Neo4jDatabaseService => T) : T = {

    try {
      val ret = f(this.configuration.egdsp.ds)
      return ret
    } catch {
      case e:Exception =>
        throw new Exception("Failure during execution in connected mode: " + e.getMessage + " - Stacktrace:" + e.getStackTraceString)
    } finally {
      // Do something finally
    }
  }*/

}
