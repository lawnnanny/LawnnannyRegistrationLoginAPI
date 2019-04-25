import sbt.Keys._
import sbt._
import sbtrelease.Version

name := "lambda"

resolvers += Resolver.sonatypeRepo("public")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
scalaVersion := "2.12.8"
releaseNextVersion := { ver => Version(ver).map(_.bumpMinor.string).getOrElse("Error") }
assemblyJarName in assembly := "lambda.jar"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "2.2.5",
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "com.github.seratch" %% "awscala-dynamodb" % "0.8.+",
  "com.pauldijou" %% "jwt-spray-json" % "2.1.0"
)

libraryDependencies += "io.github.mkotsur" %% "aws-lambda-scala" % {"0.1.1"}
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0" withSources() withJavadoc()
libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings"
)
libraryDependencies += "com.pauldijou" %% "jwt-core" % "2.1.0"
