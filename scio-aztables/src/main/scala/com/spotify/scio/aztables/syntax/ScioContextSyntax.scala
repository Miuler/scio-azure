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

package com.spotify.scio.aztables.syntax

import com.azure.data.tables.models.TableEntity
import com.spotify.scio.ScioContext
import com.spotify.scio.annotations.experimental
import com.spotify.scio.aztables.ReadTableStorageIO
import com.spotify.scio.values.SCollection

final class AZTablesScioContextOps(private val sc: ScioContext) extends AnyVal {

  @experimental
  /**
   * Read data from Azure Table Storage
   *
   * Example of the url with credentials:
   *
   * `https://[accountStorage].table.core.windows.net/[tableName]?sp=rau&st=2023-01-17T22:54:15Z&se=2024-01-18T22:54:00Z&spr=https&sv=2021-06-08&sig=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx%2Byyyyyyy%3D&tn=[tablename]`
   *
   * @param endpoint
   *   The endpoint, example: `http://[accountStorage].table.core.windows.net`
   * @param sasToken
   *   The sas token for the table, example:
   *   `sp=rau&st=2023-01-17T22:54:15Z&se=2024-01-18T22:54:00Z&spr=https&sv=2021-06-08&sig=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx%2Byyyyyyy%3D&tn=[tablename]`
   * @param tableName
   *   The table name, example: `[tableName]`
   * @param query
   *   The query example: `Timestamp ge datetime'2022-12-20T00:54:00Z' and Timestamp lt datetime'2022-12-20T00:55:00Z'`
   */
  def readTableStorageWithSas(
    endpoint: String,
    sasToken: String,
    tableName: String,
    query: String = null
  ): SCollection[TableEntity] = sc.read(ReadTableStorageIO(endpoint, sasToken, tableName, query, null))

  @experimental
  /**
   * Read data from Azure Table Storage
   *
   * @param connectionString
   *  The connectionString, example: `DefaultEndpointsProtocol=https;AccountName=[accountName];AccountKey=[key]EndpointSuffix=core.windows.net;`
   * @param tableName
   *   The table name, example: `[tableName]`
   * @param query
   *   The query example: `Timestamp ge datetime'2022-12-20T00:54:00Z' and Timestamp lt datetime'2022-12-20T00:55:00Z'`
   */
  def readTableStorage(
    connectionString: String,
    tableName: String,
    query: String = null
  ): SCollection[TableEntity] = sc.read(ReadTableStorageIO(null, null, tableName, query, connectionString))
}

trait ScioContextSyntax {
  implicit def aztablesScioContextOps(sc: ScioContext): AZTablesScioContextOps =
    new AZTablesScioContextOps(sc)
}
