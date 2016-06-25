package club.robodojo

import scala.collection.mutable.ArrayBuffer

class Board(implicit val config: Config) {

  val matrix = Array.fill[Option[Bot]](config.sim.numRows, config.sim.numCols)(None)

  var bots = new ArrayBuffer[Bot]()

  var cycleNum = 0

  // TESTED
  def addBot(bot: Bot): Unit =
    matrix(bot.row)(bot.col) match {
      case None => {
        matrix(bot.row)(bot.col) = Some(bot)
        bots += bot
      }
      case Some(_) => throw new IllegalArgumentException("matrix(r)(c) is already occupied")
    }

  // TESTED
  def removeBot(bot: Bot): Unit = {
    matrix(bot.row)(bot.col) = None
    bots = bots.filter { b =>
      b.id != bot.id
    }
  }

  // TESTED
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

  // TODO: cycle bots sorted by id
  def cycle(): ArrayBuffer[Animation] = {
    cycleNum += 1

    // TODO: this might be empty if all bots return None, which seems to causes exceptions
    bots.flatMap{ (bot: Bot) => bot.cycle() }
  }

}