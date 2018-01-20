package ru.botoss.telegram.logging

import akka.actor.Actor
import akka.event.LoggingReceive

trait ActorLogging extends Actor {

  abstract override def receive: Receive =
    LoggingReceive(super.receive)
}
