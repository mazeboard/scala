import scalariform.formatter.preferences._
import Dependencies._
import sbt.Keys.libraryDependencies


// SEE: https://github.com/sbt/sbt/issues/3618
val workaround = {
  sys.props += "packaging.type" -> "jar"
  ()
}

val resolutionRepos = Seq(
  "confluent" at "https://packages.confluent.io/maven/",
  Resolver.bintrayRepo("ovotech", "maven")
)

lazy val root = (project in file(".") withId "mazeboard")
  .settings(
    name := "mazeboard",
    inThisBuild(Seq(
      organization := "com.mazeboard",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.12.7",
      resolvers ++= resolutionRepos,
      IntegrationTest / parallelExecution  := false,
      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-explaintypes",
        "-language:existentials",
        "-Xfatal-warnings",
        "-Ypartial-unification"
      ),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/mazeboard/scala"),
          "https://github.com/mazeboard/scala.git"
        )
      ),
      publishMavenStyle := true,
    )),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .aggregate(configReader, jsonReader, objectReader, sparkUtils, examples)

lazy val examples = (project in file("examples"))
  .dependsOn(configReader, sparkUtils)
  .configs(IntegrationTest)
  .settings(
    name := "examples",
    libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.1.1" withSources() withJavadoc(),
    libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.1.1" withSources() withJavadoc())

lazy val jsonReader = (project in file("json-reader"))
  .dependsOn(objectReader)
  .settings(
    name := "json-reader", 
    libraryDependencies += "org.json" % "json" % "20180813",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

lazy val configReader = (project in file("config-reader"))
  .dependsOn(objectReader)
  .settings(
    name := "config-reader",
    libraryDependencies += "com.typesafe" % "config" % "1.3.3",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0" % Test withSources() withJavadoc(),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

lazy val objectReader = (project in file("object-reader"))
  .settings(
    name := "object-reader",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

lazy val sparkUtils = (project in file("spark-utils"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0" withSources() withJavadoc(),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "com.typesafe" % "config" % "1.3.3"
  )
