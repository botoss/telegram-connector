package ru.botoss.telegram

import info.mukel.telegrambot4s.api.BotBase
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.{KafkaReceiver, KafkaSender}
import ru.botoss.telegram.serde._

import scala.concurrent.duration.FiniteDuration

trait BaseMain extends App {
  protected def env: Environment

  protected def proxyTimeout: FiniteDuration

  protected def bot: BotBase

  private implicit val ienv = env

  import ienv._

  private val producer = new KafkaProducer(
    kafkaProperties,
    new ShowSerializer[Key],
    new ShowSerializer[Request])

  private val consumer = new KafkaConsumer(
    kafkaProperties,
    new ReadDeserializer[Key],
    new ReadDeserializer[Response])

  // protected, because it is needed to initialize bot
  protected val queueProxyActor = system.actorOf(
    QueueProxyActor.props(
      new KafkaSender(producer, topic = "to-module"),
      proxyTimeout))

  system.actorOf(
    QueueReceiverActor.props(
      new KafkaReceiver(consumer, topic = "to-connector"), sendTo = queueProxyActor
    )
  )

  bot.run()
}
