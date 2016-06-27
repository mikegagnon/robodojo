package club.robodojo

import scala.collection.mutable.ArrayBuffer

object Bot {

  var nextId: Long = 0

  def getNextId: Long = {
    val id = nextId
    nextId += 1
    id
  }

  // TODO: replace with setter functions?
  def apply(board: Board,
            playerColor: PlayerColor.EnumVal,
            row: Int,
            col: Int,
            direction: Direction.EnumVal = Direction.NoDir,
            program: Program = Program(Map[Int, Bank]()),
            instructionSet: InstructionSet.EnumVal = InstructionSet.Basic,
            mobile: Boolean = true,
            active: Short = 1)(implicit config: Config): Bot = {

    val bot = new Bot(board, playerColor)
    bot.row = row
    bot.col = col
    bot.direction = direction
    bot.program = program
    bot.instructionSet = instructionSet
    bot.mobile = mobile
    bot.active = active
    return bot
  }

}

// TODO: bots can only execute instructions from their instruction set
// TODO: bots can only move if they are mobile
class Bot(val board: Board, val playerColor: PlayerColor.EnumVal)(implicit val config: Config) {

  val id = Bot.getNextId

  override def toString: String = "Bot" + id

  var row = -1

  var col = -1

  var direction: Direction.EnumVal = Direction.NoDir

  var program = Program(Map[Int, Bank]())

  var instructionSet: InstructionSet.EnumVal = InstructionSet.Basic

  var mobile = false

  var active: Short = 0

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  // The number of cycles that must be executed before executing the current instruction
  var requiredCycles = 0

  var registers = ArrayBuffer.fill(config.sim.maxNumVariables)(0.toShort)

  def getRemote(): Option[Bot] = {
    val RowCol(remoteRow, remoteCol) = Direction.dirRowCol(direction, row, col)
    return board.matrix(remoteRow)(remoteCol)
  }

  // TESTED
  def cycle(): Option[Animation] = {

    if (active < 1) {
      return None
    }

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
      val errorCode = ErrorCode.DataHunger

      val message = s"<p><span class='display-failure'>Error in ${playerColor}'s bot' " +
        s"located at row ${row + 1}, column ${col + 1}</span>: " +
        s"The ${playerColor} bot has tapped out because it attempted to " +
        s"execute an empty bank (bank ${bankNum + 1})."

      val errorMessage = ErrorMessage(errorCode, 0, message)

      board.removeBot(this)

      return Some(FatalErrorAnimation(id, playerColor, row, col, errorMessage))
    }
  }

}