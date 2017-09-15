package ru.botoss.telegram

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.{KafkaReceiver, KafkaSender}
import ru.botoss.telegram.serde._

object Main extends App {
  implicit val env = DockerEnvironment
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

  new Bot(queueProxyActor).run()
}
