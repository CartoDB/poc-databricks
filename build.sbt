import de.heikoseeberger.sbtheader._
import java.time.Year

val scalaVersions = Seq("2.12.15")

lazy val commonSettings = Seq(
  scalaVersion       := scalaVersions.head,
  crossScalaVersions := scalaVersions,
  organization       := "com.carto.analyticstoolbox",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature",
    "-target:jvm-1.8" // ,
    // "-Xsource:3"
  ),
  licenses               := Seq("BSD-3-Clause" -> url("https://github.com/CartoDB/analytics-toolbox-databricks/blob/master/LICENSE")),
  homepage               := Some(url("https://github.com/CartoDB/analytics-toolbox-databricks")),
  versionScheme          := Some("semver-spec"),
  Test / publishArtifact := false,
  developers := List(
    Developer(
      "pomadchin",
      "Grigory Pomadchin",
      "@pomadchin",
      url("https://github.com/pomadchin")
    )
  ),
  headerMappings := Map(
    FileType.scala -> CommentStyle.cStyleBlockComment.copy(
      commentCreator = { (text, existingText) =>
        // preserve year of old headers
        val newText = CommentStyle.cStyleBlockComment.commentCreator.apply(text, existingText)
        existingText.flatMap(_ => existingText.map(_.trim)).getOrElse(newText)
      }
    )
  ),
  resolvers += "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  sonatypeProfileName := "com.carto",
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(name := "analyticstoolbox")
  .settings(
    scalaVersion       := scalaVersions.head,
    crossScalaVersions := Nil,
    publish            := {},
    publishLocal       := {}
  )
  .aggregate(core)

lazy val core = project
  .settings(commonSettings)
  .settings(name := "core")
  .settings(
    libraryDependencies ++= Seq(
      "com.azavea"               %% "hiveless-core"     % "0.0.0+50-2380a4be-SNAPSHOT",
      "org.locationtech.geomesa" %% "geomesa-spark-jts" % "3.3.0",
      "org.apache.spark"         %% "spark-hive"        % "3.1.2"  % Provided,
      "org.scalatest"            %% "scalatest"         % "3.2.10" % Test
    ),
    headerLicense := Some(HeaderLicense.ALv2(Year.now.getValue.toString, "Azavea")),
    assembly / test := {},
    assembly / assemblyShadeRules := {
      val shadePackage = "com.carto.analytics.hiveless"
      Seq(
        ShadeRule.rename("shapeless.**" -> s"$shadePackage.shapeless.@1").inAll,
        ShadeRule.rename("cats.kernel.**" -> s"$shadePackage.cats.kernel.@1").inAll
      )
    },
    assembly / assemblyMergeStrategy := {
      case s if s.startsWith("META-INF/services")           => MergeStrategy.concat
      case "reference.conf" | "application.conf"            => MergeStrategy.concat
      case "META-INF/MANIFEST.MF" | "META-INF\\MANIFEST.MF" => MergeStrategy.discard
      case "META-INF/ECLIPSEF.RSA" | "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
      case _                                                => MergeStrategy.first
    }
  )
