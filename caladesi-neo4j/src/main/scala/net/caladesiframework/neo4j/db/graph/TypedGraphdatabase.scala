package net.caladesiframework.neo4j.db.graph

import java.util.UUID
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.{IndexHits, Index}
import net.caladesiframework.neo4j.db.index.IndexSource
import net.caladesiframework.neo4j.db.entity._
import net.caladesiframework.field.Field
import scala.Some

trait TypedGraphDatabase extends GraphDatabase {

  type Identifiable = { def id: UUID }

  import language.reflectiveCalls

  private def createNode[A](a: A)(implicit ma: NodeMarshaller[A]): Node =
    ma.marshal(newNode())(a)

  private def find[A](indexOperation: Index[Node] => IndexHits[Node])
                     (implicit is: IndexSource[A], unmarshaller: NodeUnmarshaller[A]):
  Option[(A, Node)] = {
    val index = is.getIndex(graphDatabaseService)
    val hits = indexOperation(index)
    val result = if (hits.size() == 1) {
      val node = hits.getSingle
      Some((unmarshaller.unmarshal(node), node))
    } else {
      None
    }
    hits.close()
                                          4japh
    result
  }

  private def byIdIndexOperation(id: UUID):
    Index[Node] => IndexHits[Node] = index => index.get("id", id.toString)

  private def byFieldIndexOperation(field: Field[_,_]):
    Index[Node] => IndexHits[Node] = index => index.get(field.name, field.get)

  def findOne[A <: Identifiable]
    (id: UUID)
    (implicit is: IndexSource[A], uma: NodeUnmarshaller[A]):
    Option[(A, Node)] =
    find(byIdIndexOperation(id))

  def findOneEntity[A <: Identifiable]
    (id: UUID)
    (implicit is: IndexSource[A], uma: NodeUnmarshaller[A]):
    Option[A] =
      find(byIdIndexOperation(id)).map(_._1)

  def findOneEntityWithIndex[A]
    (indexOperation: Index[Node] => IndexHits[Node])
    (implicit is: IndexSource[A], uma: NodeUnmarshaller[A]):
    Option[A] =
      find(indexOperation).map(_._1)

  def addNode[A <: Neo4jEntity[A]]
    (a: A)
    (implicit is: IndexSource[A]): Node = {
      implicit lazy val nodeMarshaller = new NodeEntityMarshaller[A]()

      val node = createNode(a)
      is.getIndex(graphDatabaseService).putIfAbsent(node, "id", "TODO")
      node
  }
}

