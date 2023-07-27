/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import com.github.sbt.git.SbtGit.GitKeys.gitRemoteRepo
import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings
import de.heikoseeberger.sbtheader.CommentCreator
import _root_.io.github.davidgregory084.DevMode

ThisBuild / turbo := true

val beamVendorVersion = "0.1"
val beamVersion = "2.44.0"

// check version used by beam
// https://github.com/apache/beam/blob/v2.44.0/buildSrc/src/main/groovy/org/apache/beam/gradle/BeamModulePlugin.groovy
val flinkVersion = "1.15.0"
val slf4jVersion = "1.7.30"
val sparkVersion = "3.1.2"
// dependent versions

// check versions from libraries-bom
// https://storage.googleapis.com/cloud-opensource-java-dashboard/com.google.cloud/libraries-bom/26.1.5/index.html
val errorProneAnnotationsVersion = "2.16"
val grpcVersion = "1.50.2"
val protobufVersion = "3.21.9"

val azureCoreVersion = "1.35.0"
val azureDataTablesVersion = "12.3.6"
val azureJacksonVersion = "1.2.25"
val azureJsonXmlVersion = "1.0.0-beta.1"
val bsonVersion = "4.8.1"
val cosmosVersion = "4.37.1"
val cosmosContainerVersion = "1.17.5"
val kantanCsvVersion = "0.7.0"
val scalacheckVersion = "1.17.0"
val scalaMacrosVersion = "2.1.1"
val scribeVersion = "3.10.7"
val testContainersVersion = "0.40.12"

ThisBuild / tpolecatDefaultOptionsMode := DevMode
ThisBuild / tpolecatDevModeOptions ~= { opts =>
  val excludes = Set(
    ScalacOptions.lintPackageObjectClasses,
    ScalacOptions.privateWarnDeadCode,
    ScalacOptions.privateWarnValueDiscard,
    ScalacOptions.warnDeadCode,
    ScalacOptions.warnValueDiscard
  )

  val extras = Set(
    Scalac.delambdafyInlineOption,
    Scalac.macroAnnotationsOption,
    Scalac.macroSettingsOption,
    Scalac.maxClassfileName,
    Scalac.privateBackendParallelism,
    Scalac.privateWarnMacrosOption,
    Scalac.release8,
    Scalac.targetOption,
    Scalac.warnConfOption,
    Scalac.warnMacrosOption
  )

  opts.filterNot(excludes).union(extras)
}

ThisBuild / doc / tpolecatDevModeOptions ++= Set(Scalac.docNoJavaCommentOption)

ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
val excludeLint = SettingKey[Set[Def.KeyedInitialize[_]]]("excludeLintKeys")
Global / excludeLint := (Global / excludeLint).?.value.getOrElse(Set.empty)
Global / excludeLint += sonatypeProfileName
Global / excludeLint += site / Paradox / sourceManaged

def previousVersion(currentVersion: String): Option[String] = {
  val Version =
    """(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)(?<preRelease>-.*)?(?<build>\+.*)?""".r
  currentVersion match {
    case Version(x, y, z, null, null) if z != "0" =>
      // patch release
      Some(s"$x.$y.${z.toInt - 1}")
    case Version(x, y, z, null, _) =>
      // post release build
      Some(s"$x.$y.$z")
    case Version(x, y, z, _, _) if z != "0" =>
      // patch pre-release
      Some(s"$x.$y.${z.toInt - 1}")
    case _ =>
      None
  }
}

lazy val mimaSettings = Def.settings(
  mimaPreviousArtifacts :=
    previousVersion(version.value)
      .filter(_ => publishArtifact.value)
      .map(organization.value % s"${normalizedName.value}_${scalaBinaryVersion.value}" % _)
      .toSet
)

lazy val formatSettings = Def.settings(scalafmtOnCompile := false, javafmtOnCompile := false)

lazy val currentYear = java.time.LocalDate.now().getYear
lazy val keepExistingHeader =
  HeaderCommentStyle.cStyleBlockComment.copy(commentCreator = new CommentCreator() {
    override def apply(text: String, existingText: Option[String]): String =
      existingText
        .getOrElse(HeaderCommentStyle.cStyleBlockComment.commentCreator(text))
        .trim()
  })

