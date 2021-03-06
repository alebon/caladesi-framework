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
import org.specs2.execute.{AsResult, Result}
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
      db.close()
    }
  }

  def initDatabase() = {
    OrientConfigurationRegistry.register(OrientDbEmbeddedConfiguration(location = "/%s/orientdb-test".format(System.getProperty("java.io.tmpdir"))))
  }

  def destroyDatabase() {
    val config = OrientConfigurationRegistry.loadByName("default").asInstanceOf[OrientDbEmbeddedConfiguration]
    val db = new ODatabaseDocumentTx("plocal::%s".format(config.location))
    if (db.exists()) {
      if (db.isClosed) {
        db.open("admin", "admin")
      }
      db.drop()
      db.close()
    }
  }

  object OrientEmbeddedTestContext extends Around {

    def around[T](t: => T)(implicit evidence$1: AsResult[T]) = {
      initDatabase()

      try {
        AsResult(t)
      } finally {
        destroyDatabase()
      }
    }

  }

  object OrientMemoryTestContext extends Around {

    def around[T](t: => T)(implicit evidence$1: AsResult[T]) = {
      initMemoryDatabase()

      try {
        AsResult(t)
      } finally {
        destroyMemoryDatabase()
      }
    }

  }

}
