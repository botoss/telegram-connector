name := "telegram-connector"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "info.mukel" %% "telegrambot4s" % "3.0.1",
  "org.apache.kafka" % "kafka-clients" % "0.11.0.0",
  "com.typesafe.play" %% "play-json" % "2.6.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
