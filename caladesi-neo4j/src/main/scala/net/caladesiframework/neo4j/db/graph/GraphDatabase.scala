package net.caladesiframework.neo4j.db.graph

import org.neo4j.graphdb.{Node, GraphDatabaseService}

trait GraphDatabase {

  def graphDatabaseService: GraphDatabaseService

  /**
   * Performs block ``f`` within a transaction
   *
   * @param f the block to be performed
   * @tparam T the type the inner block returns
   * @return ``f``'s result
   */
  def withTransaction[T](f: GraphDatabaseService => T): T = {
    val tx = graphDatabaseService.beginTx
    try {
      val result = f(graphDatabaseService)
      tx.success()
      result
    } catch {
      case e: Throwable =>
        tx.failure()
        throw e
    } finally {
      tx.close()
    }
  }

  /**
   * Performs block ``f`` within a synchronized transaction
   *
   * @param f the block to be performed
   * @tparam T the type the inner block returns
   * @return ``f``'s result
   */
  def withSyncTransaction[T](f: GraphDatabaseService => T): T = {
    val tx = synchronized { graphDatabaseService.beginTx }
    try {
      val result = f(graphDatabaseService)
      tx.success()
      result
    } catch {
      case e: Throwable =>
        tx.failure()
        throw e
    } finally {
      tx.close()
    }
  }

  /**
   * Creates a new and empty node
   *
   * @return the newly created node
   */
  def newNode(): Node = graphDatabaseService.createNode()

}

