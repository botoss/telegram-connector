package ru.botoss.telegram
import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

object DockerEnvironment extends Environment {
  override val config: Config = {
    val pathnames = Seq("/kafka.cfg", "/telegram.cfg", "/run/secrets/telegram.secrets")
    pathnames.map(parseFile).fold(ConfigFactory.defaultApplication())(_.withFallback(_))
  }

  private def parseFile(pathname: String) = ConfigFactory.parseFile(new File(pathname))
}
