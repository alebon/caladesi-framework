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

package net.caladesiframework.orientdb.query

import net.caladesiframework.orientdb.field.Field
import net.caladesiframework.orientdb.graph.entity.OrientGraphEntity
import java.util.UUID
import net.caladesiframework.orientdb.graph.OrientGraphRepository

class IndexQueryBuilder extends QueryBuilder {

  override def select(entity: OrientGraphEntity) = {
    qry += "select from index:" + callBack.repositoryEntityClass + "_"
  }

  /**
   * Adds where conditions (Fluent interface)
   *
   * @return
   */
  override def where[FieldType](field: Field[FieldType]): IndexQueryBuilder = {
    qry += field.name + " WHERE key"
    this
  }

  /**
   * Restricted
   *
   * @param value
   * @return
   */
  override def eqs(value: Any) = {
    throw new Exception("Not allowed on index queries")
  }

  def contains(value: Any) = {
    value match {
      case v: UUID => params = v.toString :: params
      case v: scala.Double => params = v.asInstanceOf[java.lang.Double] :: params
      case v: scala.Int => params = v.asInstanceOf[java.lang.Integer] :: params
      case _ => params = value.toString :: params
    }
    qry += " CONTAINSTEXT ?"
    this
  }

  def this(entity: Any, repository: Any) = {
    this
    callBack = repository.asInstanceOf[OrientGraphRepository[OrientGraphEntity]]
    select(entity.asInstanceOf[OrientGraphEntity])
  }

  /**
   * Adds where condition (Fluent interface)
   *
   * @return
   */
  override def and[FieldType](field: Field[FieldType]): IndexQueryBuilder = {
    throw new Exception("Not allowed on index queries")
  }
}
