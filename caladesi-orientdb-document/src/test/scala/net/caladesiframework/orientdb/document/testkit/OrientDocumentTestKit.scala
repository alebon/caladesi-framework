/*
 * Copyright 2013 Caladesi Framework
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

package net.caladesiframework.orientdb.document.testkit

import org.specs2.mutable.{Around, SpecificationWithJUnit}
import org.specs2.execute.Result
import net.caladesiframework.orientdb.config.{OrientDbEmbeddedConfiguration, OrientDbMemoryConfiguration, OrientConfigurationRegistry}
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx

trait OrientDocumentTestKit {

  def initMemoryDatabase() = {
    OrientConfigurationRegistry.register(OrientDbMemoryConfiguration(name = "inmemorytest"), "memoryDB")
  }

  def destroyMemoryDatabase() {
    val config = OrientConfigurationRegistry.loadByName("memoryDB").asInstanceOf[OrientDbMemoryConfiguration]
    val db = new ODatabaseDocumentTx("memory:%s".format(config.name))
    if (db.exists()) {
      if (db.isClosed) {
        db.open("admin", "admin")
      }
      db.drop()
    }
  }

  def initDatabase() = {
    OrientConfigurationRegistry.register(OrientDbEmbeddedConfiguration(location = "/Users/abondarenko/Projects/data/orientdb-test"))
  }

  def destroyDatabase() {
    val config = OrientConfigurationRegistry.loadByName("default").asInstanceOf[OrientDbEmbeddedConfiguration]
    //val db = new ODatabaseDocumentTx("memory:%s".format(config.name))
    //if (db.exists()) {
    //  db.drop()
    //  db.close()
    //}

  }

  object OrientEmbeddedTestContext extends Around {

    def around[T <% Result](testToRun: => T)  = {
      initDatabase()

      try {
        testToRun
      } finally {
        destroyDatabase()
      }
    }

  }

  object OrientMemoryTestContext extends Around {

    def around[T <% Result](testToRun: => T)  = {
      initMemoryDatabase()

      try {
        testToRun
      } finally {
        destroyMemoryDatabase()
      }
    }

  }

}
