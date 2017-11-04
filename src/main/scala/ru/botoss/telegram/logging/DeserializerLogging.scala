package ru.botoss.telegram.logging

import java.nio.charset.StandardCharsets.UTF_8

import org.apache.kafka.common.serialization.Deserializer

trait DeserializerLogging[T] extends Deserializer[T] with Logging {

  override abstract def deserialize(topic: String, data: Array[Byte]): T = {
    val invocation = s"serialize(" +
      s"$topic, " +
      s"${new String(data, UTF_8)})"
    logger.info(invocation)
    val result = super.deserialize(topic, data)
    logger.info(s"$invocation = $result")
    result
  }
}
