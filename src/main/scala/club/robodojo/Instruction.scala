package club.robodojo


object InstructionSet {
  sealed trait EnumVal
  case object Basic extends EnumVal
  case object Extended extends EnumVal
}

sealed abstract class Instruction {

  // TODO: document instruction set
  val instructionSet: InstructionSet.EnumVal
  val requiredCycles: Int

  def cycle(bot: Bot, cycleNum: Int) : Option[Animation]
}

// TODO: move appropriate functions into objects

case class MoveInstruction(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  val requiredCycles = config.sim.moveCycles

  // TODO: factor out common code?
  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimation(bot.id, cycleNum, bot.row, bot.col, destRow, destCol,
        bot.direction))
    }

  // TESTED
  def execute(bot: Bot): Option[Animation] = {

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        bot.board.moveBot(bot, row, col)
        Some(MoveAnimation(bot.id, requiredCycles, oldRow, oldCol, row, col, bot.direction))
      }
      case Some(_) => Some(MoveAnimation(bot.id, requiredCycles, oldRow, oldCol, oldRow, oldCol,
        bot.direction))
    }
  }
}

object Constant {
  sealed trait EnumVal
  case object Banks extends EnumVal
  case object Mobile extends EnumVal
  case object InstrSet extends EnumVal
  case object Fields extends EnumVal
}

case class ActiveVariable()

sealed trait Param
sealed trait ParamValue // Like Param, but without Label

final case class Integer(value: Short) extends Param with ParamValue
// TODO: should Label be under Param?
final case class Label(value: String) extends Param
final case class Constant(value: Constant.EnumVal) extends Param with ParamValue
final case class Remote(value: Constant.EnumVal) extends Param with ParamValue
final case class Variable(value: Either[Int, ActiveVariable])(implicit config: Config) extends Param
    with ParamValue {
  value match {
    case Left(v) => if (v < 0 || v >= config.sim.maxNumVariables) {
      throw new IllegalArgumentException("variable value out of range: " + v)
    }
    case _ => ()
  }
}

// TODO: take direction as a ParamValue?
// TODO: take leftOrRight as a Direction.EnumVal
case class TurnInstruction(leftOrRight: Int)(implicit val config: Config) extends Instruction {

    val instructionSet = InstructionSet.Basic
    val requiredCycles = config.sim.turnCycles
    val turnDirection = leftOrRight match {
        case 0 => Direction.Left
        case _ => Direction.Right
      }

    // TODO: factor out cycle and moveInstruction.cycle?
    def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
      if (cycleNum == requiredCycles) {
        return execute(bot)
      } else if (cycleNum > requiredCycles) {
        throw new IllegalArgumentException("cycleNum > requiredCycles")
      } else {
        return Some(TurnAnimation(bot.id, cycleNum, bot.direction, turnDirection))
      }

    // Executre
    def getNewDirection(currentDir: Direction.EnumVal): Direction.EnumVal =
      leftOrRight match {
        case 0 => Direction.rotateLeft(currentDir)
        case _ => Direction.rotateRight(currentDir)
      }

    def execute(bot: Bot): Option[Animation] = {

      val oldDirection = bot.direction

      bot.direction = getNewDirection(bot.direction)

      Some(TurnAnimation(bot.id, requiredCycles, oldDirection, turnDirection))
    }
}

// TODO: take params as ParamValue objects?
// TODO: test
case class CreateInstruction(
    childInstructionSet: InstructionSet.EnumVal,
    numBanks: Int,
    mobile: Boolean)(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: will need to change to def if take ParamValue objects as params
  val requiredCycles = {

    val durCreate1 = config.sim.durCreate1
    val durCreate2 = config.sim.durCreate2
    val durCreate3 = config.sim.durCreate3
    val durCreate3a = config.sim.durCreate3a
    val durCreate4 = config.sim.durCreate4
    val durCreate5 = config.sim.durCreate5
    val maxCreateDur = config.sim.maxCreateDur

    val primary = durCreate1 + durCreate2 * numBanks
    val mobilityCost = if (mobile) durCreate3 else 1 
    val secondaryMobilityCost = if (mobile) durCreate3a else 0

    val instructionSetCostBasic = childInstructionSet match {
      case InstructionSet.Basic => durCreate4
      case InstructionSet.Extended => 0
    }

    val instructionSetCostExtended = childInstructionSet match {
      case InstructionSet.Basic => 0
      case InstructionSet.Extended => durCreate5
    }

    val calculatedCost =
      primary * mobilityCost +
      secondaryMobilityCost +
      instructionSetCostBasic +
      instructionSetCostExtended

    Math.min(calculatedCost, maxCreateDur)
  }

  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(BirthAnimationProgress(bot.id, cycleNum, bot.row, bot.col, destRow, destCol,
        bot.direction))
    }

  // TODO: Test
  def execute(bot: Bot): Option[Animation] = {

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        val emptyProgram = Program.emptyProgram(numBanks)

        // TODO: create and set mobile flag
        val newBot = Bot(
          bot.board,
          bot.playerColor,
          row,
          col,
          bot.direction,
          emptyProgram)

        bot.board.addBot(newBot)

        Some(BirthAnimationProgress(bot.id, requiredCycles, oldRow, oldCol, row, col,
          bot.direction))
      }
      case Some(_) => Some(BirthAnimationFail(bot.id, requiredCycles))
    }

  }


}
