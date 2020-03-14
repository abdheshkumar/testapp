import sbt.Keys.version
val AkkaVersion = "2.5.26"
val sparkV = "2.4.5"
val scalaTestV = "3.1.0"
val scalacheckV = "1.14.3"
val circeVersion = "0.12.3"

lazy val commonSettings =
  Seq(dockerBaseImage := "openjdk:8-jre-alpine", dockerCommands ++= Seq())

lazy val root = Project("root", file("."))
  .settings(
    name := "spark-practices",
    version := "0.1",
    scalaVersion := "2.12.10"
  )
  .aggregate(ingestion, api, etl)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val ingestion = Project("ingestion", file("ingestion"))
  .settings(commonSettings)
  .settings(
    name := "ingestion",
    version := "0.1",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkV,
      "org.apache.spark" %% "spark-sql" % sparkV,
      "org.apache.spark" %% "spark-streaming" % sparkV,
      "org.apache.spark" %% "spark-mllib" % sparkV,
      "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      //"com.datastax.spark" %% "spark-cassandra-connector" % sparkV,
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkV, //Structured Streaming + Kafka Integration Guide
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkV, //spark-streaming-kafka
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % scalaTestV % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckV % Test
    )
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val api = Project("api", file("api"))
  .settings(commonSettings)
  .settings(
    name := "api",
    version := "0.1",
    scalaVersion := "2.12.10",
    dockerExposedPorts ++= Seq(8085),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkV,
      "org.apache.spark" %% "spark-sql" % sparkV,
      "org.apache.spark" %% "spark-streaming" % sparkV,
      "org.apache.spark" %% "spark-mllib" % sparkV,
      "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      //"com.datastax.spark" %% "spark-cassandra-connector" % sparkV,
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkV, //Structured Streaming + Kafka Integration Guide
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      ("de.heikoseeberger" %% "akka-http-circe" % "1.31.0")
        .excludeAll(ExclusionRule("com.typesafe.akka")),
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.slick" %% "slick" % "3.3.1",
      "org.slf4j" % "slf4j-nop" % "1.7.26",
      "org.postgresql" % "postgresql" % "42.2.11",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
      "org.scalatest" %% "scalatest" % scalaTestV % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckV % Test
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val etl = Project("etl", file("etl"))
  .settings(commonSettings)
  .settings(
    name := "etl",
    version := "0.1",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkV,
      "org.apache.spark" %% "spark-sql" % sparkV,
      "org.apache.spark" %% "spark-streaming" % sparkV,
      "org.apache.spark" %% "spark-mllib" % sparkV,
      "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.11",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.postgresql" % "postgresql" % "42.2.11",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkV, //Structured Streaming + Kafka Integration Guide
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkV, //spark-streaming-kafka
      "org.scalatest" %% "scalatest" % scalaTestV % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckV % Test
    )
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)
