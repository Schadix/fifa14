name := "fifa14"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies +=
  "com.tinkerpop.rexster" % "rexster-protocol" % "2.4.0"

libraryDependencies +=
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.4.0"

libraryDependencies +=
  "com.thinkaurelius.titan" % "titan-core" % "0.4.2"

libraryDependencies +=
  "com.thinkaurelius.titan" % "titan-berkeleyje" % "0.4.2"

play.Project.playScalaSettings
