package ru.botoss.telegram

import info.mukel.telegrambot4s.api.RequestHandler
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.{Chat, ChatId, ChatType, Message}
import net.manub.embeddedkafka.EmbeddedKafka
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.serde._

import scala.concurrent.Promise
import scala.concurrent.duration._
import scalaz.Show.showFromToString

class TelegramConnectorIntTest extends UnitSpec with EmbeddedKafka with BeforeAndAfterAll with ScalaFutures {
  implicit private val env = TestEnvironment
  private val requestHandler = mock[RequestHandler]
  private val proxyTimeout = 10.seconds
  private val bot = TelegramConnectorFactory(
    new Bot(_) {
      override val client: RequestHandler = requestHandler
    },
    proxyTimeout
  )
  private val responseMessage = Message(
    messageId = 1,
    date = 1,
    chat = Chat(1, ChatType.Private)
  )
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(proxyTimeout)

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

    // needed to await for requestHandler.apply() call
    val promise = Promise[Message]()
    (requestHandler.apply(_: SendMessage)(_: Manifest[Message])).expects(
      SendMessage(ChatId(1), "UPPERCASE IT"), *
    ).onCall { _ =>
      promise.success(responseMessage)
      promise.future
    }

    val (key, request) = consumeFirstKeyedMessageFrom[Key, Request]("to-module")
    val response = Response(text = request.command.params.map(_.toUpperCase).mkString(" "))
    publishToKafka("to-connector", key, response)
    promise.future.futureValue
  }

  it should "ignore invalid message and then handle request anyway" in {
    bot.receiveMessage(Message(
      messageId = 1,
      date = (System.currentTimeMillis() / 1000).toInt,
      chat = Chat(id = 1, `type` = ChatType.Private),
      text = Some("/testcmd uppercase it")
    ))

    // needed to await for requestHandler.apply() call
    val promise = Promise[Message]()
    (requestHandler.apply(_: SendMessage)(_: Manifest[Message])).expects(
      SendMessage(ChatId(1), "UPPERCASE IT"), *
    ).onCall { _ =>
      promise.success(responseMessage)
      promise.future
    }

    val (key, request) = consumeFirstKeyedMessageFrom[Key, Request]("to-module")
    val response = Response(text = request.command.params.map(_.toUpperCase).mkString(" "))
    implicit val ss = showFromToString[String]
    publishToKafka("to-connector", "invalid-key", response)
    publishToKafka("to-connector", key, response)
    promise.future.futureValue
  }

  override protected def afterAll(): Unit = {
    EmbeddedKafka.stop()
  }
}
