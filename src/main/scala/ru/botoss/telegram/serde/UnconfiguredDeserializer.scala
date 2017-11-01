package ru.botoss.telegram.serde

import java.util

import org.apache.kafka.common.serialization.Deserializer

trait UnconfiguredDeserializer[T] extends Deserializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}
