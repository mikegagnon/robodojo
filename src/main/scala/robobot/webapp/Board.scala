package robobot.webapp

import scala.collection.mutable.ArrayBuffer

class Board(implicit val config: Config) {

  val matrix = Array.fill[Option[Bot]](config.sim.numRows, config.sim.numCols)(None)

  val bots = new ArrayBuffer[Bot]()

  var cycleNum = 0

  def addBot(bot: Bot): Unit =
    matrix(bot.row)(bot.col) match {
      case None => {
        matrix(bot.row)(bot.col) = Some(bot)
        bots += bot
      }
      case Some(_) => throw new IllegalArgumentException("matrix(r)(c) is already occupied")
    }

  def moveBot(bot: Bot, row: Int, col: Int): Unit =
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

  def cycle(): ArrayBuffer[Animation] = {
    cycleNum += 1
    bots.flatMap{ (bot: Bot) => bot.cycle() }
  }

}