package ru.botoss.telegram

import scala.concurrent.duration._

object Main extends App {
  implicit val env = DockerEnvironment

  TelegramConnectorFactory(
    new Bot(_),
    proxyTimeout = 3.seconds
  ).run()
}
