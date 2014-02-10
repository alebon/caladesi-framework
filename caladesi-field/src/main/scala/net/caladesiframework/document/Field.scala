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

  protected var value: FieldType

  private var fieldName: String = null

  def name: String = fieldName
  def owner: OwnerType

  def initField: Unit = {}

  def applyName(name: String) = {
    this.fieldName = name
  }

  def get = {
    this.value
  }

  def set(value: FieldType): Unit = {
    this.value = value
  }
}

/**
 * Optional Field handling
 *
 * @tparam FieldType
 * @tparam OwnerType
 */
trait OptionalField[FieldType, OwnerType] extends Field[FieldType, OwnerType] {

  protected var valueOption: Option[FieldType] = None

  override def set(value: FieldType) = {
    this.valueOption = Some(value)
  }

  def reset = {
    this.valueOption = None
  }

  def getOrElse(alternativeValue: FieldType): FieldType = {
    if (this.valueOption.isEmpty) {
      return alternativeValue
    }
    return this.valueOption.get
  }

  override def get = {
    this.valueOption.get
  }

  def hasValue: Boolean = {
    if (this.valueOption.isEmpty) {
      return false
    }
    true
  }

}

trait RequiredField[FieldType, OwnerType] extends Field[FieldType, OwnerType] {

  def defaultValue: FieldType

  override protected var value: FieldType = defaultValue
}
