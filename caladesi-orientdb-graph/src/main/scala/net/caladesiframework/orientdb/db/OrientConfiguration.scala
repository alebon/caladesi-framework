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

package net.caladesiframework.orientdb.db

trait OrientConfiguration {
  def getType: OrientDbType
}

case class OrientDbRemoteConfiguration(user: String = "admin",
                                     password: String = "admin",
                                     database: String = "temp",
                                     host: OrientHost = null) extends OrientConfiguration {
  override def getType = OrientDbRemoteType()

}

case class OrientDbEmbeddedConfiguration(location: String = "/tmp/orientdb") extends OrientConfiguration {
  override def getType = OrientDbEmbeddedType()

}

case class OrientDbMemoryConfiguration() extends OrientConfiguration {
  override def getType = OrientDbMemoryType()

}

trait OrientDbType

case class OrientDbMemoryType() extends OrientDbType {}
case class OrientDbEmbeddedType() extends OrientDbType {}
case class OrientDbRemoteType() extends OrientDbType {}
