# Scio - Azure

[![Build Status](https://img.shields.io/github/workflow/status/Miuler/scio-azure/ci)](https://github.com/Miuler/scio-azure/actions?query=workflow%3Aci)
[![codecov.io](https://codecov.io/github/Miuler/scio-azure/coverage.svg?branch=master)](https://codecov.io/github/Miuler/scio-azure?branch=master)
[![GitHub license](https://img.shields.io/github/license/spotify/scio.svg)](./LICENSE)

[//]: # ([![Maven Central]&#40;https://img.shields.io/maven-central/v/com.spotify/scio-core_2.12.svg&#41;]&#40;https://github.com/Miuler?tab=packages&repo_name=scio-azure&#41;)

[//]: # ([![Scaladoc]&#40;https://img.shields.io/badge/scaladoc-latest-blue.svg&#41;]&#40;https://spotify.github.io/scio/api/com/spotify/scio/index.html&#41;)

[//]: # ([![Scala Steward badge]&#40;https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=&#41;]&#40;https://scala-steward.org&#41;)

[//]: # ()
[//]: # (<img src="https://raw.github.com/spotify/scio/master/site/src/main/paradox/images/scio.png" alt="Scio Logo" width="250"/>)

[//]: # ()
[//]: # (> Ecclesiastical Latin IPA: /ˈʃi.o/, [ˈʃiː.o], [ˈʃi.i̯o])

[//]: # (> Verb: I can, know, understand, have knowledge.)

[//]: # ()
[//]: # (Scio is a Scala API for [Apache Beam]&#40;http://beam.incubator.apache.org/&#41; and [Google Cloud Dataflow]&#40;https://github.com/GoogleCloudPlatform/DataflowJavaSDK&#41; inspired by [Apache Spark]&#40;http://spark.apache.org/&#41; and [Scalding]&#40;https://github.com/twitter/scalding&#41;.)

[//]: # ()
[//]: # (Scio 0.3.0 and future versions depend on Apache Beam &#40;`org.apache.beam`&#41; while earlier versions depend on Google Cloud Dataflow SDK &#40;`com.google.cloud.dataflow`&#41;. See this [page]&#40;https://spotify.github.io/scio/Apache-Beam.html&#41; for a list of breaking changes.)

[//]: # ()
[//]: # (# Features)

[//]: # ()
[//]: # (- Scala API close to that of Spark and Scalding core APIs)

[//]: # (- Unified batch and streaming programming model)

[//]: # (- Fully managed service<sup>\*</sup>)

[//]: # (- Integration with Google Cloud products: Cloud Storage, BigQuery, Pub/Sub, Datastore, Bigtable)

[//]: # (- JDBC, [TensorFlow]&#40;http://tensorflow.org/&#41; TFRecords, Cassandra, Elasticsearch and Parquet I/O)

[//]: # (- Interactive mode with Scio REPL)

[//]: # (- Type safe BigQuery)

[//]: # (- Integration with [Algebird]&#40;https://github.com/twitter/algebird&#41; and [Breeze]&#40;https://github.com/scalanlp/breeze&#41;)

[//]: # (- Pipeline orchestration with [Scala Futures]&#40;http://docs.scala-lang.org/overviews/core/futures.html&#41;)

[//]: # (- Distributed cache)

[//]: # ()
[//]: # (<sup>\*</sup> provided by Google Cloud Dataflow)

[//]: # ()
[//]: # (# Quick Start)

[//]: # ()
[//]: # (Download and install the [Java Development Kit &#40;JDK&#41;]&#40;https://adoptopenjdk.net/index.html&#41; version 8.)

[//]: # ()
[//]: # (Install [sbt]&#40;https://www.scala-sbt.org/1.x/docs/Setup.html&#41;.)

[//]: # ()
[//]: # (Use our [giter8 template]&#40;https://github.com/spotify/scio.g8&#41; to quickly create a new Scio job repository:)

[//]: # ()
[//]: # (`sbt new spotify/scio.g8`)

[//]: # ()
[//]: # (Switch to the new repo &#40;default `scio-job`&#41; and build it:)

[//]: # ()
[//]: # (```)

[//]: # (cd scio-job)

[//]: # (sbt stage)

[//]: # (```)

[//]: # ()
[//]: # (Run the included word count example:)

[//]: # ()
[//]: # (`target/universal/stage/bin/scio-job --output=wc`)

[//]: # ()
[//]: # (List result files and inspect content:)

[//]: # ()
[//]: # (```)

[//]: # (ls -l wc)

[//]: # (cat wc/part-00000-of-00004.txt)

[//]: # (```)

[//]: # ()
[//]: # (# Documentation)

[//]: # ()
[//]: # ([Getting Started]&#40;https://spotify.github.io/scio/Getting-Started.html&#41; is the best place to start with Scio. If you are new to Apache Beam and distributed data processing, check out the [Beam Programming Guide]&#40;https://beam.apache.org/documentation/programming-guide/&#41; first for a detailed explanation of the Beam programming model and concepts. If you have experience with other Scala data processing libraries, check out this comparison between [Scio, Scalding and Spark]&#40;https://spotify.github.io/scio/Scio,-Scalding-and-Spark.html&#41;. Finally check out this document about the relationship between [Scio, Beam and Dataflow]&#40;https://spotify.github.io/scio/Scio,-Beam-and-Dataflow.html&#41;.)

[//]: # ()
[//]: # (Example Scio pipelines and tests can be found under [scio-examples]&#40;https://github.com/spotify/scio/tree/master/scio-examples/src&#41;. A lot of them are direct ports from Beam's Java [examples]&#40;https://github.com/apache/beam/tree/master/examples&#41;. See this [page]&#40;http://spotify.github.io/scio/examples/&#41; for some of them with side-by-side explanation. Also see [Big Data Rosetta Code]&#40;https://github.com/spotify/big-data-rosetta-code&#41; for common data processing code snippets in Scio, Scalding and Spark.)

[//]: # ()
[//]: # (- [Scio Docs]&#40;https://spotify.github.io/scio/&#41; - main documentation site)

[//]: # (- [Scio Scaladocs]&#40;http://spotify.github.io/scio/api/&#41; - current API documentation)

[//]: # (- [Scio Examples]&#40;http://spotify.github.io/scio/examples/&#41; - examples with side-by-side explanation)

[//]: # ()
[//]: # (# Artifacts)

[//]: # ()
[//]: # (Scio includes the following artifacts:)

[//]: # ()
[//]: # (- `scio-core`: core library)

[//]: # (- `scio-test`: test utilities, add to your project as a "test" dependency)

[//]: # (- `scio-avro`: add-on for Avro, can also be used standalone)

[//]: # (- `scio-google-cloud-platform`: add-on for Google Cloud IO's: BigQuery, Bigtable, Pub/Sub, Datastore, Spanner)

[//]: # (- `scio-cassandra*`: add-ons for Cassandra)

[//]: # (- `scio-elasticsearch*`: add-ons for Elasticsearch)

[//]: # (- `scio-extra`: extra utilities for working with collections, Breeze, etc., best effort support)

[//]: # (- `scio-jdbc`: add-on for JDBC IO)

[//]: # (- `scio-neo4j`: add-on for Neo4J IO)

[//]: # (- `scio-parquet`: add-on for Parquet)

[//]: # (- `scio-tensorflow`: add-on for TensorFlow TFRecords IO and prediction)

[//]: # (- `scio-redis`: add-on for Redis)

[//]: # (- `scio-smb`: add-on for Sort Merge Bucket operations)

[//]: # (- `scio-repl`: extension of the Scala REPL with Scio specific operations)

[//]: # ()
[//]: # (# License)

[//]: # ()
[//]: # (Copyright 2022 Miuler.)

[//]: # ()
[//]: # (Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0)
