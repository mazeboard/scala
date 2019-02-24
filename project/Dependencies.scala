import sbt._

object Dependencies {
  
  val Atto = Seq("org.tpolecat" %% "atto-core" % "0.6.3")

  val Avro = Seq("org.apache.avro" % "avro" % "1.8.2")

  val AvroExcludes = Seq("joda-time" % "joda-time")

  val AvroMapRed = Seq("org.apache.avro" % "avro-mapred" % "1.8.2")

  val Cats = Seq("org.typelevel" %% "cats-core" % "1.3.0")
  val hdfsVersion="3.1.0"
  lazy val ScalaCheck = Seq("org.scalacheck" %% "scalacheck"  % "1.13.5" % "test")
  val hdfsClient= Seq("org.apache.hadoop" % "hadoop-client" % hdfsVersion)

  val Fs2 = {
  val version = "1.0.0"
    Seq(
      "co.fs2" %% "fs2-core" % version,
      "co.fs2" %% "fs2-io" % version
    )
  }
  val Fs2Kafka = Seq("com.ovoenergy" %% "fs2-kafka" % "0.16.4")
  val KafkaAvroSerdes = Seq("io.confluent" % "kafka-avro-serializer" % "5.0.1")
  val PureConfig = Seq("com.github.pureconfig" %% "pureconfig" % "0.10.0")

  val KafkaStreams = Seq(
    "org.apache.kafka" % "kafka-streams" % "2.1.0-cp1",
    "org.apache.kafka" %% "kafka-streams-scala" % "2.1.0-cp1",
    "io.confluent" % "kafka-streams-avro-serde" % "5.1.0"
  )
  val Grpc = {
    val version  = "1.16.1"
    Seq(
      "io.grpc" % "grpc-protobuf" % version,
      "io.grpc" % "grpc-stub" % version,
      "io.grpc" % "grpc-netty" % version
    )
  }

  val Prometheus = {
    val version = "0.5.0"
    Seq(
      "io.prometheus" % "simpleclient" % version,
      "io.prometheus" % "simpleclient_hotspot" % version,
      "io.prometheus" % "simpleclient_httpserver" % version,
      "io.prometheus" % "simpleclient_dropwizard" % version,
      "io.prometheus" % "simpleclient_servlet" % version
    )
  }

  val Logging = {
    val version  = "1.2.3"
    Seq(
      "ch.qos.logback" % "logback-core" % version,
      "ch.qos.logback" % "logback-classic" % version
    )
  }

  val LoggingExclude = Seq(
    "log4j" % "log4j",
    "org.slf4j" % "slf4j-log4j12"
  )

  val Jetty = {
    val version = "9.4.7.v20170914"
    Seq(
      "org.eclipse.jetty" % "jetty-server" % version,
      "org.eclipse.jetty" % "jetty-webapp" % version
    )
  }

  val TypeSafeConfig = Seq(
    "com.typesafe" % "config" % "1.3.2"
  )

  val ScalaTest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test,it",
    "org.scalamock" %% "scalamock" % "4.1.0" % "test,it"
  )

  val HadoopCommon = Seq(
    "org.apache.hadoop" % "hadoop-common" % "2.7.3"
  )
}
