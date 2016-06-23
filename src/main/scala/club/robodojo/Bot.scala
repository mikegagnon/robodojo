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
            active: Boolean = true)(implicit config: Config): Bot = {

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

  var active = false

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  // TODO: test
  var registers = new ArrayBuffer[Int](config.sim.maxNumVariables)

  // TESTED
  def cycle(): Option[Animation] = {

    if (!active) {
      return Option(InactiveAnimation(id))
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
      // TODO: tapout bot?
      return None
    }
  }

}