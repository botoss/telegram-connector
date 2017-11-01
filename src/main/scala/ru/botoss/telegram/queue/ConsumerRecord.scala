package ru.botoss.telegram.queue

import org.apache.kafka.clients.consumer.{ConsumerRecord => JavaConsumerRecord}

object ConsumerRecord {
  def unapply[K, V](record: JavaConsumerRecord[K, V]): Option[(K, V)] =
    Some(record.key -> record.value)
}
