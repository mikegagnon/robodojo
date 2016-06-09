package robobot.webapp

object Bot {

  var nextId: Long = 0

  def getNextId = {
    val id = nextId
    nextId += 1
    id
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

  var row = -1

  var col = -1

  var direction: Direction.EnumVal = Direction.NoDir

  var banks = Map[Int, Bank]()

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  def cycle() = {

    var bank = banks(bankNum)

    val instruction = bank.instructions(instructionNum)

    if (cycleNum == instruction.cycles) {

      cycleNum = 0
      instructionNum += 1

      if (instructionNum == bank.instructions.length) {
        instructionNum = 0
      }

      instruction.execute(this, board)
    } else {
      cycleNum += 1
    }

  }

}