package ru.botoss.telegram.logged

import akka.actor.Actor
import akka.event.LoggingReceive

trait LoggedActor extends Actor {

  abstract override def receive: Receive =
    LoggingReceive(super.receive)
}
