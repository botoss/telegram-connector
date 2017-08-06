package ru.botoss.telegram.serde

import java.util

import org.apache.kafka.common.serialization.Deserializer

class ReadDeserializer[T](implicit read: Read[T]) extends Deserializer[T] {
  private val encoding = "UTF-8"

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): T = {
    read.read(new String(data, encoding))
  }

  override def close(): Unit = {}
}
