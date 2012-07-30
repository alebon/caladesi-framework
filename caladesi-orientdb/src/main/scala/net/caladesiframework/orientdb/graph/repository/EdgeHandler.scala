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
import net.caladesiframework.orientdb.relation.RelatedToOne
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.record.impl.ODocument

import scala.collection.JavaConverters._

trait EdgeHandler {

  /**
   * Checks for edge existence and creates one if this edge type is missing
   *
   * @param field
   * @param db
   * @return
   */
  protected def checkEdgeType(field: RelatedToOne[OrientGraphEntity])(implicit db: OGraphDatabase) = {
    val name = edgeName(field)
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
  private def edgeName(field: RelatedToOne[OrientGraphEntity]) = {
    field.relType + "_E"
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
  protected def handleRelatedToOne[RelatedEntityType <: OrientGraphEntity](vertex: ODocument,
    field: RelatedToOne[RelatedEntityType])(implicit db: OGraphDatabase) = {

    field.is.hasInternalId() match {
      case true =>

        val edges = db.getEdgesBetweenVertexes(vertex, field.is.getUnderlyingVertex)
        var existent = false
        edges.asScala foreach  {
          entry => {
            entry match {
              case oDoc: ODocument if (oDoc.getClassName == edgeName(field.asInstanceOf[RelatedToOne[OrientGraphEntity]])) =>
                existent = true
              case _ =>
                println(entry.toString)
            }
          }
        }

        if (!existent) {
          // Create relationship here
          val edge = db.createEdge(vertex, field.is.getUnderlyingVertex,
                                   edgeName(field.asInstanceOf[RelatedToOne[OrientGraphEntity]]))
          edge.save
        }

      case _ => throw new Exception("Please update the related entity first")
    }
  }

}
