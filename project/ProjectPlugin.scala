import higherkindness.mu.rpc.srcgen.SrcGenPlugin.autoImport._
import higherkindness.mu.rpc.srcgen.Model._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt.Keys._
import sbt._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      val catsEffect     = "2.2.0"
      val log4cats       = "1.1.1"
      val logbackClassic = "1.2.3"
      val mu             = "0.21.3"
      val pureconfig     = "0.13.0"
      val circeVersion   = "0.13.0"
      val scala          = "2.13.1"
      val fs2PubSub      = "0.16.0"
    }
  }

  import autoImport._

  private lazy val codeGenSettings: Seq[Def.Setting[_]] = Seq(
    muSrcGenIdlType := IdlType.Proto,
    muSrcGenIdiomaticEndpoints := true,
    muSrcGenJarNames := Seq("mu-smart-home-protocol"),
    sourceGenerators in Compile += (muSrcGen in Compile).taskValue
  )

  private lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback"     % "logback-classic" % V.logbackClassic,
      "io.chrisdavenport" %% "log4cats-core"   % V.log4cats,
      "io.chrisdavenport" %% "log4cats-slf4j"  % V.log4cats
    )
  )

  lazy val noPublishSettings: Seq[Def.Setting[_]] = Seq(
    publish := ((): Unit),
    publishLocal := ((): Unit),
    publishArtifact := false
  )

  lazy val protocolSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    crossPaths := false,
    libraryDependencies := Nil
  )

  lazy val sharedSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect" % V.catsEffect,
      "com.github.pureconfig" %% "pureconfig"  % V.pureconfig
    )
  )

  lazy val serverSettings: Seq[Def.Setting[_]] = commonSettings ++
    codeGenSettings ++
    Seq(
      libraryDependencies ++= Seq(
        "com.permutive"     %% "fs2-google-pubsub-grpc" % V.fs2PubSub,
        "io.higherkindness" %% "mu-rpc-server"          % V.mu,
        "io.higherkindness" %% "mu-rpc-fs2"             % V.mu
      ),
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % V.circeVersion)
    )

  lazy val clientSettings: Seq[Def.Setting[_]] = commonSettings ++
    codeGenSettings ++
    Seq(
      libraryDependencies ++= Seq(
        "io.higherkindness" %% "mu-rpc-netty" % V.mu,
        "io.higherkindness" %% "mu-rpc-fs2"   % V.mu
      )
    )

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "mu-smart-home",
      organization := "com.47deg",
      organizationName := "47 Degrees",
      scalaVersion := V.scala,
      scalacOptions += "-Ymacro-annotations",
      scalafmtCheck := true
    )
}
