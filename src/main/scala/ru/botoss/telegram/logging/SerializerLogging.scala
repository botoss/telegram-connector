package ru.botoss.telegram.logging

import java.nio.charset.StandardCharsets.UTF_8

import org.apache.kafka.common.serialization.Serializer

trait SerializerLogging[T] extends Serializer[T] with Logging {

  override abstract def serialize(topic: String, data: T): Array[Byte] = {
    val invocation = s"serialize(" +
      s"$topic, " +
      s"$data)"
    logger.info(invocation)
    val result = super.serialize(topic, data)
    logger.info(s"$invocation = ${new String(result, UTF_8)}")
    result
  }
}
