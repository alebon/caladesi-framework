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

package net.caladesiframework.orientdb.document.record

import net.caladesiframework.document.Field
import net.caladesiframework.orientdb.config._
import com.orientechnologies.orient.core.db.document.{ODatabaseDocumentTx, ODatabaseDocumentPool, ODatabaseDocument}
import com.orientechnologies.orient.core.tx.OTransaction
import net.caladesiframework.orientdb.config.OrientDbRemoteType
import net.caladesiframework.orientdb.config.OrientDbRemoteConfiguration
import net.caladesiframework.orientdb.config.OrientDbEmbeddedType
import net.caladesiframework.orientdb.config.OrientDbMemoryType
import net.caladesiframework.orientdb.config.OrientDbEmbeddedConfiguration
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Holds meta information and operations on a record
 *
 * @tparam RecordType
 */
trait OrientMetaRecord[RecordType] extends OrientRecord[RecordType] {
  self: RecordType =>

  def databaseName: String = "default"

  def collectionName: String = this.getClass.getSuperclass.getSimpleName

  def clusterName: String = "default"


  def createRecord: RecordType = {
    val record = this.getClass.getSuperclass.newInstance().asInstanceOf[RecordType]

    // Avoid iterating all fields
    //if (!initComplete) {
      initFields(record)
    //  initComplete = true
    //}

    record
  }

  lazy val config: OrientConfiguration = {
    OrientConfigurationRegistry.loadByName(this.databaseName)
  }

  private [this] val fieldMap: scala.collection.mutable.Map[String, Field[_, RecordType]]
    = new scala.collection.mutable.HashMap[String, Field[_, RecordType]]()

  private [this] var databaseRef: ODatabaseDocument = null

  def fields = this.fieldMap.toSeq

  private var initComplete: Boolean = false

  private def initFields(record: Any) = {
    record.getClass.getDeclaredFields foreach {
      field => {
        field.getType.getMethods foreach {

          // If field contains a method for initialization, call it and add field to meta fields
          method => {
            if  (method.getName.equals("initField")) {
              field.setAccessible(true)
              val m = record.getClass.getMethod(field.getName.replace("$module", ""))

              val fieldObj: Field[_, RecordType] = m.invoke(record).asInstanceOf[Field[_, RecordType]]

              // Apply default naming
              if (fieldObj.name == null) {
                fieldObj.applyName(field.getName.replace("$module", ""))
              }

              fieldObj.initField
              attach(field.getName.replace("$module", ""), fieldObj)
            }
          }
        }

      }
    }
  }

  /**
   * Adds the field to the meta fields
   *
   * @param field
   * @return
   */
  private def attach(key: String, field: Field[_, RecordType]) = {
    if (this.fieldMap.get(key).isEmpty) {
      this.fieldMap.put(key, field)
    }
  }

  /**
   * Returns the count of record for this collection
   *
   * @return
   */
  def count() = countClass()

  /**
   * Returns the count of record for this collection
   *
   * @return
   */
  def countClass() = connected(implicit db => {
    // Initial class creation
    val clazz = db.getMetadata.getSchema.getOrCreateClass(meta.collectionName)

    val count = db.countClass(clazz.getName)
    println("Amount for clazz  %s: %s".format(clazz.getName, count))

    count
  })

  /**
   * Returns the count of record for this collection
   *
   * @return
   */
  def countCluster() = connected(implicit db => {
    db.countClusterElements(clusterName)
  })

  /**
   * Opens the db, performs execution
   *
   * @param f
   * @tparam T
   * @return
   */
  def transactional[T <: Any](f: ODatabaseDocument => T) : T = {

    val db = emitDb
    if (db.isClosed) {
      db.open("admin", "admin")
    }
    //db.open("admin", "admin")

    try {
      val transaction = synchronized { db.begin(OTransaction.TXTYPE.OPTIMISTIC) }
      val ret = f(db)
      transaction.commit()

      return ret
    } catch {
      case e:Exception =>
        db.rollback()
        throw new Exception("Failure during execution (%s) : %s - STACKTRACE: %s".format(e.getClass, e.getMessage, e.getStackTraceString))
    } finally {
      db.close()
    }
  }

  /**
   * Opens the db, performs execution and closes connection
   *
   * @param f
   * @tparam T
   * @return
   */
  def connected[T <: Any](f: ODatabaseDocument => T) : T = {
    val db = emitDb
    if (db.isClosed) {
      db.open("admin", "admin")
    }

    try {
      val ret = f(db)
      return ret
    } catch {
      case e:Exception =>
        throw new Exception("Failure during execution in connected mode: " + e.getMessage + " - Stacktrace:" + e.getStackTraceString)
    } finally {
      db.close()
    }
  }

  private def emitDb = meta.config.getType match {
    case dbType: OrientDbRemoteType =>
      val remoteConfig = meta.config.asInstanceOf[OrientDbRemoteConfiguration]

      ODatabaseDocumentPool.global()
        .acquire("remote:" + remoteConfig.host.server.location + "/" + remoteConfig.database,
        remoteConfig.user, remoteConfig.password)

    case dbType: OrientDbEmbeddedType =>
      val localConfig = meta.config.asInstanceOf[OrientDbEmbeddedConfiguration]

      // TODO: Refactoring to single initial call
      initLocalStore(localConfig.location)
      //ODatabaseDocumentPool.global().acquire("local:%s".format(localConfig.location), "admin", "admin")
      new ODatabaseDocumentTx("local:%s".format(localConfig.location))

    case dbType: OrientDbMemoryType =>
      val memoryConfig = meta.config.asInstanceOf[OrientDbMemoryConfiguration]

      databaseRef = new ODatabaseDocumentTx("memory:%s".format(memoryConfig.name))
      if (!databaseRef.exists()) {
        databaseRef.create()
      }

      if (databaseRef.isClosed) {
        databaseRef.open("admin", "admin")
      }

      databaseRef = ODatabaseDocumentPool.global().acquire("memory:%s".format(memoryConfig.name), "admin", "admin")

      databaseRef
    case _ =>
      throw new NotImplementedException()
  }


  /**
   *
   * @param path
   */
  private def initLocalStore(path: String) = {
    try {
      val db: ODatabaseDocumentTx = new ODatabaseDocumentTx("local:%s".format(path))
      if (!db.exists()) {
        db.create()
      }
    } catch {
      case e:Exception => throw new RuntimeException("Couldn't create DB at local store %s. Reason: %s".format(path, e.getMessage))
    }

  }

}
