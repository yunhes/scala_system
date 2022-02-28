val scala3Version = "3.1.2-RC1"
val dfiantVersion = "0.2.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dfiantdemo",
    version := "0.1.0-SNAPSHOT",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:strictEquality",
      "-language:implicitConversions",
    ),
    scalaVersion := scala3Version,
    addCompilerPlugin("io.github.dfianthdl" % "dfiant-plugin" % dfiantVersion cross CrossVersion.full),
    libraryDependencies += "io.github.dfianthdl" % "dfiant_3" % dfiantVersion
  )
