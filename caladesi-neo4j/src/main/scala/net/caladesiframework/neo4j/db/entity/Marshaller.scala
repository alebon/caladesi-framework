package net.caladesiframework.neo4j.db.entity

import org.neo4j.graphdb.Node

/**
 * Modifies the given ``Node``s with values in the instances of ``A``
 *
 * @tparam A the A
 */
trait NodeMarshaller[A] {
  def marshal(node: Node)(a: A): Node
}

/**
 * Unmarshals given ``Node``s to create instances of ``A``
 *
 * @tparam A the A
 */
trait NodeUnmarshaller[A] {
  def unmarshal(node: Node): A
}

