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

package net.caladesiframework.document

trait Field[FieldType, OwnerType] {

  def name: String
  def owner: OwnerType

  def initField: Unit = {}
}

/**
 * Optional Field handling
 *
 * @tparam FieldType
 * @tparam OwnerType
 */
trait OptionalField[FieldType, OwnerType] extends Field[FieldType, OwnerType] {

  protected var value: Option[FieldType] = None

  def set(value: FieldType) = {
    this.value = Some(value)
  }

  def reset = {
    this.value = None
  }

  def getOrElse(alternativeValue: FieldType): FieldType = {
    if (this.value.isEmpty) {
      return alternativeValue
    }
    return this.value.get
  }

}

trait RequiredField[FieldType, OwnerType] extends Field[FieldType, OwnerType] {

  protected var value: FieldType = defaultValue

  def defaultValue: FieldType

  def set(value: FieldType) = {
    this.value = value
  }

  def get: FieldType = {
    this.value
  }
}
