package ru.botoss.telegram

import ru.botoss.telegram.logging.BotLogging

import scala.concurrent.duration._

object Main extends App {
  implicit val env = DockerEnvironment

  TelegramConnectorFactory(
    new Bot(_) with BotLogging,
    proxyTimeout = 3.seconds
  ).run()
}
