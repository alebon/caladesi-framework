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
import net.caladesiframework.orientdb.graph.entity.{OrientGraphEntity}
import net.caladesiframework.orientdb.graph.OrientGraphRepository
import java.util.UUID

class QueryBuilder {

  protected var qry : String = ""

  protected var callBack : OrientGraphRepository[OrientGraphEntity] = null

  protected var params: List[AnyRef] = List[AnyRef]()

  protected def select(entity: OrientGraphEntity) = {
    qry += "select from " + callBack.repositoryEntityClass
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

  /**
   * To lower case modifier (only after where)
   *
   * @return
   */
  def toLC: QueryBuilder = {
    qry += ".toLowerCase()"
    this
  }

  def eqs(value: Any) = {
    value match {
      case v: UUID => params = v.toString :: params
      case v: scala.Double => params = v.asInstanceOf[java.lang.Double] :: params
      case v:scala.Int => params = v.asInstanceOf[java.lang.Integer] :: params
      case _ => params = value.toString :: params
    }
    qry += " = ?"
    this
  }

  /**
   * Adds where condition (Fluent interface)
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
    println("Executing query: " + qry + " with params " + params.toString())
    callBack.execute(qry, reverse[AnyRef](params):_*)
  }

  def this(entity: Any, repository: Any) = {
    this
    callBack = repository.asInstanceOf[OrientGraphRepository[OrientGraphEntity]]
    select(entity.asInstanceOf[OrientGraphEntity])
  }

  /**
   * Returns the currently pre-assembled query
   * @return
   */
  def qryTemp = {
    this.qry
  }

  /**
   * Little helper method
   *
   * @param l
   * @tparam T
   * @return
   */
  protected def reverse[T](l: List[T]) : List[T] = l match {
    case Nil => Nil
    case h::t => reverse(t):::List(h)
  }
}
