package ru.botoss.telegram.queue

trait QueueSender[K, V] {
  def asyncSend(key: K, value: V): Unit
}
