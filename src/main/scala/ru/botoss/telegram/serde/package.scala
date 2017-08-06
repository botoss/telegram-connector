package ru.botoss.telegram

import java.util.UUID

import ru.botoss.telegram.model.{Command, Key, Request, Response}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scalaz.Show
import scalaz.Show.showFromToString

package object serde {
  implicit val showKey: Show[Key] = showFromToString[Key]

  implicit val readKey: Read[Key] = UUID.fromString(_)

  implicit val commandFormat: JsonFormat[Command] = jsonFormat2(Command)

  implicit val requestFormat: JsonWriter[Request] =
    request => commandFormat.write(request.command)

  implicit val responseFormat: JsonFormat[Response] = jsonFormat1(Response)

  implicit def showJson[T: JsonWriter]: Show[T] =
    Show.shows[T](_.toJson.toString)

  implicit def readJson[T](implicit jsonReader: JsonReader[T]): Read[T] =
    s => jsonReader.read(s.parseJson)
}
