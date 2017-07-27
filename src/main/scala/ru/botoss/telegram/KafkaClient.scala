package ru.botoss.telegram

import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.github.andr83.scalaconfig._
import scala.collection.JavaConverters._

class KafkaClient(implicit val env: Environment) extends Logging {
  def send(key: String, value: String): Unit = {
    log.info(s"sending $key to kafka")
    producer.send(new ProducerRecord[String, String](toModuleTopic, key, value))
    log.info(s"sent $key to kafka")
  }

  def receive(key: String): String = {
    while (true) {
      val records = consumer.poll(1000).asScala
      log.debug(s"got records $records")
      records.find(record => record.key().equals(key)) match {
        case Some(foundRecord) =>
          log.debug(s"found record $foundRecord")
          return foundRecord.value()
        case None =>
      }
    }
    throw new RuntimeException
  }

  private val props = env.config.atKey("kafka").as[Properties]
  private val producer = new KafkaProducer[String, String](props)

  private val toConnectorTopic = "to-connector"
  private val toModuleTopic = "to-module"

  private val consumer = new KafkaConsumer[String, String](props)
  log.debug(s"created kafka consumer")
  consumer.subscribe(Seq(toConnectorTopic).asJava)
  log.debug(s"subscribed to topic $toConnectorTopic")
}
