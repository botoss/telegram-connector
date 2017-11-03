package ru.botoss.telegram

import akka.actor.ActorRef
import info.mukel.telegrambot4s.api.BotBase
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.{KafkaReceiver, KafkaSender}
import ru.botoss.telegram.serde._

import scala.concurrent.duration.FiniteDuration

object TelegramConnectorFactory {

  /**
    * Creates all telegram connector facilities.
    *
    * @param botGen       Creates bot from actor ref to queue proxy actor.
    *                     Bot sends all messages to this ref.
    * @param proxyTimeout Duration, after which proxy throws away messages,
    *                     for which no module sent response back.
    * @param env          General settings.
    * @return Bot object.
    */
  def apply(botGen: ActorRef => BotBase,
            proxyTimeout: FiniteDuration)
           (implicit env: Environment): BotBase = {
    import env._
    val producer = new KafkaProducer(
      kafkaProperties,
      new ShowSerializer[Key],
      new ShowSerializer[Request])
    val consumer = new KafkaConsumer(
      kafkaProperties,
      new TryDeserializer(new ReadDeserializer[Key]),
      new TryDeserializer(new ReadDeserializer[Response]))
    val queueProxyActor = system.actorOf(
      QueueProxyActor.props(
        new KafkaSender(producer, topic = "to-module"),
        proxyTimeout),
      "queue-proxy"
    )
    system.actorOf(
      QueueReceiverActor.props(
        new KafkaReceiver(consumer, topic = "to-connector"),
        sendTo = queueProxyActor),
      "queue-receiver"
    )
    botGen(queueProxyActor)
  }
}
