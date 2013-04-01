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

package net.caladesiframework.orientdb.graph.testkit

import org.specs2.mutable._
import com.orientechnologies.orient.core.db.graph.OGraphDatabasePool
import net.caladesiframework.orientdb.db.{OrientDbRemoteConfiguration, OrientHost, OrientConfiguration}

trait OrientDatabaseTestKit {

  this: SpecificationWithJUnit =>

  implicit val configuration = OrientDbRemoteConfiguration(host = OrientHost())

  def checkOrientDBIsRunning = {
    var running = false

    try {
      OGraphDatabasePool.global().acquire("remote:localhost/db", "admin", "admin")
      running = true
    } catch {
      case _ => // Do nothing, no valid DB
    }
    running must be_==(true).orSkip
  }

}
