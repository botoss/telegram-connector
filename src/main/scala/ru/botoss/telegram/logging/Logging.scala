package ru.botoss.telegram.logging

import com.typesafe.scalalogging.{Logger, StrictLogging}
import org.slf4j.LoggerFactory

/**
  * This trait should be used instead of using [[StrictLogging]] directly,
  * to be able to switch underlying logging library easily.
  */
trait Logging extends StrictLogging {

  override protected val logger: Logger =
    Logger(LoggerFactory.getLogger(getClass.getSuperclass.getName))
}
