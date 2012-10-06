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

package net.caladesiframework.neo4j.relation

import scala.collection.JavaConverters._
import java.util
import net.caladesiframework.neo4j.field.Field
import net.caladesiframework.neo4j.graph.entity.Neo4jGraphEntity
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node}
import net.caladesiframework.neo4j.db.Neo4jDatabaseService
import net.caladesiframework.neo4j.repository.RepositoryRegistry

trait RelationManager {

  /**
   * Convention method to create edge names
   *
   * @param field
   * @return
   */
  private def relationName(field: Field[_] with Relation) = {
    field.relName
  }

  /**
   * Single relation handler method (Check for only one, create if not existent)
   *
   * @param node
   * @param field
   * @param db
   * @tparam RelatedEntityType
   * @return
   */
  protected def handleRelation[RelatedEntityType <: Neo4jGraphEntity](node: Node,
    field: Relation)(implicit db: Neo4jDatabaseService) = {

    field match {
      case fld: RelatedToOne[RelatedEntityType] =>
        handleRelatedToOne(node, fld)
      //case fld: OptionalRelatedToOne[RelatedEntityType] =>
      //  handleOptionalRelatedToOne(vertex, fld)
      //case fld: RelatedToMany[RelatedEntityType] =>
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
   * @param node
   * @param field
   * @param db
   * @tparam RelatedEntityType
   * @return
   */
  protected def handleRelatedToOne[RelatedEntityType <: Neo4jGraphEntity](node: Node,
    field: RelatedToOne[RelatedEntityType])(implicit db: Neo4jDatabaseService) = {
    val relType = DynamicRelationshipType.withName( relationName(field) )

    field.is.hasInternalId() match {
      case true =>
        val relations = node.getRelationships(relType, Direction.OUTGOING)

        // Remove all old relations of this type
        while (relations.iterator().hasNext) {
          val relation = relations.iterator().next()
          relation.delete()
        }

        node.createRelationshipTo(field.is.getUnderlyingNode, relType)



      case _ => throw new Exception("Please update the related entity first")
    }

  }

  /**
   * Loads target node for RelatedToOne relations
   *
   * @param field
   * @param node
   * @param db
   * @return
   */
  protected def loadRelation(field: Field[AnyRef] with Relation,
                             node: Node, depth: Int = 0)(implicit db: Neo4jDatabaseService) = {
    val rel = node.getSingleRelationship(DynamicRelationshipType.withName( relationName(field) ), Direction.OUTGOING)

    val targetRepo =  RepositoryRegistry.get(field.defaultValue.asInstanceOf[Neo4jGraphEntity].clazz)
    field.set(targetRepo.createFromNode(rel.getEndNode, depth))
  }

}