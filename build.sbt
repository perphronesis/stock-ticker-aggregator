ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

name := "stock-ticker-app"

resolvers ++= Seq(
  "Maven Central" at "https://repo1.maven.org/maven2/"
)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.6"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "com.typesafe" % "config" % "1.4.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"

crossPaths := false
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

assembly / assemblyMergeStrategy := {
  //case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("META-INF", xs@_*) =>
    (xs map {
      _.toLowerCase
    }) match {
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.discard
    }
  case x => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "stock-ticker-app"
  )
