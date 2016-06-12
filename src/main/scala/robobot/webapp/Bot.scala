package robobot.webapp

object Bot {

  var nextId: Long = 0

  def getNextId = {
    val id = nextId
    nextId += 1
    id
  }

  // TODO: add direction to params?
  def apply(board: Board, row: Int, col: Int) = {
    val bot = new Bot(board)
    bot.row = row
    bot.col = col
    bot
  }

}

class Bot(val board: Board) {

  val id = Bot.getNextId

  // TODO: test
  override def equals(that: Any) = {
    that match {
      case thatBot: Bot => id == thatBot.id
      case _ => false
    }
  }

  override def hashCode:Int = id.toInt

  override def toString: String = "Bot" + id

  var row = -1

  var col = -1

  var direction: Direction.EnumVal = Direction.NoDir

  var banks = Map[Int, Bank](0 -> new Bank())

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  def cycle(): Option[Animation] = {

    var bank = banks(bankNum)

    if (bank.instructions.length > 0) {

      val instruction = bank.instructions(instructionNum)

      val animation: Option[Animation] = instruction.cycle(this, cycleNum)

      if (cycleNum == instruction.cycles) {

        cycleNum = 0
        instructionNum += 1

        if (instructionNum == bank.instructions.length) {
          instructionNum = 0
        }
      } else {
        cycleNum += 1
      }

      return animation
    } else {
      return None
    }
  }

}