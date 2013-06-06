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
   * Adds an item to the given index
   *
   * @param indexName
   * @param itemType
   * @param id
   * @param dbValues
   */
  def addItem(indexName: String, itemType: String, id: String, dbValues: XContentBuilder) = {
    val start = System.currentTimeMillis()

    client.prepareIndex(indexName, itemType, id).setSource(dbValues).setRefresh(true).execute().actionGet()

    val end = System.currentTimeMillis()

    client.admin().indices().prepareRefresh(indexName).execute().actionGet()
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
    client.admin().indices().prepareRefresh(indexName).execute().actionGet()
  }

  /**
   * Performs a query and return an elastic SearchResponse
   *
   * @param queryTerm
   * @param indexName
   * @param itemType
   * @return
   */
  def query(field: Field[_, _], queryTerm : String, indexName: String, itemType: String, termMatchType: String = TermMatchType.FUZZY): SearchResponse = {

    import org.elasticsearch.index.query.QueryBuilders._

    //debug("Query for term '%s'".format(queryTerm))

    val response: SearchResponse = client.prepareSearch(indexName)
      .setTypes(itemType)
      //.setSearchType(SearchType.DEFAULT)
      .setQuery(fuzzyQuery(field.name, queryTerm).maxExpansions(5)) // Query
      //.setFrom(0).setSize(60).setExplain(true)
      .execute()
      .actionGet()

    response
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
