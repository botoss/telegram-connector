package ru.botoss.telegram

import akka.actor.{Actor, ActorRef, Props}
import ru.botoss.telegram.queue.QueueReceiver

import scala.concurrent.duration._

class QueueReceiverActor[K, V](receiver: QueueReceiver[K, V], sendTo: ActorRef) extends Actor {
  self ! Poll

  override def receive: Receive = {
    case Poll =>
      receiver.syncReceive(100.millis).foreach(sendTo ! _)
      self ! Poll
  }

  private case object Poll
}

object QueueReceiverActor {
  def props[K, V](receiver: QueueReceiver[K, V], sendTo: ActorRef) =
    Props(new QueueReceiverActor(receiver, sendTo))
}
