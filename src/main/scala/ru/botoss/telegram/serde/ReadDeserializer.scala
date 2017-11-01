package ru.botoss.telegram.serde

class ReadDeserializer[T](implicit read: Read[T]) extends UnconfiguredDeserializer[T] {
  private val encoding = "UTF-8"

  override def deserialize(topic: String, data: Array[Byte]): T = {
    read.read(new String(data, encoding))
  }
}
