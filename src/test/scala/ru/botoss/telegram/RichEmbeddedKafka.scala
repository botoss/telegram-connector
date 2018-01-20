package ru.botoss.telegram

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.Deserializer
import org.scalatest.Suite

import scala.concurrent.duration._

trait RichEmbeddedKafka extends EmbeddedKafka {

  self: Suite =>

  // should contribute this to embedded kafka lib
  def consumeFirstKeyedMessageFromWithCustomTimeout[K, V](topic: String,
                                                          autoCommit: Boolean = false,
                                                          timeout: Duration = 5.seconds)
                                                         (implicit config: EmbeddedKafkaConfig,
                                                          keyDeserializer: Deserializer[K],
                                                          valueDeserializer: Deserializer[V]): (K, V) = {
    val result = consumeNumberKeyedMessagesFromTopics[K, V](Set(topic), number = 1, autoCommit, timeout)
    result(topic).head
  }
}
