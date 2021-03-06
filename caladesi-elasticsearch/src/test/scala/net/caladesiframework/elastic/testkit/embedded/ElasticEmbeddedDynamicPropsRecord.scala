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

package net.caladesiframework.elastic.testkit.embedded

import net.caladesiframework.elastic.record.{ElasticMetaRecord, ElasticRecord}
import net.caladesiframework.elastic.field.{StringField, DynamicPropertiesField}
import net.caladesiframework.elastic.field.analyzer.{Analyzed, NotAnalyzed}

class ElasticEmbeddedDynamicPropsRecord extends ElasticRecord[ElasticEmbeddedDynamicPropsRecord] {
  def meta = ElasticEmbeddedDynamicPropsRecord

  object notAnalyzedStringProperty extends StringField(this) with NotAnalyzed

  object analyzedStringProperty extends StringField(this) with Analyzed

  object dynamicProperties extends DynamicPropertiesField[ElasticEmbeddedDynamicPropsRecord](this)
}

object ElasticEmbeddedDynamicPropsRecord extends ElasticEmbeddedDynamicPropsRecord
  with ElasticMetaRecord[ElasticEmbeddedDynamicPropsRecord]
