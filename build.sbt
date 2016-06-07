enablePlugins(ScalaJSPlugin)

name := "Robobot"

scalaVersion := "2.11.8"

scalaJSUseRhino in Global := false

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"

libraryDependencies += "org.singlespaced" %%% "scalajs-d3" % "0.3.3"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")