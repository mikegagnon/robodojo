enablePlugins(ScalaJSPlugin)

name := "Robodojo"

scalaVersion := "2.11.8"

scalaJSUseRhino in Global := false

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"

libraryDependencies += "org.singlespaced" %%% "scalajs-d3" % "0.3.3"

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")

resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //add resolver
libraryDependencies += "org.denigma" %%% "codemirror-facade" % "5.11-0.7" //add dependency

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")
