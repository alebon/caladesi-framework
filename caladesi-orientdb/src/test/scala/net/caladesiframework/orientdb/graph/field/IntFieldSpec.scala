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

package net.caladesiframework.orientdb.graph.field

import org.specs2.mutable._
import net.caladesiframework.orientdb.graph.testkit.TestEntity

class IntFieldSpec extends SpecificationWithJUnit {

   var testEntity : TestEntity = new TestEntity()

   "IntField" should {
     "accept and reuturn values properly" in {

       testEntity.intField.set(5)
       testEntity.intField.is must_==(5)
     }

     "be not optional" in {

       testEntity.intField.optional must_==false
     }
   }
 }
