package club.robodojo

object Bot {

  var nextId: Long = 0

  def getNextId: Long = {
    val id = nextId
    nextId += 1
    id
  }

  def apply(board: Board,
            playerColor: PlayerColor.EnumVal,
            row: Int,
            col: Int,
            direction: Direction.EnumVal = Direction.NoDir,
            program: Program = Program(Map[Int, Bank]())): Bot = {

    val bot = new Bot(board, playerColor)
    bot.row = row
    bot.col = col
    bot.direction = direction
    bot.program = program
    return bot
  }

}

class Bot(val board: Board, val playerColor: PlayerColor.EnumVal) {

  val id = Bot.getNextId

  override def toString: String = "Bot" + id

  var row = -1

  var col = -1

  var direction: Direction.EnumVal = Direction.NoDir

  var program = Program(Map[Int, Bank]())

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  // TESTED
  def cycle(): Option[Animation] = {

    var bank = program.banks(bankNum)

    if (bank.instructions.length > 0) {

      val instruction = bank.instructions(instructionNum)

      val animation: Option[Animation] = instruction.cycle(this, cycleNum)

      if (cycleNum == instruction.requiredCycles) {

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