
ThisBuild / version := "1.0.0-cloudOrgSimulator"

ThisBuild / scalaVersion := "3.2.1"

val logbackVersion = "1.3.0"
val sfl4sVersion = "2.0.3"
val typesafeConfigVersion = "1.4.2"
val scalacticVersion = "3.2.9"
val cloudSimPlusVersion = "7.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "CS441-Homework3"
  )

libraryDependencies ++= Seq(
  "org.cloudsimplus" % "cloudsim-plus" % cloudSimPlusVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4j" % "slf4j-api" % sfl4sVersion,
  "org.slf4j" % "slf4j-simple" % sfl4sVersion,
  "com.typesafe" % "config" % typesafeConfigVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test
)

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}