package ru.botoss.telegram

import java.util.Properties

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig, KafkaUnavailableException}
import org.apache.kafka.clients.consumer.{KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.{KafkaException, TopicPartition}
import org.scalatest.Suite

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.TimeoutException
import scala.concurrent.duration._
import scala.util.Try

trait RichEmbeddedKafka extends EmbeddedKafka {
  this: Suite =>

  def consumeFirstMessageWithKeyFrom[K, V](topic: String,
                                           autoCommit: Boolean = false,
                                           timeout: Duration = 5.seconds)(
                                            implicit config: EmbeddedKafkaConfig,
                                            keyDeserializer: Deserializer[K],
                                            valueDeserializer: Deserializer[V]): (K, V) =
    consumeFirstMessageWithKeyFromTopics(
      Set(topic), autoCommit, timeout
    )(config, keyDeserializer, valueDeserializer)._2.head

  def consumeFirstMessageWithKeyFromTopics[K, V](topics: Set[String],
                                                 autoCommit: Boolean = false,
                                                 timeout: Duration = 5.seconds)(
                                                  implicit config: EmbeddedKafkaConfig,
                                                  keyDeserializer: Deserializer[K],
                                                  valueDeserializer: Deserializer[V]): (String, Map[K, V]) =
    consumeNumberMessagesWithKeysFromTopics(
      topics, number = 1, autoCommit, timeout
    )(config, keyDeserializer, valueDeserializer).head

  // EmbeddedKafka doesn't have method to consume messages with keys.
  // This method is mostly copy-paste from EmbeddedKafka.consumeNumberMessagesFromTopics().
  // Need to PR this into embedded kafka.
  def consumeNumberMessagesWithKeysFromTopics[K, V](topics: Set[String],
                                                    number: Int,
                                                    autoCommit: Boolean = false,
                                                    timeout: Duration = 5.seconds,
                                                    resetTimeoutOnEachMessage: Boolean = true)(
                                                     implicit config: EmbeddedKafkaConfig,
                                                     keyDeserializer: Deserializer[K],
                                                     valueDeserializer: Deserializer[V]): Map[String, Map[K, V]] = {

    import scala.collection.JavaConverters._

    val props = baseConsumerConfig
    props.put("enable.auto.commit", autoCommit.toString)

    var timeoutNanoTime = System.nanoTime + timeout.toNanos
    val consumer =
      new KafkaConsumer[K, V](props, keyDeserializer, valueDeserializer)

    val messages = Try {
      val messagesBuffers = topics.map(_ -> mutable.Map.empty[K, V]).toMap
      var messagesRead = 0
      consumer.subscribe(topics.asJava)
      topics.foreach(consumer.partitionsFor)

      while (messagesRead < number && System.nanoTime < timeoutNanoTime) {
        val records = consumer.poll(1000)
        val recordIter = records.iterator()
        if (resetTimeoutOnEachMessage && recordIter.hasNext) {
          timeoutNanoTime = System.nanoTime + timeout.toNanos
        }
        while (recordIter.hasNext && messagesRead < number) {
          val record = recordIter.next()
          val topic = record.topic()
          messagesBuffers(topic) += record.key -> record.value()
          val tp = new TopicPartition(topic, record.partition())
          val om = new OffsetAndMetadata(record.offset() + 1)
          consumer.commitSync(Map(tp -> om).asJava)
          messagesRead += 1
        }
      }
      if (messagesRead < number) {
        throw new TimeoutException(s"Unable to retrieve $number message(s) from Kafka in $timeout")
      }
      messagesBuffers.map { case (topic, msgs) => (topic, msgs.toMap) }
    }

    consumer.close()
    messages.recover {
      case ex: KafkaException => throw new KafkaUnavailableException(ex)
    }.get
  }

  private def baseConsumerConfig(implicit config: EmbeddedKafkaConfig): Properties = {
    val props = new Properties()
    props.put("group.id", s"embedded-kafka-spec")
    props.put("bootstrap.servers", s"localhost:${config.kafkaPort}")
    props.put("auto.offset.reset", "earliest")
    props.put("enable.auto.commit", "false")
    props.putAll(config.customConsumerProperties.asJava)
    props
  }
}
