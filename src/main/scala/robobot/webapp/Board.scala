package robobot.webapp

import scala.collection.mutable.ListBuffer

class Board(implicit val config: Config) {

  val matrix = Array.fill[Option[Bot]](config.numRows, config.numCols)(None)

  val bots = new ListBuffer[Bot]()

  def addBot(bot: Bot) = {

    if (bot.row < 0 || bot.row >= config.numRows ||
        bot.col < 0 || bot.col >= config.numCols) {
      throw new IllegalArgumentException("Cannot add bot; it is out of bounds")
    }

    matrix(bot.row)(bot.col) match {
      case None => {
        matrix(bot.row)(bot.col) = Some(bot)
        bots.append(bot)
      }
      case Some(_) => throw new IllegalArgumentException("matrix(r)(c) is already occupied")
    }
  }

  def cycle = {
    bots.foreach( (bot: Bot) => bot.cycle() )
  }

}