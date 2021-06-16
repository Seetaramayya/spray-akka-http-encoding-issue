import Dependencies._

ThisBuild / scalaVersion     := "2.11.7"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "spray-akka-http-encoding-issue",
    libraryDependencies ++= Seq(
      akka,
      akkaStream,
      akkaHttp,
      spray,
      sprayJson,
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
