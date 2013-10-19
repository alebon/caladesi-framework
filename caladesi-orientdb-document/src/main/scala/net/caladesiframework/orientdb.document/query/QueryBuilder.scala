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

package net.caladesiframework.orientdb.document.query

import java.util.UUID
import net.caladesiframework.orientdb.document.record.OrientRecord
import net.caladesiframework.document.Field

class QueryBuilder[QRType]() {

  protected var qry : String = ""

  protected var prototype: Option[OrientRecord[_]] = None

  protected var params: List[AnyRef] = List[AnyRef]()

  protected def select(entity: OrientRecord[_]) = {
    qry += "select from " + prototype.get.meta.collectionName
  }

  /**
   * Query limit (fluent interface)
   *
   * @param limit
   */
  def limit(limit: Long = 10) : QueryBuilder[QRType] = {
    qry += " limit " + limit
    this
  }

  /**
   * Adds where conditions (Fluent interface)
   *
   * @return
   */
  def where(field: Field[_, _]) : QueryBuilder[QRType] = {
    qry += " where " + field.name
    this
  }

  /**
   * To lower case modifier (only after where)
   *
   * @return
   */
  def toLC: QueryBuilder[QRType] = {
    qry += ".toLowerCase()"
    this
  }

  def eqs(value: Any): QueryBuilder[QRType] = {
    value match {
      case v: UUID => params = v.toString :: params
      case v: scala.Double => params = v.asInstanceOf[java.lang.Double] :: params
      case v:scala.Int => params = v.asInstanceOf[java.lang.Integer] :: params
      case _ => params = value.toString :: params
    }
    qry += " = ?"
    this
  }

  def like(value: String): QueryBuilder[QRType] = {
    params = "%" + value.toString + "%" :: params
    qry += ".toLowerCase() LIKE ?"
    this
  }

  def startsLike(value: String): QueryBuilder[QRType] = {
    params = value.toString + "%" :: params
    qry += ".toLowerCase() LIKE ?"
    this
  }

  def endsLike(value: String): QueryBuilder[QRType] = {
    params = "%" + value.toString :: params
    qry += ".toLowerCase() LIKE ?"
    this
  }

  def in(values:AnyRef*): QueryBuilder[QRType] = {
    values foreach {
      value => {
        params = value.toString :: params
      }
    }

    qry += " in [" + values.map(value => "?").mkString(",") + "]"
    this
  }

  /**
   * Adds where condition (Fluent interface)
   *
   * @return
   */
  def and[FieldType](field: Field[_, _]) : QueryBuilder[QRType] = {
    qry += " and " + field.name

    this
  }

  def skip(value: Long) : QueryBuilder[QRType] = {

    qry += " skip " + value

    this
  }

  /**
   * Execute the query
   */
  def ex: List[QRType] = {
    //println("Executing query: " + qry + " with params " + params.toString())
    prototype.get.meta.execute(this.qry, this.reverse[AnyRef](params):_*).asInstanceOf[List[QRType]]
  }

  def setPrototype(record: OrientRecord[_]) = {
    this.prototype = Some(record)
    select(record)
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
