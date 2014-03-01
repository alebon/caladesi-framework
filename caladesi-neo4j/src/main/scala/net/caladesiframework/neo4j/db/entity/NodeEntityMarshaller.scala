package net.caladesiframework.neo4j.db.entity

import org.neo4j.graphdb.Node

class NodeEntityMarshaller[EntityType <: Neo4jEntity[EntityType]] extends NodeMarshaller[EntityType] {
  def marshal(node: Node)(a: EntityType): Node = {

    a.meta.fields.foreach(field => {

      // TODO: Fill the fields of the node

    })

    node
  }
}