val commonSettings = Def
  .settings(
    organization := "com.spotify",
    headerLicense := Some(HeaderLicense.ALv2(currentYear.toString, "Spotify AB")),
    headerMappings := headerMappings.value + (HeaderFileType.scala -> keepExistingHeader, HeaderFileType.java -> keepExistingHeader),
    scalaVersion := "2.13.8",
    crossScalaVersions := Seq("2.12.17", scalaVersion.value),
    // this setting is not derived in sbt-tpolecat
    // https://github.com/typelevel/sbt-tpolecat/issues/36
    inTask(doc)(TpolecatPlugin.projectSettings),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked"),
    Compile / doc / javacOptions := Seq("-source", "1.8"),
    excludeDependencies ++= Seq("org.apache.beam" % "beam-sdks-java-io-kafka"),
    resolvers ++= Resolver.sonatypeOssRepos("public"),
    Test / javaOptions += "-Dscio.ignoreVersionWarning=true",
    Test / testOptions += Tests.Argument("-oD"),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-a"),
    testOptions ++= {
      if (sys.env.contains("SLOW")) {
        Nil
      } else {
        Seq(Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.scalatest.tags.Slow"))
      }
    },
    coverageExcludedPackages := (Seq(
      "com\\.spotify\\.scio\\.examples\\..*",
    ) ++ (2 to 10).map(x => s"com\\.spotify\\.scio\\.sql\\.Query$x")).mkString(";"),
    coverageHighlighting := true,
    licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/Miuler/scio-azure")),
    scmInfo := Some(ScmInfo(url("https://github.com/Miuler/scio-azure"), "scm:git:git@github.com:Miuler/scio-azure.git")),
    developers := List(
      Developer(
        id = "miuler",
        name = "Hector Miuler Malpica Gallegos",
        email = "miuler@gmail.com",
        url = url("https://miuler.com")
      )
    ),
    mimaSettings,
    formatSettings,
    java17Settings
  )

lazy val publishSettings = Def.settings(
  // Release settings
  sonatypeProfileName := "com.spotify"
)

lazy val itSettings = Defaults.itSettings ++ inConfig(IntegrationTest)(
  Def.settings(
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    // exclude all sources if we don't have GCP credentials
    unmanagedSources / excludeFilter := {
      if (BuildCredentials.exists) {
        HiddenFileFilter
      } else {
        HiddenFileFilter || "*.scala"
      }
    },
    run / fork := true,
    scalafmtConfigSettings,
    scalafixConfigSettings(IntegrationTest)
  )
)

lazy val assemblySettings = Seq(
  assembly / test := {},
  assembly / assemblyMergeStrategy ~= { old =>
  {
    case PathList("dev", "ludovic", "netlib", "InstanceBuilder.class") =>
      // arbitrary pick last conflicting InstanceBuilder
      MergeStrategy.last
    case s if s.endsWith(".proto") =>
      // arbitrary pick last conflicting proto file
      MergeStrategy.last
    case PathList("git.properties") =>
      // drop conflicting git properties
      MergeStrategy.discard
    case PathList("META-INF", "versions", "9", "module-info.class") =>
      // drop conflicting module-info.class
      MergeStrategy.discard
    case PathList("META-INF", "gradle", "incremental.annotation.processors") =>
      // drop conflicting kotlin compiler info
      MergeStrategy.discard
    case PathList("META-INF", "io.netty.versions.properties") =>
      // merge conflicting netty property files
      MergeStrategy.filterDistinctLines
    case PathList("META-INF", "native-image", "native-image.properties") =>
      // merge conflicting native-image property files
      MergeStrategy.filterDistinctLines
    case s => old(s)
  }
  }
)

lazy val macroSettings = Def.settings(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies ++= {
    VersionNumber(scalaVersion.value) match {
      case v if v.matchesSemVer(SemanticSelector("2.12.x")) =>
        Seq(compilerPlugin(("org.scalamacros" % "paradise" % scalaMacrosVersion).cross(CrossVersion.full)))
      case _ => Nil
    }
  },
  // see MacroSettings.scala
  scalacOptions += "-Xmacro-settings:cache-implicit-schemas=true"
)

lazy val directRunnerDependencies = Seq("org.apache.beam" % "beam-runners-direct-java" % beamVersion % Runtime)
lazy val dataflowRunnerDependencies = Seq(
  "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion % Runtime
)

// only available for scala 2.12
// scala 2.13 is supported from spark 3.2.0
lazy val sparkRunnerDependencies = Seq(
  "org.apache.beam" % "beam-runners-spark" % beamVersion % Runtime,
  "org.apache.spark" %% "spark-core" % sparkVersion % Runtime,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Runtime
)

lazy val flinkRunnerDependencies = Seq(
  "org.apache.beam" % "beam-runners-flink-1.15" % beamVersion % Runtime,
  "org.apache.flink" % "flink-clients" % flinkVersion % Runtime,
  "org.apache.flink" % "flink-streaming-java" % flinkVersion % Runtime
)
lazy val beamRunners = settingKey[String]("beam runners")
lazy val beamRunnersEval = settingKey[Seq[ModuleID]]("beam runners")

def beamRunnerSettings: Seq[Setting[_]] = Seq(
  beamRunners := "",
  beamRunnersEval := {
    Option(beamRunners.value)
      .filter(_.nonEmpty)
      .orElse(sys.props.get("beamRunners"))
      .orElse(sys.env.get("BEAM_RUNNERS"))
      .map(_.split(","))
      .map {
        _.flatMap {
          case "DirectRunner"   => directRunnerDependencies
          case "DataflowRunner" => dataflowRunnerDependencies
          case "SparkRunner"    => sparkRunnerDependencies
          case "FlinkRunner"    => flinkRunnerDependencies
          case _                => Nil
        }.toSeq
      }
      .getOrElse(directRunnerDependencies)
  },
  libraryDependencies ++= beamRunnersEval.value
)

ThisBuild / PB.protocVersion := protobufVersion
lazy val scopedProtobufSettings = Def.settings(
  PB.targets := Seq(
    PB.gens.java -> (ThisScope.copy(config = Zero) / sourceManaged).value /
      "compiled_proto" /
      configuration.value.name,
    PB.gens.plugin("grpc-java") -> (ThisScope.copy(config = Zero) / sourceManaged).value /
      "compiled_grpc" /
      configuration.value.name
  ),
  managedSourceDirectories ++= PB.targets.value.map(_.outputPath)
)

lazy val protobufSettings = Def.settings(
  libraryDependencies ++= Seq(
    "io.grpc" % "protoc-gen-grpc-java" % grpcVersion asProtocPlugin (),
    "com.google.protobuf" % "protobuf-java" % protobufVersion % "protobuf"
  )
) ++ Seq(Compile, Test).flatMap(c => inConfig(c)(scopedProtobufSettings))

def splitTests(tests: Seq[TestDefinition], filter: Seq[String], forkOptions: ForkOptions) = {
  val (filtered, default) = tests.partition(test => filter.contains(test.name))
  val policy = Tests.SubProcess(forkOptions)
  new Tests.Group(name = "<default>", tests = default, runPolicy = policy) +: filtered.map { test =>
    new Tests.Group(name = test.name, tests = Seq(test), runPolicy = policy)
  }
}

lazy val java17Settings = sys.props("java.version") match {
  case v if v.startsWith("17.") =>
    Seq(
      Test / fork := true,
      Test / javaOptions ++= Seq(
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.invoke=ALL-UNNAMED"
      )
    )
  case _ => Seq()
}

lazy val root: Project = Project("scio-azure", file("."))
  .settings(commonSettings)
  .settings(
    publish / skip := true,
    mimaPreviousArtifacts := Set.empty,
    assembly / aggregate := false
  )
  .aggregate(
    `scio-cosmosdb`,
    `scio-aztables`,
  )

lazy val `scio-cosmosdb`: Project = project
  .in(file("scio-cosmosdb"))
  .configs(IntegrationTest)
  .settings(itSettings)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    // scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xsource:3"), // , "-Ymacro-annotations"
    scalacOptions ++= Seq("-Xsource:3"),
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % "0.13.1",
      "com.azure" % "azure-cosmos" % cosmosVersion,
      "org.mongodb" % "bson" % bsonVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      // TEST
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test,it",
      "com.spotify" %% "scio-test" % "0.13.1" % "test;it",
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion % "it",
      "org.testcontainers" % "azure" % cosmosContainerVersion % IntegrationTest,
      "com.outr" %% "scribe" % scribeVersion % IntegrationTest,
      "com.outr" %% "scribe-slf4j" % scribeVersion % IntegrationTest
    )
  )

