package net.caladesiframework.neo4j.db.index

import org.neo4j.graphdb.{Relationship, Node, GraphDatabaseService}
import org.neo4j.graphdb.index.Index

/**
 * Provides index for the ``A``s
 *
 * @tparam A the A
 */
trait IndexSource[A] {

  def getIndex(graphDatabase: GraphDatabaseService): Index[Node]

  def getFulltextIndex(graphDatabase: GraphDatabaseService): Index[Node]

  def getRelationshipIndex(graphDatabase: GraphDatabaseService): Index[Relationship]

}


