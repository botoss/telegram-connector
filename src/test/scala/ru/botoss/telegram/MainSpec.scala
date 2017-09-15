package ru.botoss.telegram

import info.mukel.telegrambot4s.api.RequestHandler
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.{Chat, ChatId, ChatType, Message}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.{KafkaReceiver, KafkaSender}
import ru.botoss.telegram.serde._

class MainSpec extends UnitSpec with RichEmbeddedKafka {
  it should "handle request" in {
    withRunningKafka {
      implicit val env = TestEnvironment
      import env._

      val producer = new KafkaProducer(
        kafkaProperties,
        new ShowSerializer[Key],
        new ShowSerializer[Request])

      val consumer = new KafkaConsumer(
        kafkaProperties,
        new ReadDeserializer[Key],
        new ReadDeserializer[Response])

      val queueProxyActor = system.actorOf(
        QueueProxyActor.props(
          new KafkaSender(producer, topic = "to-module")))

      system.actorOf(
        QueueReceiverActor.props(
          new KafkaReceiver(consumer, topic = "to-connector"), sendTo = queueProxyActor
        )
      )

      val requestHandler = mock[RequestHandler]

      val bot = new Bot(queueProxyActor) {
        override val client: RequestHandler = requestHandler
      }

      bot.receiveMessage(Message(
        messageId = 1,
        date = (System.currentTimeMillis() / 1000).toInt,
        chat = Chat(id = 1, `type` = ChatType.Private),
        text = Some("/testcmd uppercase it")
      ))

      (requestHandler.apply(_: SendMessage)(_: Manifest[Message])).expects(
        SendMessage(ChatId(1), "UPPERCASE IT"), *
      )

      val (key, request) = consumeFirstMessageWithKeyFrom[Key, Request]("to-module")
      val response = Response(text = request.command.params.map(_.toUpperCase).mkString(" "))
      publishToKafka("to-connector", key, response)
    }
  }
}
