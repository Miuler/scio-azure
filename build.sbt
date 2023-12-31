/*
 * Copyright 2016 Miuler
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

import Dependencies.*
import sbt.*
import Keys.*
import sbtassembly.AssemblyPlugin.autoImport.*
import com.github.sbt.git.SbtGit.GitKeys.gitRemoteRepo
import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings
import de.heikoseeberger.sbtheader.CommentCreator
import _root_.io.github.davidgregory084.DevMode


ThisBuild / turbo := true


// check version used by beam
// https://github.com/apache/beam/blob/v2.44.0/buildSrc/src/main/groovy/org/apache/beam/gradle/BeamModulePlugin.groovy
// dependent versions

// check versions from libraries-bom
// https://storage.googleapis.com/cloud-opensource-java-dashboard/com.google.cloud/libraries-bom/26.1.5/index.html
val errorProneAnnotationsVersion = "2.16"


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
    organization := "miuler",
    headerLicense := Some(HeaderLicense.ALv2(currentYear.toString, "Spotify AB")),
    headerMappings := headerMappings.value + (HeaderFileType.scala -> keepExistingHeader, HeaderFileType.java -> keepExistingHeader),
    scalaVersion := "2.12.18",
    crossScalaVersions := Seq("2.13.11", scalaVersion.value),

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
        id = "Miuler",
        name = "Hector Miuler Malpica Gallegos",
        email = "miuler@gmail.com",
        url = url("https://miuler.com")
      )
    ),
    //MySettingsDefinition.github,
    MySettingsDefinition.mimaSettings,
    MySettingsDefinition.formatSettings,
    MySettingsDefinition.java17Settings
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
          case "DirectRunner" => `beam-runners-direct`
          case "DataflowRunner" => `beam-runners-dataflow`
          case "SparkRunner" => `beam-runners-spark`
          case "FlinkRunner" => `beam-runners-flink`
          case _ => Nil
        }.toSeq
      }
      .getOrElse(`beam-runners-direct`)
  },
  libraryDependencies ++= beamRunnersEval.value
)


lazy val macroSettings = Def.settings(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies ++= {
    VersionNumber(scalaVersion.value) match {
      case v if v.matchesSemVer(SemanticSelector("2.12.x")) =>
        Seq(compilerPlugin((`paradise`).cross(CrossVersion.full)))
      case _ => Nil
    }
  },
  // see MacroSettings.scala
  scalacOptions += "-Xmacro-settings:cache-implicit-schemas=true"
)



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
  .settings(beamRunnerSettings)
  .settings(
    // scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xsource:3"), // , "-Ymacro-annotations"
    scalacOptions ++= Seq("-Xsource:3"),
    libraryDependencies ++= Seq(
      `scio-core`,
      `azure-cosmos`,
      `bson`,
      `slf4j-api`,

      // TEST
      `scio-test`,
      `scalacheck-test`,
      `testcontainers-scalatest-test`,
      `testcontainers-azure-test`,
      `scribe-test`,
      `scribe-slf4j-test`,
    )
  )

lazy val `scio-aztables`: Project = project
  .in(file("scio-aztables"))
  .configs(IntegrationTest)
  .settings(itSettings)
  .settings(commonSettings)
  .settings(beamRunnerSettings)
  .settings(
    // scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xsource:3"), // , "-Ymacro-annotations"
    scalacOptions ++= Seq("-Xsource:3"),
    libraryDependencies ++= Seq(
      `scio-core`,
      `beam-extensions-kryo`,
      `azure-data-tables`,
      `azure-core`,
      `azure-jackson`,
      `azure-json`,
      `azure-xml`,
      // TEST
      `scio-test`,
      `scribe-test`,
      `scribe-slf4j-test`,
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
    "javadoc.org.apache.beam.base_url" -> s"https://beam.apache.org/releases/javadoc/${Version.beam}",
    "scaladoc.com.spotify.scio.base_url" -> "https://spotify.github.io/scio/api",
    "github.base_url" -> "https://github.com/spotify/scio",
    "extref.example.base_url" -> "https://spotify.github.io/scio/examples/%s.scala.html"
  ),
  Compile / paradoxMaterialTheme := ParadoxMaterialTheme()
    .withFavicon("images/favicon.ico")
    .withColor("white", "indigo")
    .withLogo("images/logo.png")
    .withRepository(uri("https://github.com/miuler/scio-azure"))
    .withSocial(uri("https://github.com/miuler"), uri("https://twitter.com/Miuler")),
  // sbt-site
  addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, ScalaUnidoc / siteSubdirName),
  makeSite / mappings ++= Seq(
    file("scio-examples/target/site/index.html") -> "examples/index.html"
  ) ++ SoccoIndex.mappings,
  makeSite := makeSite.dependsOn(mdoc.toTask("")).value
)


// strict should only be enabled when updating/adding dependencies
// ThisBuild / conflictManager := ConflictManager.strict
// To update this list we need to check against the dependencies being evicted
ThisBuild / dependencyOverrides ++= Seq(
)
