package ru.botoss.telegram.queue

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaSender[K, V](kafkaProducer: KafkaProducer[K, V], topic: Topic) extends QueueSender[K, V] {
  override def asyncSend(key: K, value: V): Unit = {
    kafkaProducer.send(new ProducerRecord[K, V](topic, key, value))
  }
}
