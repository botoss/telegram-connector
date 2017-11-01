package ru.botoss.telegram.serde

import java.util

import org.apache.kafka.common.serialization.Serializer

trait UnconfiguredSerializer[T] extends Serializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}
