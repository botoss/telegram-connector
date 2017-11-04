package ru.botoss.telegram.logged

import info.mukel.telegrambot4s.api.BotExecutionContext
import info.mukel.telegrambot4s.api.declarative.Messages
import info.mukel.telegrambot4s.methods.ParseMode.ParseMode
import info.mukel.telegrambot4s.models.{Message, ReplyMarkup}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait BotLogging extends Messages with BotExecutionContext with Logging {

  override abstract def receiveMessage(msg: Message): Unit = {
    val invocation = s"receiveMessage($msg)"
    logger.info(invocation)
    val result = super.receiveMessage(msg)
    logger.info(s"$invocation = $result")
  }

  override abstract def reply(text: String,
                              parseMode: Option[ParseMode],
                              disableWebPagePreview: Option[Boolean],
                              disableNotification: Option[Boolean],
                              replyToMessageId: Option[Int],
                              replyMarkup: Option[ReplyMarkup])
                             (implicit message: Message): Future[Message] = {
    val invocation = s"reply(" +
      s"$text, " +
      s"$parseMode, " +
      s"$disableWebPagePreview, " +
      s"$disableNotification, " +
      s"$replyToMessageId, " +
      s"$replyMarkup" +
      s")"
    logger.info(invocation)
    val future = super.reply(
      text,
      parseMode,
      disableWebPagePreview,
      disableNotification,
      replyToMessageId,
      replyMarkup
    )
    future.onComplete {
      case Success(result) =>
        logger.info(s"$invocation = $result")
      case Failure(ex) =>
        logger.error(s"$invocation = $ex", ex)
    }
    future
  }
}
