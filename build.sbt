enablePlugins(ScalaJSPlugin)

name := "Robobot"

scalaVersion := "2.11.8"

scalaJSUseRhino in Global := false

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"

libraryDependencies += "org.singlespaced" %%% "scalajs-d3" % "0.3.3"

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"
libraryDependencies += "com.scalawarrior" %%% "scalajs-createjs" % "0.0.2"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")
