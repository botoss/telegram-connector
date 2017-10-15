package ru.botoss.telegram

import info.mukel.telegrambot4s.api.Extractors._
import info.mukel.telegrambot4s.api.declarative.ToCommand
import info.mukel.telegrambot4s.models.Message

trait CommandExtractors {

  def command(msg: Message): Option[TelegramCommand] =
    textTokens(msg)
      .map(_.head)
      .filter(_.startsWith(ToCommand.CommandPrefix))
      .map(ToCommand.cleanCommand)
}
