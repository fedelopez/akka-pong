name := "akka-pong"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "io.reactivex" %% "rxscala" % "0.25.0",
  "io.reactivex" % "rxswing" % "0.24.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.0",
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1"
)
    