package ru.botoss.telegram

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  protected val log: Logger = LoggerFactory.getLogger(super.getClass)
}
