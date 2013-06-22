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

package net.caladesiframework.orientdb.document

import entity.Document
import net.caladesiframework.orientdb.repository.CRUDRepository

class OrientDocumentRepository[T <: Document](implicit tag: scala.reflect.ClassTag[T]) extends CRUDRepository[T] {

  /**
   * Create a fresh document
   * @return
   */
  def create = tag.runtimeClass.newInstance().asInstanceOf[T]

  def update(document: T) = {
    document.fields foreach {
      field => {

      }
    }

    throw new Exception("Not implemented yet")
  }

  def delete(document: T) = throw new Exception("Not implemented yet")
}
