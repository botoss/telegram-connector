package ru.botoss.telegram

object Main extends App {
  if (args.length != 3) {
    println("Usage: Main kafka_ip token bot_login")
    throw new Exception
  }
  val kafkaIp = args(0)
  val token = args(1)
  val botLogin = args(2)
  new TelegramConnector(token, botLogin, new KafkaClient(kafkaIp)).run()
}
