package ru.botoss.telegram

import com.typesafe.config.Config

trait Environment {
  val config: Config
}
