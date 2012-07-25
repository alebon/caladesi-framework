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
import net.caladesiframework.orientdb.graph.entity.{GraphEntity, OrientGraphEntity}
import net.caladesiframework.orientdb.graph.OrientGraphRepository

class QueryBuilder {

  private var qry : String = ""

  private var callBack : OrientGraphRepository[OrientGraphEntity] = null

  private def select(entity: OrientGraphEntity) = {
    qry += "select from " + entity.clazz
  }

  /**
   * Query limit (fluent interface)
   *
   * @param limit
   */
  def limit(limit: Long = 10) : QueryBuilder = {
    qry += " limit " + limit
    this
  }

  /**
   * Adds where conditions (Fluent interface)
   *
   * @return
   */
  def where[FieldType](field: Field[FieldType]) : QueryBuilder = {
    qry += " where " + field.name
    this
  }

  def eqs(value: Any) = {
    // @TODO Add field type awareness
    if (value.isInstanceOf[String]) {
      qry += " = '" + value + "'"
    } else {
      qry += " = " + value
    }
    this
  }

  /**
   * Adds where condition (Fluent interface)
   * // JCT-Q2H-X95-94K
   *
   * @return
   */
  def and[FieldType](field: Field[FieldType]) : QueryBuilder = {
    qry += " and " + field.name

    this
  }

  def skip(value: Long) : QueryBuilder = {

    qry += " skip " + value

    this
  }

  /**
   * Execute the query
   */
  def ex = {
    callBack.execute(qry)
  }

  def this(entity: Any, repository: Any) = {
    this
    select(entity.asInstanceOf[OrientGraphEntity])
    callBack = repository.asInstanceOf[OrientGraphRepository[OrientGraphEntity]]
  }

  /**
   * Returns the currently pre-assembled query
   * @return
   */
  def qryTemp = {
    this.qry
  }

}
