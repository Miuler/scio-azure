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

import com.azure.data.tables.models.TableEntity
import com.spotify.scio.annotations.experimental
import org.apache.beam.sdk.io.Read
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.values.{PBegin, PCollection}
import org.slf4j.LoggerFactory

@experimental
private[aztables] case class TableStorageRead(
  endpoint: String,
  sasToken: String,
  tableName: String,
  query: String = null,
  connectionString: String
) extends PTransform[PBegin, PCollection[TableEntity]] {
  private val log = LoggerFactory.getLogger(classOf[TableStorageRead])

  override def expand(input: PBegin): PCollection[TableEntity] = {
    log.info(s"Read CosmosDB with endpoint: $endpoint and query: $query")

    input.apply(Read.from(new TableStorageBoundedSource(endpoint, sasToken, tableName, query, connectionString)))
  }
}
