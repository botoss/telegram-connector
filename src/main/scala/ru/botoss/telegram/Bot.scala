package ru.botoss.telegram

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{Logger, StrictLogging}
import info.mukel.telegrambot4s.api.Extractors._
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{BotBase, Polling, RequestHandler}
import info.mukel.telegrambot4s.clients.AkkaClient
import info.mukel.telegrambot4s.models.Message
import org.slf4j.LoggerFactory.getLogger
import ru.botoss.telegram.model.{Command, Request, Response}

import scala.concurrent.ExecutionContext

class Bot(queueProxyActor: ActorRef)(implicit env: Environment)
  extends BotBase with Polling with Commands with StrictLogging {

  override implicit val system: ActorSystem = env.system
  override implicit val materializer: ActorMaterializer = env.materializer
  override implicit val executionContext: ExecutionContext = env.executionContext

  override lazy val token: String = env.config.getString("telegram.bot.token")
  override val client: RequestHandler = new AkkaClient(token)
  override val logger: Logger = Logger(getLogger(getClass.getName))

  onMessage { implicit msg =>
    using(command) { cmd =>
      withArgs { args =>
        system.actorOf(ConnectorActor.props()) ! Request(Command(cmd, args))
      }
    }
  }

  private class ConnectorActor()(implicit msg: Message) extends Actor {
    override def receive: Receive = {
      case request: Request =>
        queueProxyActor ! request
      case Response(text) =>
        reply(text)
        context.stop(self)
    }
  }

  private object ConnectorActor {
    def props()(implicit msg: Message): Props = Props(new ConnectorActor())
  }
}
