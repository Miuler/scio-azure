/*
 * Copyright 2022 Miuler.
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

package com.spotify.scio.aztables.read

import com.azure.data.tables.models.{ ListEntitiesOptions, TableEntity }
import com.azure.data.tables.{ TableServiceAsyncClient, TableServiceClientBuilder }
import com.spotify.scio.annotations.experimental
import org.apache.beam.sdk.io.BoundedSource
import org.slf4j.LoggerFactory

@experimental
private class TableStorageBoundedReader(tableStorageBoundedSource: TableStorageBoundedSource)
    extends BoundedSource.BoundedReader[TableEntity] {
  private val log = LoggerFactory.getLogger(getClass)
  private var maybeTableServiceAsyncClient: Option[TableServiceAsyncClient] = None
  private var maybeIterator: Option[java.util.Iterator[TableEntity]] = None
  @volatile private var current: Option[TableEntity] = None
  @volatile private var recordsReturned = 0L

  override def start(): Boolean = {
    log.debug("TableStorageBoundedReader.start()")

    maybeTableServiceAsyncClient = Some(
      if(tableStorageBoundedSource.connectionString != null) {
        new TableServiceClientBuilder()
          .connectionString(tableStorageBoundedSource.connectionString)
          .buildAsyncClient()
      } else {
        new TableServiceClientBuilder()
          .endpoint(tableStorageBoundedSource.endpoint)
          .sasToken(tableStorageBoundedSource.sasToken)
          .buildAsyncClient()
      }
    )

    maybeIterator = maybeTableServiceAsyncClient.map { tableServiceAsyncClient =>
      log.info("Get the container name")
      log.info(
        s"Get the iterator of the query in container ${tableStorageBoundedSource.tableName}"
      )

      val listEntitiesOptions = new ListEntitiesOptions

      val query = tableStorageBoundedSource.query
      if (query != null && query.nonEmpty) {
        listEntitiesOptions.setFilter(query)
      }

      tableServiceAsyncClient
        .getTableClient(tableStorageBoundedSource.tableName)
        .listEntities(listEntitiesOptions)
        .toIterable
        .iterator()
    }

    advance()
  }

  override def advance(): Boolean = maybeIterator match {
    case Some(iterator) if iterator.hasNext =>
      current = Some(iterator.next())
      recordsReturned += 1
      true
    case _ =>
      false
  }

  override def getCurrent: TableEntity = current.orNull

  override def getCurrentSource: TableStorageBoundedSource = tableStorageBoundedSource

  override def close(): Unit = ()
}
