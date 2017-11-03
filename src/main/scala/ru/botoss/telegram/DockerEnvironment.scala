package ru.botoss.telegram
import java.io.{File, FileReader}
import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import resource._
import ru.botoss.telegram.logged.Logging

import scala.concurrent.ExecutionContext

object DockerEnvironment extends Environment with Logging {
  override val config: Config = {
    val pathnames = Seq("/telegram.properties", "/run/secrets/telegram.properties")
    val config = pathnames.map(parseFile).fold(ConfigFactory.defaultApplication())(_.withFallback(_))
    logger.debug(s"config: $config")
    config
  }

  override val kafkaProperties: Properties = {
    val props = new Properties()
    managed(new FileReader("/kafka.properties")).foreach(props.load)
    props.put("group.id", "telegram-connector")
    props.remove("key.serializer")
    props.remove("value.serializer")
    props.remove("key.deserializer")
    props.remove("value.deserializer")
    props
  }

  override implicit val system: ActorSystem = ActorSystem()

  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  override implicit val executionContext: ExecutionContext = system.dispatcher

  private def parseFile(pathname: String) = ConfigFactory.parseFile(new File(pathname))
}
