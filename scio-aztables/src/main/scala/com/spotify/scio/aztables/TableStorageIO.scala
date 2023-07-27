/*
 * Copyright 2023 Spotify AB
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

package com.spotify.scio.aztables

import com.azure.data.tables.models.TableEntity
import com.spotify.scio.ScioContext
import com.spotify.scio.aztables.read.TableStorageRead
import com.spotify.scio.io.*
import com.spotify.scio.values.SCollection

trait TableStorageIO[T] extends ScioIO[T] {}

case class ReadTableStorageIO(
  endpoint: String,
  sasToken: String,
  tableName: String,
  query: String,
  connectionString: String
) extends TableStorageIO[TableEntity] {
  override type ReadP = Unit
  override type WriteP = Nothing
  override val tapT: TapT.Aux[TableEntity, Nothing] = EmptyTapOf[TableEntity]

  override protected def read(sc: ScioContext, params: ReadP): SCollection[TableEntity] =
    sc.applyTransform(TableStorageRead(endpoint, sasToken, tableName, query, connectionString))

  override protected def write(data: SCollection[TableEntity], params: WriteP): Tap[Nothing] =
    throw new UnsupportedOperationException("ReadTableStorageIO is read-only")

  /**
   * Write options also return a `ClosedTap`. Once the job completes you can open the `Tap`. Tap
   * abstracts away the logic of reading the dataset directly as an Iterator[T] or re-opening it in
   * another ScioContext. The Future is complete once the job finishes. This can be used to do light
   * weight pipeline orchestration e.g. WordCountOrchestration.scala.
   */
  override def tap(read: ReadP): Tap[Nothing] = EmptyTap
}
