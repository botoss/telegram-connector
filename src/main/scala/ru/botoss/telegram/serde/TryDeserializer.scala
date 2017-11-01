package ru.botoss.telegram.serde

import org.apache.kafka.common.serialization.Deserializer

import scala.util.Try

class TryDeserializer[T](deserializer: Deserializer[T]) extends UnconfiguredDeserializer[Try[T]] {

  override def deserialize(topic: String, data: Array[Byte]): Try[T] = {
    Try(deserializer.deserialize(topic, data))
  }
}