lazy val `scio-aztables`: Project = project
  .in(file("scio-aztables"))
  .configs(IntegrationTest)
  .settings(itSettings)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    // scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xsource:3"), // , "-Ymacro-annotations"
    scalacOptions ++= Seq("-Xsource:3"),
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % "0.13.1",
      "org.apache.beam" % "beam-sdks-java-extensions-kryo" % beamVersion,
      "com.azure" % "azure-data-tables" % azureDataTablesVersion,
      "com.azure" % "azure-core" % azureCoreVersion,
      "com.azure" % "azure-core-serializer-json-jackson" % azureJacksonVersion,
      "com.azure" % "azure-json" % azureJsonXmlVersion,
      "com.azure" % "azure-xml" % azureJsonXmlVersion,
      // "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion,

      "com.spotify" %% "scio-test" % "0.13.1" % "test;it",
      "com.outr" %% "scribe" % scribeVersion % "it,test",
      "com.outr" %% "scribe-slf4j" % scribeVersion % "it,test"
    )
  )


lazy val site: Project = project
  .in(file("site"))
  .enablePlugins(
    ParadoxSitePlugin,
    ParadoxMaterialThemePlugin,
    GhpagesPlugin,
    ScalaUnidocPlugin,
    SiteScaladocPlugin,
    MdocPlugin
  )
  .dependsOn(
    `scio-cosmosdb`,
    //`scio-test` % "compile->test",
  )
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(siteSettings)

