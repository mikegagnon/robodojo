package robobot.webapp

import scala.collection.mutable.HashSet

class Board(implicit val config: Config) {

  val matrix = Array.fill[Option[Bot]](config.sim.numRows, config.sim.numCols)(None)

  val bots = new HashSet[Bot]()

  def addBot(bot: Bot) = {

    if (bots.contains(bot)) {
      throw new IllegalArgumentException("Board already contains botd")
    }

    if (bot.row < 0 || bot.row >= config.sim.numRows ||
        bot.col < 0 || bot.col >= config.sim.numCols) {
      throw new IllegalArgumentException("Cannot add bot; it is out of bounds")
    }

    matrix(bot.row)(bot.col) match {
      case None => {
        matrix(bot.row)(bot.col) = Some(bot)
        bots += bot
      }
      case Some(_) => throw new IllegalArgumentException("matrix(r)(c) is already occupied")
    }
  }

  def moveBot(bot: Bot, row: Int, col: Int) {
    matrix(row)(col) match {
      case None => {
        matrix(row)(col) = Some(bot)
        matrix(bot.row)(bot.col) = None
        bot.row = row
        bot.col = col
      }
      case Some(_) => throw new IllegalArgumentException("Cannot move bot: matrix(r)(c) is " +
        "already occupied")
    }
  }

  // TODO: test
  def cycle(): List[Animation] = {
    // TODO: is toList cast unncessary?
    bots.toList.flatMap{ (bot: Bot) => bot.cycle() }
  }

}