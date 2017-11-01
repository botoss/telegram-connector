package ru.botoss.telegram.serde

import scalaz._
import scalaz.syntax.show._

class ShowSerializer[T: Show] extends UnconfiguredSerializer[T] {
  private val encoding = "UTF-8"

  override def serialize(topic: String, data: T): Array[Byte] = {
    data.shows.getBytes(encoding)
  }
}
