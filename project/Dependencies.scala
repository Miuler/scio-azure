import sbt.*
import sbtprotoc.ProtocPlugin.autoImport.AsProtocPlugin

import scala.collection.immutable.Seq

object Dependencies {

  object Version {
    val beam = "2.44.0"
    val scio = "0.13.1"
    val spark = "3.1.2"
    val flink = "1.15.0"
    val azureCore = "1.37.0"
    val azureJsonXml = "1.0.0-beta.1"
    val azureJackson = "1.4.2"
    val azureDataTables = "12.3.13"
    val grpc = "1.53.0"
    val protobuf = "3.22.2"
    val socco = "0.1.7"
    val scalaMacros = "2.1.1"
    val bson = "4.10.2"
    val cosmosdb = "4.48.0"
    val slf4j = "1.7.30"
    val scribe = "3.11.8"
    val testContainerAzure = "1.18.3"
    val scalacheck = "1.17.0"
    val testContainers = "0.40.12"
  }

  lazy val `beam-runners-direct` = Seq("org.apache.beam" % "beam-runners-direct-java" % Version.beam % Runtime)
  lazy val `beam-runners-dataflow` = Seq("org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % Version.beam)
  // only available for scala 2.12
  // scala 2.13 is supported from spark 3.2.0
  lazy val `beam-runners-spark` = Seq(
    "org.apache.beam" % "beam-runners-spark" % Version.beam % Runtime,
    "org.apache.spark" %% "spark-core" % Version.spark % Runtime,
    "org.apache.spark" %% "spark-streaming" % Version.spark % Runtime
  )
  lazy val `beam-runners-flink` = Seq(
    "org.apache.beam" % "beam-runners-flink-1.15" % Version.beam % Runtime,
    "org.apache.flink" % "flink-clients" % Version.flink % Runtime,
    "org.apache.flink" % "flink-streaming-java" % Version.flink % Runtime
  )

  val `scio-core` = "com.spotify" %% "scio-core" % Version.scio
  val `beam-extensions-kryo` = "org.apache.beam" % "beam-sdks-java-extensions-kryo" % Version.beam
  val `azure-data-tables` = "com.azure" % "azure-data-tables" % Version.azureDataTables
  val `azure-core` = "com.azure" % "azure-core" % Version.azureCore
  val `azure-jackson` = "com.azure" % "azure-core-serializer-json-jackson" % Version.azureJackson
  val `azure-json` = "com.azure" % "azure-json" % Version.azureJsonXml
  val `azure-xml` = "com.azure" % "azure-xml" % Version.azureJsonXml
  val `protoc-gen-grpc` = "io.grpc" % "protoc-gen-grpc-java" % Version.grpc asProtocPlugin ()
  val `protoc-java` = "com.google.protobuf" % "protobuf-java" % Version.protobuf % "protobuf"
  val `socco-ng` = "io.regadas" %% "socco-ng" % Version.socco
  val `paradise` = "org.scalamacros" % "paradise" % Version.scalaMacros
  val `azure-cosmos` = "com.azure" % "azure-cosmos" % Version.cosmosdb
  val `bson` = "org.mongodb" % "bson" % Version.bson
  val `slf4j-api` = "org.slf4j" % "slf4j-api" % Version.slf4j

  val `scio-test` = "com.spotify" %% "scio-test" % Version.scio % "test;it"
  val `scribe-test` = "com.outr" %% "scribe" % Version.scribe % "it,test"
  val `scribe-slf4j-test` = "com.outr" %% "scribe-slf4j" % Version.scribe % "it,test"
  val `scalacheck-test` = "org.scalacheck" %% "scalacheck" % Version.scalacheck % "test,it"
  val `testcontainers-azure-test` = "org.testcontainers" % "azure" % Version.testContainerAzure % IntegrationTest
  val `testcontainers-scalatest-test` =
    "com.dimafeng" %% "testcontainers-scala-scalatest" % Version.testContainers % IntegrationTest

}
