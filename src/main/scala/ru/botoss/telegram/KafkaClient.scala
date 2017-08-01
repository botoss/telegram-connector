package ru.botoss.telegram

import java.util.Properties

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._

class KafkaClient(implicit val env: Environment) extends StrictLogging {
  def send(key: String, value: String): Unit = {
    logger.info(s"sending $key to kafka")
    producer.send(new ProducerRecord[String, String](toModuleTopic, key, value))
    logger.info(s"sent $key to kafka")
  }

  def receive(key: String): String = {
    while (true) {
      val records = consumer.poll(1000).asScala
      logger.debug(s"got records $records")
      records.find(record => record.key().equals(key)) match {
        case Some(foundRecord) =>
          logger.debug(s"found record $foundRecord")
          return foundRecord.value()
        case None =>
      }
    }
    throw new RuntimeException
  }

  private val props = {
    val map = env.config.getConfig("kafka").entrySet().asScala.map { entry =>
      entry.getKey -> entry.getValue.unwrapped()
    }.toMap.asJava
    val props = new Properties()
    props.putAll(map)
    props
  }

  private val producer = new KafkaProducer[String, String](props)

  private val toConnectorTopic = "to-connector"
  private val toModuleTopic = "to-module"

  private val consumer = new KafkaConsumer[String, String](props)
  logger.debug(s"created kafka consumer")
  consumer.subscribe(Seq(toConnectorTopic).asJava)
  logger.debug(s"subscribed to topic $toConnectorTopic")
}
