import com.lightbend.sbt.JavaFormatterPlugin.autoImport.javafmtOnCompile
import com.typesafe.tools.mima.plugin.MimaKeys.*
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.*
import sbt.Keys.*
//import sbtghpackages.*
//import sbtghpackages.GitHubPackagesKeys.*

object MySettingsDefinition {
  lazy val mimaSettings = Def.settings(
    mimaPreviousArtifacts :=
      previousVersion(version.value)
        .filter(_ => publishArtifact.value)
        .map(organization.value % s"${normalizedName.value}_${scalaBinaryVersion.value}" % _)
        .toSet
  )

  // Example: https://maven.pkg.github.com/Miuler/scio-azure/miuler/scio-aztables_2.13/1.0.0/scio-aztables_2.13-1.0.0.jar
  //lazy val github = Def.settings(
  //  githubOwner := "Miuler",
  //  githubRepository := "scio-azure",
  //  githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
  //  publishTo := githubPublishTo.value
  //)

  lazy val formatSettings = Def.settings(scalafmtOnCompile := false, javafmtOnCompile := false)

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




  private def previousVersion(currentVersion: String): Option[String] = {
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
}
