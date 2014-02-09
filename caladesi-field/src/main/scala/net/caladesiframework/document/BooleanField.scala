/*
 * Copyright 2014 Caladesi Framework
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

class BooleanField[OwnerType](ownerConstruct: OwnerType, default: Boolean = false) extends RequiredField[Boolean, OwnerType]{

  def owner = ownerConstruct

  def defaultValue = default
}

class OptionalBooleanField[OwnerType](ownerConstruct: OwnerType) extends OptionalField[Boolean, OwnerType] {

  def owner = ownerConstruct

  def defaultValue = None

}
