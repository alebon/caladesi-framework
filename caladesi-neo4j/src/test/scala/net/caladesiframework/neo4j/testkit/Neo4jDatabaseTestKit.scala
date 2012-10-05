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

package net.caladesiframework.neo4j.testkit

import org.specs2.mutable._
import net.caladesiframework.neo4j.db.Neo4jConfiguration
import net.caladesiframework.neo4j.provider.EmbeddedGraphDatabaseServiceProvider
import java.io.File

trait Neo4jDatabaseTestKit {

  this: SpecificationWithJUnit =>

  implicit val configuration = Neo4jConfiguration(Neo4jStore.ds)

  protected def deleteFileOrDirectory(file: File): Unit = {
    val file = new File(configuration.egdsp.neo4jStoreDir)
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

object Neo4jStore {

  val ds = new EmbeddedGraphDatabaseServiceProvider {
    /**
     * directory where to store the data files
     */
    def neo4jStoreDir = "/Users/abondarenko/Projects/data/neo4j"
  }
}
