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

import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.action.admin.cluster.health.{ClusterHealthRequest, ClusterIndexHealth}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.client.Requests
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import net.caladesiframework.elastic.search.TermMatchType
import org.elasticsearch.common.xcontent.XContentBuilder
import net.caladesiframework.document.Field
import org.elasticsearch.search.SearchHitField
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.search.facet.FacetBuilders
import scala.collection.immutable.HashMap

case class ElasticProvider(nodeName: String, path: String, useHttpConnector: Boolean = false) {

  private lazy val node = startUp()
  private lazy val client = node.client()

  /**
   * Setup
   *
   * @return
   */
  def startUp() = {

    val settings: ImmutableSettings.Builder = ImmutableSettings.settingsBuilder()
    settings.put("node.name", nodeName)
    settings.put("path.data", path);
    settings.put("http.enabled", useHttpConnector);

    NodeBuilder.nodeBuilder().settings(settings)
      .clusterName(nodeName)
      .data(true).local(true).node()

  }

  /**
   * Close elastic properly
   */
  def shutdown = {

    client.close()
    node.close()

  }

  /**
   * Generates index if not present in health response
   *
   * @param indexName
   * @param itemType
   * @return
   */
  def ensureIndex(indexName: String, itemType: String) = {

    //debug("Ensuring index '%s' for %s".format(indexName, entityName))

    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet()

    val map: java.util.Map[String, ClusterIndexHealth] = client.admin().cluster().health(new
        ClusterHealthRequest(indexName)).actionGet().getIndices()

    if (!map.containsKey(indexName)) {
      val settings: ImmutableSettings.Builder = ImmutableSettings.settingsBuilder()
      settings.put("_index", indexName)
      settings.put("_type", itemType)
      val request: CreateIndexRequest = Requests.createIndexRequest(indexName).settings(settings)

      //debug("Creating index %s with type %s".format(indexName, entityName))
      client.admin().indices().create(request).actionGet()
    }

  }

  /**
   * Removes the index if present in health response
   *
   * @param indexName
   * @return
   */
  def deleteIndex(indexName: String) = {
    node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet()

    val map: java.util.Map[String, ClusterIndexHealth] = client.admin().cluster().health(new
        ClusterHealthRequest(indexName)).actionGet().getIndices()

    if (map.containsKey(indexName)) {
      val request: DeleteIndexRequest = Requests.deleteIndexRequest(indexName)
      client.admin().indices().delete(request).actionGet()
    }
  }

  /**
   * Adds an item to the given index
   *
   * @param indexName
   * @param itemType
   * @param id
   * @param dbValues
   */
  def addItem(indexName: String, itemType: String, id: String, dbValues: XContentBuilder) = {
    val start = System.currentTimeMillis()

    client.prepareIndex(indexName, itemType, id).setSource(dbValues).execute().actionGet()

    val end = System.currentTimeMillis()

    client.admin().indices().prepareRefresh().execute().actionGet()
  }

  /**
   * Returns an item by given id
   *
   * @param indexName
   * @param itemType
   * @param id
   * @return
   */
  def getItemById(indexName: String, itemType: String, id: String): GetResponse = {
    val response: GetResponse = client.prepareGet(indexName, itemType, id)
      .execute()
      .actionGet()

    return response
  }

  /**
   * Removes item from index
   *
   * @param indexName
   * @param id
   * @param itemType
   * @return
   */
  def removeItem(indexName: String, itemType: String, id: String) = {
    val request = Requests.deleteRequest(indexName)
      .`type`(itemType)
      .id(id)

    client.delete(request).actionGet()
    client.admin().indices().prepareRefresh().execute().actionGet()
  }

  def executeFuzzyQuery(fieldName: String, queryTerm : String, indexName: String, itemType: String): SearchResponse = {
    import org.elasticsearch.index.query.QueryBuilders._

    val response: SearchResponse = client.prepareSearch(indexName)
      .setTypes(itemType)
      //.setSearchType(SearchType.DEFAULT)
      .setQuery(queryString("*%s*".format(queryTerm)).field(fieldName))
      //.setQuery(fuzzyQuery(fieldName, "%s*".format(queryTerm)).maxExpansions(100)) // Query
      //.setFrom(0).setSize(60).setExplain(true)
      .execute()
      .actionGet()

    response
  }

  /**
   * Filters search request by given fields and its values
   *
   * @param indexName
   * @param itemType
   * @param filterMap
   * @return
   */
  def executeFilterQuery(indexName: String, itemType: String, filterMap: HashMap[String, String]): SearchResponse = {

    val filter = FilterBuilders.boolFilter()
    val responsePrepare = client.prepareSearch(indexName)
      .setTypes(itemType)

    filterMap.toList.foreach(entry => {
      filter.must(FilterBuilders.termFilter(entry._1, entry._2))
      responsePrepare.addFacet(FacetBuilders.termsFacet(entry._1 + "Facet").field(entry._1))
    })
    responsePrepare.setFilter(filter)

    val response: SearchResponse = responsePrepare.execute()
      .actionGet()

    response
  }

  /**
   * Common facet query (get facets for a given field)
   *
   * @param fieldName
   * @return
   */
  def executeFacetForFieldQuery(fieldName: String, indexName: String, itemType: String): TermsFacet = {

    val matchAllQueryBuilder = QueryBuilders.matchAllQuery()
    val fieldFacet = FacetBuilders.termsFacet(fieldName + "Facet").field(fieldName)
    val response: SearchResponse = client.prepareSearch(indexName)
      .setTypes(itemType)
      .setQuery(matchAllQueryBuilder)
      .addFacet(fieldFacet)
      .execute().actionGet()

    response.getFacets().facet(fieldName + "Facet")
  }

  /**
   * Performs count request for given index/type combination
   *
   * @param indexName
   * @param itemType
   * @return
   */
  def countAll(indexName: String, itemType: String): Long = {
    val countResponse = client.prepareCount(indexName).execute().actionGet()

    return countResponse.getCount
  }
}
