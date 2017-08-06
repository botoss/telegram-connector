package ru.botoss.telegram.serde

trait Read[T] {
  def read(s: String): T
}