// =======================================================================
// Site settings
// =======================================================================

// ScalaDoc links look like http://site/index.html#my.package.MyClass while JavaDoc links look
// like http://site/my/package/MyClass.html. Therefore we need to fix links to external JavaDoc
// generated by ScalaDoc.
def fixJavaDocLinks(bases: Seq[String], doc: String): String =
  bases.foldLeft(doc) { (d, base) =>
    val regex = s"""\"($base)#([^"]*)\"""".r
    regex.replaceAllIn(
      d,
      m => {
        val b = base.replaceAll("/index.html$", "")
        val c = m.group(2).replace(".", "/")
        s"$b/$c.html"
      }
    )
  }

lazy val soccoIndex = taskKey[File]("Generates examples/index.html")


lazy val siteSettings = Def.settings(
  publish / skip := true,
  description := "Scio - Documentation",
  autoAPIMappings := true,
  gitRemoteRepo := "git@github.com:spotify/scio.git",
  libraryDependencies ++= Seq(
    "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
    "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
    "com.nrinaudo" %% "kantan.csv" % kantanCsvVersion
  ),
  // unidoc
  ScalaUnidoc / siteSubdirName := "api",
  ScalaUnidoc / scalacOptions := Seq.empty,
  ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
    `scio-cosmosdb`
  ),
  // unidoc handles class paths differently than compile and may give older
  // versions high precedence.
  ScalaUnidoc / unidoc / unidocAllClasspaths := (ScalaUnidoc / unidoc / unidocAllClasspaths).value
    .map { cp =>
      cp.filterNot(_.data.getCanonicalPath.matches(""".*guava-11\..*"""))
        .filterNot(_.data.getCanonicalPath.matches(""".*bigtable-client-core-0\..*"""))
    },
  // mdoc
  // pre-compile md using mdoc
  mdocIn := (paradox / sourceDirectory).value,
  mdocExtraArguments ++= Seq("--no-link-hygiene"),
  // paradox
  paradox / sourceManaged := mdocOut.value,
  paradoxProperties ++= Map(
    "javadoc.com.spotify.scio.base_url" -> "http://spotify.github.com/scio/api",
    "javadoc.org.apache.beam.sdk.extensions.smb.base_url" ->
      "https://spotify.github.io/scio/api/org/apache/beam/sdk/extensions/smb",
    "javadoc.org.apache.beam.base_url" -> s"https://beam.apache.org/releases/javadoc/$beamVersion",
    "scaladoc.com.spotify.scio.base_url" -> "https://spotify.github.io/scio/api",
    "github.base_url" -> "https://github.com/spotify/scio",
    "extref.example.base_url" -> "https://spotify.github.io/scio/examples/%s.scala.html"
  ),
  Compile / paradoxMaterialTheme := ParadoxMaterialTheme()
    .withFavicon("images/favicon.ico")
    .withColor("white", "indigo")
    .withLogo("images/logo.png")
    .withCopyright("Copyright (C) 2020 Spotify AB")
    .withRepository(uri("https://github.com/spotify/scio"))
    .withSocial(uri("https://github.com/spotify"), uri("https://twitter.com/spotifyeng")),
  // sbt-site
  addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, ScalaUnidoc / siteSubdirName),
  makeSite / mappings ++= Seq(
    file("scio-examples/target/site/index.html") -> "examples/index.html"
  ) ++ SoccoIndex.mappings,
  makeSite := makeSite.dependsOn(mdoc.toTask("")).value
)

lazy val soccoSettings = if (sys.env.contains("SOCCO")) {
  Seq(
    scalacOptions ++= Seq(
      "-P:socco:out:scio-examples/target/site",
      "-P:socco:package_com.spotify.scio:https://spotify.github.io/scio/api"
    ),
    autoCompilerPlugins := true,
    addCompilerPlugin(("io.regadas" %% "socco-ng" % "0.1.7").cross(CrossVersion.full)),
    // Generate scio-examples/target/site/index.html
    soccoIndex := SoccoIndex.generate(target.value / "site" / "index.html"),
    Compile / compile := {
      val _ = soccoIndex.value
      (Compile / compile).value
    }
  )
} else {
  Nil
}

// strict should only be enabled when updating/adding dependencies
// ThisBuild / conflictManager := ConflictManager.strict
// To update this list we need to check against the dependencies being evicted
ThisBuild / dependencyOverrides ++= Seq(
)
