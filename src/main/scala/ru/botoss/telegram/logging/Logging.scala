package ru.botoss.telegram.logging

import com.typesafe.scalalogging.StrictLogging

/**
  * This trait should be used instead of using [[StrictLogging]] directly,
  * to be able to switch underlying logging library easily.
  */
trait Logging extends StrictLogging
