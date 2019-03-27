import scalariform.formatter.preferences._
import Dependencies._
import sbt.Keys.libraryDependencies

// SEE: https://github.com/sbt/sbt/issues/3618
val workaround = {
  sys.props += "packaging.type" -> "jar"
  ()
}

val avroSettings = Seq(javaSource in AvroConfig := (sourceManaged in Compile).value,
  AvroConfig / stringType := "String")

val resolutionRepos = Seq(
  "mvnrepo" at "https://mvnrepository.com"
  ,"confluent" at "https://packages.confluent.io/maven/"
  ,"jcenter" at "https://jcenter.bintray.com/"
  ,"Artima Maven Repository" at "http://repo.artima.com/releases"
  ,Resolver.bintrayRepo("ovotech", "maven")
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
  .aggregate(configReader, jsonReader, objectReader, avroUtils, sparkUtils/*, dataStream, tests*/)

/*lazy val tests = (project in file("tests"))
  .dependsOn(configReader/*, dataStream*/)
  .settings(
    name := "tests",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
    //libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.1.1" % Test withSources() withJavadoc(),
    //libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.1.1" % Test withSources() withJavadoc()
)*/

/*lazy val dataStream = (project in file("data-stream"))
  .dependsOn(configReader)
  .settings(
    name := "data-stream",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.apache.kafka" %% "kafka" % "2.1.1",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.21"
    //, libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.21"
  )
*/
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
  .dependsOn(configReader, avroUtils)
  .settings(
    name := "spark-utils",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0" withSources() withJavadoc(),
    libraryDependencies += "com.databricks" % "spark-avro_2.11" % "4.0.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

val x = {
  System.setProperty("org.apache.avro.specific.templates", "avro-utils/src/main/templates/avro/java/")
}

lazy val avroUtils = (project in file("avro-utils"))
  .dependsOn(configReader)
  .settings(
    name := "avro-utils",
    javacOptions ++= Seq("-parameters"),
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.apache.avro" % "avro-tools" % "1.8.2",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0",
    libraryDependencies += "com.databricks" % "spark-avro_2.11" % "4.0.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    avroSettings
  )
