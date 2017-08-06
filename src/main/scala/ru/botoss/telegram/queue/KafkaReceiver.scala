package ru.botoss.telegram.queue

import java.util.Collections.singletonList

import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConverters._
import scala.concurrent.blocking
import scala.concurrent.duration.FiniteDuration

class KafkaReceiver[K, V](kafkaConsumer: KafkaConsumer[K, V], topic: Topic) extends QueueReceiver[K, V] {

  kafkaConsumer.subscribe(singletonList(topic))

  override def syncReceive(timeout: FiniteDuration): Seq[(K, V)] = {
    blocking {
      kafkaConsumer.poll(timeout.toMillis)
    }.asScala
      .map(record => (record.key(), record.value()))
      .toSeq
  }
}
