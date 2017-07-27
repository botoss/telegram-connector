package ru.botoss.telegram

object Main extends App {
  implicit val env = DockerEnvironment
  new TelegramConnector(new KafkaClient).run()
}
