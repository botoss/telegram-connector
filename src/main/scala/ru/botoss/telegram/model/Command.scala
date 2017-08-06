package ru.botoss.telegram.model

case class Command(command: CommandName, params: Seq[Param])
