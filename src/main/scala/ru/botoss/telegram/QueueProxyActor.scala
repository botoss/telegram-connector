package ru.botoss.telegram

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import ru.botoss.telegram.logged.ActorLogging
import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.QueueSender

import scala.concurrent.duration.FiniteDuration

class QueueProxyActor(toModuleSender: QueueSender[Key, Request],
                      timeout: FiniteDuration) extends Actor {
  private implicit val ec = context.dispatcher

  override def receive: Receive = receiveImpl(Map())

  private def receiveImpl(cache: Map[Key, ActorRef]): Receive = {
    case request: Request =>
      val key = UUID.randomUUID()
      toModuleSender.asyncSend(key, request)
      context.system.scheduler.scheduleOnce(timeout, self, RequestTimeout(key))
      context become receiveImpl(cache + (key -> sender()))
    case (key: Key, response: Response) =>
      val partitioned = cache.partition(_._1 == key)
      partitioned.copy(_1 = partitioned._1.toSeq) match {
        case (Seq((`key`, replyTo)), newCache) =>
          replyTo ! response
          context become receiveImpl(newCache)
        case _ =>
      }
    case RequestTimeout(key) =>
      context become receiveImpl(cache - key)
  }
}

object QueueProxyActor {
  def props(toModuleSender: QueueSender[Key, Request],
            timeout: FiniteDuration) =
    Props(new QueueProxyActor(toModuleSender, timeout) with ActorLogging)
}

private case class RequestTimeout(key: Key)
