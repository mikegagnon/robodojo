package club.robodojo

import scala.collection.mutable

class Board(implicit val config: Config) {

  val matrix = Array.fill[Option[Bot]](config.sim.numRows, config.sim.numCols)(None)

  var bots = new mutable.ArrayBuffer[Bot]()

  var cycleNum = 0

  var botCount = mutable.Map[PlayerColor.EnumVal, Int]()

  var victor: Option[PlayerColor.EnumVal] = None

  val originalPlayerColors = mutable.Set[PlayerColor.EnumVal]()

  def deepCopy(): Board = {
    val newBoard = new Board()
    newBoard.cycleNum = cycleNum

    bots.foreach { bot =>
      val newBot = bot.deepCopy(newBoard)
      newBoard.addBot(newBot)
    }

    newBoard.botCount = botCount
    newBoard.victor = victor

    return newBoard
  }

  // TESTED
  def addBot(bot: Bot): Unit =
    matrix(bot.row)(bot.col) match {
      case None => {
        matrix(bot.row)(bot.col) = Some(bot)
        bots += bot


        if (!botCount.contains(bot.playerColor)) {
          botCount(bot.playerColor) = 0
        }

        botCount(bot.playerColor) += 1

        originalPlayerColors.add(bot.playerColor) 
      }
      case Some(_) => throw new IllegalArgumentException("matrix(r)(c) is already occupied")
    }

  // TESTED
  def removeBot(bot: Bot): Unit = {
    matrix(bot.row)(bot.col) = None
    bots = bots.filter { b =>
      b.id != bot.id
    }

    botCount(bot.playerColor) -= 1

    checkVictory()
  }

  def checkVictory(): Unit = {

    if (originalPlayerColors.size == 1) {
      return
    }

    var colors: List[PlayerColor.EnumVal] = PlayerColor
      .colors
      .flatMap { color: PlayerColor.EnumVal =>
        if (botCount.getOrElse(color, 0) == 0) {
          None
        } else {
          Some(color)
        }
      }

    if (colors.length == 1) {
      victor = Some(colors(0))
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
  def cycle(): mutable.ArrayBuffer[Animation] = {
    cycleNum += 1

    // TODO: this might be empty if all bots return None, which seems to causes exceptions
    bots.flatMap{ (bot: Bot) => bot.cycle() }
  }

  // TODO: use different data structure for constant performance?
  def getBot(botId: Long): Option[Bot] = {
    bots.find { bot: Bot => bot.id == botId }
  }

}