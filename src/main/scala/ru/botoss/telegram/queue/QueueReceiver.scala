package ru.botoss.telegram.queue

import scala.concurrent.duration.FiniteDuration

trait QueueReceiver[K, V] {
  def syncReceive(timeout: FiniteDuration): Seq[(K, V)]
}
