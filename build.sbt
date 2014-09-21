organization := "com.github.dnvriend"

name := "scala-futures-test"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies ++= {
    Seq(
      "org.scala-lang"       % "scala-library" % scalaVersion.value,
      "org.scalatest"       %% "scalatest"     % "2.2.2" % "test"
    )
  }

autoCompilerPlugins := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")