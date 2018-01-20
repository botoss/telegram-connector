package ru.botoss.telegram

import java.util.UUID

import ru.botoss.telegram.model.{Command, Key, Request, Response}
import ru.botoss.telegram.queue.QueueSender

import scala.concurrent.duration._

class QueueProxyActorSpec extends ActorUnitSpec {
  private val request = Request(Command("command", Seq()))
  private val response = Response("text")
  private val randomKey = UUID.randomUUID()
  // this way we get UUID generated inside actor
  private val sender: QueueSender[Key, Request] =
    (key, _) => self ! (key, response)
  private val proxy = system.actorOf(QueueProxyActor.props(sender, 1.second))

  "QueueProxyActor" should "response to known actor" in {
    proxy ! request
    val toProxy = expectMsgType[(Key, Response)]
    proxy ! toProxy
    expectMsg(response)
  }

  it should "ignore unknown key with response" in {
    proxy ! (randomKey, response)
    expectNoMsg()
  }
}
