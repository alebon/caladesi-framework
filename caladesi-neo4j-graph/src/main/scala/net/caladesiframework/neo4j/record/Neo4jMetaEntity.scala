/*
 * Copyright 2014 Caladesi Framework
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
package net.caladesiframework.neo4j.record

import net.caladesiframework.neo4j.db.{Neo4jConfigurationRegistry, Neo4jDatabaseService}
import net.caladesiframework.field._
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.impl.util.StringLogger
import org.neo4j.graphdb.Node

trait Neo4jMetaEntity[RecordType] extends Neo4jEntity[RecordType] {
  self: RecordType =>

  def databaseIdentifier: String = "default"

  def label: String = this.getClass.getSuperclass.getSimpleName

  protected lazy val dbConfiguration = Neo4jConfigurationRegistry.loadByIdentifier(databaseIdentifier)

  private [this] val fieldMap: scala.collection.mutable.Map[String, Field[_, RecordType]]
    = new scala.collection.mutable.HashMap[String, Field[_, RecordType]]()

  def fields = this.fieldMap.toSeq

  protected lazy val executionEngine = this.getExecutionEngine()

  /**
   * Reuse protected val executionEngine
   *
   * @return
   */
  protected def getExecutionEngine() : ExecutionEngine = {
    return new ExecutionEngine(this.dbConfiguration.egdsp.ds.graphDatabase, StringLogger.DEV_NULL)
  }

  def createRecord: RecordType = {
    val record = this.getClass.getSuperclass.newInstance().asInstanceOf[RecordType]

    // Avoid iterating all fields
    //if (!initComplete) {
    initFields(record)
    //  initComplete = true
    //}

    record
  }

  /**
   * Creates a fresh node and assigns the node data to it
   *
   * @param node
   * @return
   */
  def createEntityFromNode(node: Node, depth: Int = 1)(implicit ds: Neo4jDatabaseService) : RecordType = {
    return setEntityFields(create, node, depth)
  }

  protected def setEntityFields(entity: RecordType, node: Node, depth: Int = 1): RecordType = {

    this.fields foreach {fieldEntry => {
      if (node.hasProperty(fieldEntry._2.name)) {

        // Shortcuts
        def fieldObj = fieldEntry._2
        def nodeProp = node.getProperty(fieldObj.name)

        // Match field and set proper value
        fieldObj match {
          // STRING
          case field: StringField[_] =>
            field.asInstanceOf[StringField[RecordType]].set(nodeProp.asInstanceOf[String])
          case field: OptionalStringField[_] =>
            field.asInstanceOf[OptionalStringField[RecordType]].set(nodeProp.asInstanceOf[String])

          // INT
          case field: IntField[_] =>
            field.asInstanceOf[IntField[_]].set(nodeProp.asInstanceOf[Int])
          case field: OptionalIntField[_] =>
            field.asInstanceOf[OptionalIntField[RecordType]].set(nodeProp.asInstanceOf[Int])

          // LONG
          case field: LongField[_] =>
            field.asInstanceOf[LongField[_]].set(nodeProp.asInstanceOf[Long])
          case field: OptionalLongField[_] =>
            field.asInstanceOf[OptionalLongField[_]].set(nodeProp.asInstanceOf[Long])

          // BOOLEAN
          case field: BooleanField[_] =>
            field.asInstanceOf[BooleanField[_]].set(nodeProp.asInstanceOf[Boolean])
          case field: OptionalBooleanField[_] =>
            field.asInstanceOf[OptionalBooleanField[_]].set(nodeProp.asInstanceOf[Boolean])

          // ERROR
          case _ =>
            throw new Exception("Unknown field type")
        }
      } else {
        // No value and optional field -> Reset optional field
        // Value and not optional -> throw exception
        fieldEntry._2 match {
          case field: OptionalField[_,_] =>
            field.reset
          case field: RequiredField[_,_] =>
            throw new Exception("Required value for '%s' is not defined on node".format(fieldEntry._2.name))
        }
      }
    }}

    entity
  }

  private def initFields(record: Any) = {
    record.getClass.getDeclaredFields foreach {
      field => {
        field.getType.getMethods foreach {

          // If field contains a method for initialization, call it and add field to meta fields
          method => {
            if  (method.getName.equals("initField")) {
              field.setAccessible(true)
              val m = record.getClass.getMethod(field.getName.replace("$module", ""))

              val fieldObj: Field[_, RecordType] = m.invoke(record).asInstanceOf[Field[_, RecordType]]

              // Apply default naming
              if (fieldObj.name == null) {
                fieldObj.applyName(field.getName.replace("$module", ""))
              }

              fieldObj.initField
              attach(fieldObj.name, fieldObj)
            }
          }
        }

      }
    }
  }

  /**
   * Returns the overall count of records for this collection
   *
   * @return
   */
  def count: Long = inSyncTrx(implicit db => {
    val engine = this.executionEngine
    val result: org.neo4j.cypher.ExecutionResult = engine.execute( "MATCH entity:%s RETURN count(entity) AS countAll"
      .format(label) )

    if (result.hasNext) {
      val row = result.next()
      return row.get("countAll").get.asInstanceOf[Long]
    }

    0L
  })

  /**
   * Adds the field to the meta fields
   *
   * @param field
   * @return
   */
  private def attach(key: String, field: Field[_, RecordType]) = {
    if (this.fieldMap.get(key).isEmpty) {
      this.fieldMap.put(key, field)
    }
  }

  /**
   * Opens the db, performs execution
   *
   * @param f
   * @tparam T
   * @return
   */
  def inSyncTrx[T <: Any](f: Neo4jDatabaseService => T) : T = {

    val transaction = synchronized { dbConfiguration.egdsp.ds.graphDatabase.beginTx() }

    try {
      val ret = f(dbConfiguration.egdsp.ds)
      transaction.success
      return ret
    } catch {
      case e:Exception =>
        transaction.failure
        throw new Exception("Failure during inSyncTrx execution (%s) : %s - STACKTRACE: %s".format(e.getClass, e.getMessage, e.getStackTraceString))
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
  def inTrx[T <: Any](f: Neo4jDatabaseService => T) : T = {

    val transaction = dbConfiguration.egdsp.ds.graphDatabase.beginTx()

    try {
      val ret = f(dbConfiguration.egdsp.ds)
      transaction.success
      return ret
    } catch {
      case e:Exception =>
        transaction.failure
        throw new Exception("Failure during inTrx execution (%s) : %s - STACKTRACE: %s".format(e.getClass, e.getMessage, e.getStackTraceString))
    } finally {
      transaction.close()
    }
  }

}
