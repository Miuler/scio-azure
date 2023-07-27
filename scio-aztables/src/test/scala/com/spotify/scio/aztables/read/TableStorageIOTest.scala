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

package com.spotify.scio.aztables.read

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.spotify.scio.aztables.Utils.initLog
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class TableStorageIOTest extends AnyFeatureSpec with Matchers {
  initLog()
  val mapper = new ObjectMapper()
  mapper.registerModule(new JavaTimeModule())

  Feature("Read Table Storage") {
    Scenario("Read Current using token sas") {
      val endpoint = "https://scmimovistardatacert.table.core.windows.net"
      val sasToken =
        "sp=rau&st=2023-01-17T22:54:15Z&se=2024-01-18T22:54:00Z&spr=https&sv=2021-06-08&sig=4AYA2B3nJGovEvys3mqdfmTfNBRM7BClY6K%2BeFzlZcw%3D&tn=LogCheckout122022"
      val tableName = "LogCheckout122022"
      val boundedSource = new TableStorageBoundedSource(endpoint, sasToken, tableName, null, null)
      val boundedReader = boundedSource.createReader(null)

      {
        boundedReader.start() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      {
        boundedReader.advance() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      boundedReader.close()
    }

    Scenario("Read Curren using key connection") {
      val endpoint =
        "DefaultEndpointsProtocol=https;AccountName=scmimovistardatacert;AccountKey=f0J/dZpPF6UDQTv8GRRkuh3r92dt3ss4lNzrmWuXDZ8y55Bvt68ZFzzcbJ0wZjegrNQdOEekqh9/gIx+ibXhaQ==;EndpointSuffix=core.windows.net;"
      val tableName = "LogCheckout122022"
      val boundedSource = new TableStorageBoundedSource(endpoint, tableName)
      val boundedReader = boundedSource.createReader(null)

      {
        boundedReader.start() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      {
        boundedReader.advance() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      boundedReader.close()
    }

    Scenario("Read Curren using key connection and filter") {
      val endpoint =
        "DefaultEndpointsProtocol=https;AccountName=scmimovistardatacert;AccountKey=f0J/dZpPF6UDQTv8GRRkuh3r92dt3ss4lNzrmWuXDZ8y55Bvt68ZFzzcbJ0wZjegrNQdOEekqh9/gIx+ibXhaQ==;EndpointSuffix=core.windows.net;"
      val tableName = "LogCheckout122022"
      val query =
        s"Timestamp ge datetime'2022-12-20T00:54:00Z' and Timestamp lt datetime'2022-12-20T00:55:00Z'"
      val boundedSource = new TableStorageBoundedSource(endpoint, tableName, query)
      val boundedReader = boundedSource.createReader(null)

      {
        boundedReader.start() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      {
        boundedReader.advance() should be(true)
        scribe.info(s"tableEntity: ${mapper.writeValueAsString(boundedReader.getCurrent)}")
      }

      boundedReader.close()
    }
  }

}
