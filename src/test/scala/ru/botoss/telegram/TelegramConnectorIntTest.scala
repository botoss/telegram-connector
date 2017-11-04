package ru.botoss.telegram

import info.mukel.telegrambot4s.api.RequestHandler
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.{Chat, ChatId, ChatType, Message}
import net.manub.embeddedkafka.EmbeddedKafka
import org.scalatest.BeforeAndAfterAll
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.serde._

import scala.concurrent.duration._
import scalaz.Show.showFromToString

class TelegramConnectorIntTest extends UnitSpec with EmbeddedKafka with BeforeAndAfterAll {
  implicit private val env = TestEnvironment
  private val requestHandler = mock[RequestHandler]
  private val bot = TelegramConnectorFactory(
    new Bot(_) {
      override val client: RequestHandler = requestHandler
    },
    proxyTimeout = 10.seconds
  )

  override protected def beforeAll(): Unit = {
    EmbeddedKafka.start()
  }

  it should "handle request" in {

    bot.receiveMessage(Message(
      messageId = 1,
      date = (System.currentTimeMillis() / 1000).toInt,
      chat = Chat(id = 1, `type` = ChatType.Private),
      text = Some("/testcmd uppercase it")
    ))

    (requestHandler.apply(_: SendMessage)(_: Manifest[Message])).expects(
      SendMessage(ChatId(1), "UPPERCASE IT"), *
    )

    val (key, request) = consumeFirstKeyedMessageFrom[Key, Request]("to-module")
    val response = Response(text = request.command.params.map(_.toUpperCase).mkString(" "))
    publishToKafka("to-connector", key, response)
  }

  it should "ignore invalid message and then handle request anyway" in {
    bot.receiveMessage(Message(
      messageId = 1,
      date = (System.currentTimeMillis() / 1000).toInt,
      chat = Chat(id = 1, `type` = ChatType.Private),
      text = Some("/testcmd uppercase it")
    ))

    (requestHandler.apply(_: SendMessage)(_: Manifest[Message])).expects(
      SendMessage(ChatId(1), "UPPERCASE IT"), *
    )

    val (key, request) = consumeFirstKeyedMessageFrom[Key, Request]("to-module")
    val response = Response(text = request.command.params.map(_.toUpperCase).mkString(" "))
    implicit val ss = showFromToString[String]
    publishToKafka("to-connector", "invalid-key", response)
    publishToKafka("to-connector", key, response)
  }

  override protected def afterAll(): Unit = {
    EmbeddedKafka.stop()
  }
}
