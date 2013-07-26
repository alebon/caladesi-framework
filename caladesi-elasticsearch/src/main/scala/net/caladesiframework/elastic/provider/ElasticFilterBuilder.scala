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

package net.caladesiframework.elastic.provider

import org.elasticsearch.index.query.{AndFilterBuilder, FilterBuilders, FilterBuilder}

object ElasticFilterBuilder {

  /**
   * Builds AND filter for facet, excluding the facet field
   *
   * @param facet
   * @param filterMap
   * @return
   */
  def buildAndFacetFilter(facet: String, filterMap: Map[String, String]): AndFilterBuilder = {

    val filter = FilterBuilders.andFilter()
    filterMap.toList.foreach(entry => {
      if (!entry._1.equals(facet)) {
        filter.add(FilterBuilders.termFilter(entry._1, entry._2))
      }
    })

    filter
  }

}
