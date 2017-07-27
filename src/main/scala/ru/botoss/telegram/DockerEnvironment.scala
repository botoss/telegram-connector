package ru.botoss.telegram
import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

object DockerEnvironment extends Environment with StrictLogging {
  override val config: Config = {
    val pathnames = Seq("/kafka.properties", "/telegram.properties", "/run/secrets/telegram.properties")
    val config = pathnames.map(parseFile).fold(ConfigFactory.defaultApplication())(_.withFallback(_))
    logger.debug(s"config: $config")
    config
  }

  private def parseFile(pathname: String) = ConfigFactory.parseFile(new File(pathname))
}
