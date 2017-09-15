package ru.botoss.telegram

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import org.scalatest.BeforeAndAfterAll

abstract class ActorUnitSpec extends UnitSpec with TestKitBase with ImplicitSender with BeforeAndAfterAll {
  override implicit lazy val system: ActorSystem = ActorSystem()

  override def afterAll(): Unit = shutdown(system)
}
