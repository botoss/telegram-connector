package ru.botoss.telegram.serde

import java.util

import org.apache.kafka.common.serialization.Serializer

import scalaz._
import scalaz.syntax.show._

class ShowSerializer[T: Show] extends Serializer[T] {
  private val encoding = "UTF-8"

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: T): Array[Byte] = {
    data.shows.getBytes(encoding)
  }

  override def close(): Unit = {}
}
