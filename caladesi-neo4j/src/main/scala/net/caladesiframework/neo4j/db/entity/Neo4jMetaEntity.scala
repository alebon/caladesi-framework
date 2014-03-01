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
package net.caladesiframework.neo4j.db.entity

import net.caladesiframework.field._
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.impl.util.StringLogger
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import net.caladesiframework.neo4j.db.config.{Neo4jDatabaseService, Neo4jConfigurationRegistry}
import net.caladesiframework.neo4j.db.graph.TypedGraphDatabase

trait Neo4jMetaEntity[EntityType]
  extends Neo4jEntity[EntityType]
  with TypedGraphDatabase {
  self: EntityType =>

  def databaseIdentifier: String = "default"

  def label: String = this.getClass.getSuperclass.getSimpleName

  // Fulfill TypedGraphDatabase requirements
  override def graphDatabaseService: GraphDatabaseService = dbConfiguration.egdsp.ds.graphDatabase

  protected lazy val dbConfiguration = Neo4jConfigurationRegistry.loadByIdentifier(databaseIdentifier)

  private [this] val fieldMap: scala.collection.mutable.Map[String, Field[_, EntityType]]
    = new scala.collection.mutable.HashMap[String, Field[_, EntityType]]()

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

  def createRecord: EntityType = {
    val entity = this.getClass.getSuperclass.newInstance().asInstanceOf[EntityType]

    // Avoid iterating all fields
    //if (!initComplete) {
    initFields(entity)
    //  initComplete = true
    //}

    entity
  }

  /**
   * Creates a fresh node and assigns the node data to it
   *
   * @param node
   * @return
   */
  def createEntityFromNode(node: Node, depth: Int = 1)(implicit ds: Neo4jDatabaseService) : EntityType = {
    return setEntityFields(create, node, depth)
  }

  protected def setEntityFields(entity: EntityType, node: Node, depth: Int = 1): EntityType = {

    this.fields foreach {fieldEntry => {
      if (node.hasProperty(fieldEntry._2.name)) {

        // Shortcuts
        def fieldObj = fieldEntry._2
        def nodeProp = node.getProperty(fieldObj.name)

        // Match field and set proper value
        fieldObj match {
          // STRING
          case field: StringField[_] =>
            field.asInstanceOf[StringField[EntityType]].set(nodeProp.asInstanceOf[String])
          case field: OptionalStringField[_] =>
            field.asInstanceOf[OptionalStringField[EntityType]].set(nodeProp.asInstanceOf[String])

          // INT
          case field: IntField[_] =>
            field.asInstanceOf[IntField[_]].set(nodeProp.asInstanceOf[Int])
          case field: OptionalIntField[_] =>
            field.asInstanceOf[OptionalIntField[EntityType]].set(nodeProp.asInstanceOf[Int])

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

              val fieldObj: Field[_, EntityType] = m.invoke(record).asInstanceOf[Field[_, EntityType]]

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
   * Adds the field to the meta fields
   *
   * @param field
   * @return
   */
  private def attach(key: String, field: Field[_, EntityType]) = {
    if (this.fieldMap.get(key).isEmpty) {
      this.fieldMap.put(key, field)
    }
  }

}
