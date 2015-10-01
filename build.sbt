name := "akka-pong"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.14",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.14",
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1"
)
    