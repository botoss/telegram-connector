package ru.botoss.telegram

import java.util.UUID

import ru.botoss.telegram.model.{Key, Request, Response}
import ru.botoss.telegram.queue.QueueSender

class QueueProxyActorSpec extends ActorUnitSpec {
  private val request = mock[Request]
  private val response = mock[Response]
  private val randomKey = UUID.randomUUID()
  // this way we get UUID generated inside actor
  private val sender: QueueSender[Key, Request] =
    (key, _) => self ! (key, response)
  private val proxy = system.actorOf(QueueProxyActor.props(sender))

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
