name := "akka-pong"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "io.reactivex" %% "rxscala" % "0.25.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1"
)
    