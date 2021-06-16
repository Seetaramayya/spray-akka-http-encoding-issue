import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.13"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.4.20"
  lazy val akka = "com.typesafe.akka" %% "akka-actor" % "2.4.20"
  lazy val spray = "io.spray" %% "spray-client" % "1.3.4"
  lazy val sprayJson = "io.spray" %% "spray-json" % "1.3.5"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.8"
}
