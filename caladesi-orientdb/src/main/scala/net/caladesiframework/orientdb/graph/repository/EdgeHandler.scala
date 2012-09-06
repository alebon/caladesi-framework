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

package net.caladesiframework.orientdb.graph.repository

import net.caladesiframework.orientdb.graph.entity.OrientGraphEntity
import com.orientechnologies.orient.core.db.graph.OGraphDatabase
import net.caladesiframework.orientdb.relation.{RelatedToMany, OptionalRelatedToOne, Relation, RelatedToOne}
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.record.impl.ODocument

import scala.collection.JavaConverters._
import java.util
import net.caladesiframework.orientdb.field.Field
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.command.traverse.OTraverse
import com.orientechnologies.orient.core.command.{OCommandContext, OCommandPredicate}
import com.orientechnologies.orient.core.record.ORecord
import net.caladesiframework.orientdb.repository.RepositoryRegistry

trait EdgeHandler {

  /**
   * Checks for edge existence and creates one if this edge type is missing
   *
   * @param field
   * @param db
   * @return
   */
  protected def checkEdgeType(field: RelatedToOne[OrientGraphEntity])(implicit db: OGraphDatabase) = {
    val name = edgeName(field.asInstanceOf[Field[AnyRef] with Relation])
    db.getEdgeType(name) match {
      case oClazz: OClass => // Everything is fine
      case _ =>
        db.createEdgeType(name)
    }

  }

  /**
   * Convention method to create edge names
   *
   * @param field
   * @return
   */
  private def edgeName(field: Field[AnyRef] with Relation) = {
    field.owner.clazz + "_" + field.relType + "_" + field.name + "_EDGE"
  }

  /**
   * Single relation handler method (Check for only one, create if not existent)
   *
   * @param vertex
   * @param field
   * @param db
   * @tparam RelatedEntityType
   * @return
   */
  protected def handleRelation[RelatedEntityType <: OrientGraphEntity](vertex: ODocument,
    field: Relation)(implicit db: OGraphDatabase) = {

    field match {
      case fld: RelatedToOne[RelatedEntityType] =>
        handleRelatedToOne(vertex, fld)
      case fld: OptionalRelatedToOne[RelatedEntityType] =>
        handleOptionalRelatedToOne(vertex, fld)
      case fld: RelatedToMany[RelatedEntityType] =>
      case _ =>
        throw new Exception("Can't handle this relation type")
    }
  }

  /**
   * 1..1 Relation
   *
   * Saves the relation if the target node is existent, relationship class is created and no other relation
   * is already created. Doesn't remove the relation!
   *
   * @param vertex
   * @param field
   * @param db
   * @tparam RelatedEntityType
   * @return
   */
  protected def handleRelatedToOne[RelatedEntityType <: OrientGraphEntity](vertex: ODocument,
    field: RelatedToOne[RelatedEntityType])(implicit db: OGraphDatabase) = {

    field.is.hasInternalId() match {
      case true =>

        val edges = db.getEdgesBetweenVertexes(vertex, field.is.getUnderlyingVertex)

        val relationshipName = edgeName(field.asInstanceOf[Field[AnyRef] with Relation])
        edges.asScala foreach  {
          entry => {
            entry match {
              case oDoc: ODocument if (oDoc.getClassName == relationshipName) =>
                db.removeEdge(oDoc)
              case _ => // Skip
            }
          }
        }

        // Create relationship here
        val edge = db.createEdge(vertex, field.is.getUnderlyingVertex, relationshipName)
        edge.save

      case _ => throw new Exception("Please update the related entity first")
    }

  }

  /**
   * 0..1 Relation
   *
   * Saves the relation if the target node is existent, relationship class is created and no other relation
   * is already created. Removes all relations of this type if target field value is null (implicit un-assign command)
   *
   * @param vertex
   * @param field
   * @param db
   * @tparam RelatedEntityType
   * @return
   */
  protected def handleOptionalRelatedToOne[RelatedEntityType <: OrientGraphEntity](vertex: ODocument,
    field: OptionalRelatedToOne[RelatedEntityType])(implicit db: OGraphDatabase) = {

    val edgeClassName = edgeName(field.asInstanceOf[Field[AnyRef] with Relation])
    val fieldWellFormed = field.asInstanceOf[OrientGraphEntity]

    val edges = getEdgesBetweenVerticesByName(vertex,
      fieldWellFormed.getUnderlyingVertex, edgeClassName)

    if (edges.size > 0) {
      if (field.markedToBeRemoved) {
        // Remove the edge
        edges foreach { edge => { db.removeEdge(edge) } }
      }

      // This case means, there are edges present and they should be kept
    } else {
      //Create the edge
      val edge = db.createEdge(vertex, field.is.getUnderlyingVertex, edgeClassName)
      edge.save
    }
  }

  /**
   * Loads target vertex for RelatedToOne relations
   *
   * @param field
   * @param vertex
   * @param db
   * @return
   */
  protected def loadRelation(field: Field[AnyRef] with Relation,
                              vertex: ODocument, depth: Int = 0)(implicit db: OGraphDatabase) = {

    val traversal = new OTraverse().field("out")
      .target(vertex)
      .predicate(new SingleRelationCommand)

    val result = traversal.execute()
    for (identifiable: OIdentifiable <- result.asScala) {
      identifiable.asInstanceOf[ODocument].getSchemaClass.toString match {
        case s:String if (s.equals(edgeName(field))) =>
          // Matched the related edge (sourceVertex):out--out:[Edge]:in-->in:(targetVertex)
          val targetVertex: ODocument = identifiable.asInstanceOf[ODocument].field("in")
          val targetRepo =  RepositoryRegistry.get(field.defaultValue.asInstanceOf[OrientGraphEntity].clazz)
          field.set(targetRepo.createFromVertex(targetVertex, depth))
        case _ =>
          // Ignore this document
      }
    }
  }

  /**
   * Returns all edges between two vertices by name
   *
   * @param source
   * @param target
   * @param name
   * @param db
   * @return
   */
  private def getEdgesBetweenVerticesByName(source: ODocument, target: ODocument,
    name: String)(implicit db: OGraphDatabase): Iterable[OIdentifiable] = {

    val labels = new util.ArrayList[String]()
    val relation = new util.ArrayList[String]()
    relation.add(name)

    db.getEdgesBetweenVertexes(source, target,
      labels.asInstanceOf[Array[String]], relation.asInstanceOf[Array[String]]).asScala

    /**val allEdges = db.getEdgesBetweenVertexes(source, target).asScala
    allEdges foreach {
      edgeName => {

      }
    } */
  }
}

sealed class SingleRelationCommand extends OCommandPredicate {
  def evaluate(oRecord: ORecord[_], iContext: OCommandContext): Boolean = {
    return ((iContext.getVariable("depth").asInstanceOf[Int]) <= 1);
  }
}
