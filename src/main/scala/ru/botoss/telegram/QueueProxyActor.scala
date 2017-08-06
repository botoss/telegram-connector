package ru.botoss.telegram

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.QueueSender

class QueueProxyActor(toModuleSender: QueueSender[Key, Request]) extends Actor {
  override def receive: Receive = receiveImpl(Map())

  private def receiveImpl(cache: Map[Key, ActorRef]): Receive = {
    case request: Request =>
      val key = UUID.randomUUID()
      toModuleSender.asyncSend(key, request)
      context become receiveImpl(cache + (key -> sender()))
    case (key: Key, response: Response) =>
      val partitioned = cache.partition(_._1 == key)
      partitioned.copy(_1 = partitioned._1.toSeq) match {
        case (Seq((`key`, replyTo)), newCache) =>
          replyTo ! response
          context become receiveImpl(newCache)
        case _ =>
      }
  }
}

object QueueProxyActor {
  def props(toModuleSender: QueueSender[Key, Request]) =
    Props(new QueueProxyActor(toModuleSender))
}
