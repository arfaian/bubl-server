name := "bubl-server"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.5",
  "org.scalaequals" %% "scalaequals-core" % "1.2.0",
  jdbc,
  anorm,
  cache
)

play.Project.playScalaSettings

scalacOptions ++= Seq("-feature")
