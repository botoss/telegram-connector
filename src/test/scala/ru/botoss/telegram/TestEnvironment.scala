package ru.botoss.telegram

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object TestEnvironment extends Environment {
  override val config: Config = ConfigFactory.parseMap(Map(
    "telegram.bot.token" -> "",
    "telegram.bot.username" -> "botoss_test_bot"
  ).asJava)

  override val kafkaProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:6001")
    props.put("group.id", "test")
    props
  }

  override implicit val system: ActorSystem = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executionContext: ExecutionContext = system.dispatcher
}
