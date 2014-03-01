package net.caladesiframework.neo4j.db.testkit

import org.specs2.mutable.SpecificationWithJUnit
import java.io.File
import java.util.UUID
import org.neo4j.graphdb.factory.GraphDatabaseFactory

trait Neo4jTestKit {

  this: SpecificationWithJUnit =>

  def neo4jStoreDir = "/tmp/%s".format(UUID.randomUUID().toString)

  lazy val graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(neo4jStoreDir)

  protected def deleteFileOrDirectory(file: File): Unit = {
    val file = new File(neo4jStoreDir)
    if ( file.exists() ) {
      if ( file.isDirectory() ) {
        for ( child: File <- file.listFiles() ) {
          deleteFileOrDirectory( child )
        }
      }
      file.delete()
    }
  }
}
