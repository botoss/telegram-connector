package ru.botoss.telegram.queue

import java.util.Collections.singletonList

import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConverters._
import scala.concurrent.blocking
import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

class KafkaReceiver[K, V](kafkaConsumer: KafkaConsumer[Try[K], Try[V]], topic: Topic) extends QueueReceiver[K, V] {

  kafkaConsumer.subscribe(singletonList(topic))

  override def syncReceive(timeout: FiniteDuration): Seq[(K, V)] = {
    blocking {
      kafkaConsumer.poll(timeout.toMillis)
    }.asScala
      .collect {
        case ConsumerRecord(Success(key), Success(value)) =>
          key -> value
      }.toSeq
  }
}
