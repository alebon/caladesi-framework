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

package net.caladesiframework.orientdb.config

import net.caladesiframework.common.db.DbHostBase
import net.caladesiframework.common.server.ServerAddress

case class OrientHost(server: ServerAddress = ServerAddress("localhost", 2480)) extends DbHostBase {

}

/**
 * The OrientDB Host Helper
 */
object OrientHost {
  // Use default port
  def apply(host: String): OrientHost = OrientHost(ServerAddress(host, 2480))

  // Create host with given host and port
  def apply(host: String, port: Int): OrientHost = OrientHost(ServerAddress(host, port))
}
