package ru.botoss.telegram

import java.util.UUID

import info.mukel.telegrambot4s.api.Extractors._
import info.mukel.telegrambot4s.api.declarative.{Commands, ToCommand}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import play.api.libs.json.Json

class TelegramConnector(kafka: KafkaClient)(implicit env: Environment)
  extends TelegramBot with Polling with Commands with Logging {

  override lazy val token: String = env.config.getString("telegram.bot.token")
  private val botUsername = env.config.getString("telegram.bot.username")

  onMessage { implicit msg =>
    using(textTokens) { tokens =>
      log.debug(s"got tokens $tokens")
      val head = tokens.head
      // Filter only commands
      if (head.startsWith(ToCommand.CommandPrefix)) {
        // In group chats command
        val cmd = head.substring(1) // remove command prefix (/)
            .replace(s"@$botUsername", "") // in group chat commands looks this way: /cmd@$botUsername
        val args = tokens.tail
        val key = UUID.randomUUID().toString
        val value = Json.obj(
          "connector-id" -> "telegram",
          "command" -> cmd,
          "params" -> args
        ).toString
        kafka.send(key, value)
        log.info(s"sent $key to kafka")
        val result = kafka.receive(key)
        log.info(s"received $key from kafka")
        reply((Json.parse(result) \ "text").as[String])
        log.info(s"replied to $key request to telegram")
      }
    }
  }
}
