package club.robodojo


// TODO: only execute instructions if the bot has the proper instruction set
// TODO: document instruction set
object InstructionSet {
  sealed trait EnumVal
  case object Basic extends EnumVal
  case object Extended extends EnumVal
}

sealed abstract class Instruction {

  val instructionSet: InstructionSet.EnumVal
  val requiredCycles: Int

  def cycle(bot: Bot, cycleNum: Int) : Option[Animation]
}

// TODO: move appropriate functions into objects

case class MoveInstruction(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  val requiredCycles = config.sim.moveCycles

  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimationProgress(bot.id, cycleNum, requiredCycles, bot.row, bot.col, destRow, destCol,
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
        Some(MoveAnimationSucceed(bot.id))
      }
      case Some(_) => Some(MoveAnimationFail(bot.id))
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
case class TurnInstruction(leftOrRight: Direction.EnumVal)(implicit val config: Config) extends Instruction {

    val instructionSet = InstructionSet.Basic
    val requiredCycles = config.sim.turnCycles

    def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
      if (cycleNum == requiredCycles) {
        return execute(bot)
      } else if (cycleNum > requiredCycles) {
        throw new IllegalArgumentException("cycleNum > requiredCycles")
      } else {
        return Some(TurnAnimation(bot.id, cycleNum, bot.direction, leftOrRight))
      }

    def getNewDirection(currentDir: Direction.EnumVal): Direction.EnumVal =
      leftOrRight match {
        case Direction.Left => Direction.rotateLeft(currentDir)
        case Direction.Right => Direction.rotateRight(currentDir)
        case _ => throw new IllegalArgumentException("leftOrRight == " + leftOrRight)
      }

    def execute(bot: Bot): Option[Animation] = {

      val oldDirection = bot.direction

      bot.direction = getNewDirection(bot.direction)

      Some(TurnAnimation(bot.id, requiredCycles, oldDirection, leftOrRight))
    }
}

// TODO: take params as ParamValue objects?
// TODO: Prevent FAT hack
case class CreateInstruction(
    childInstructionSet: InstructionSet.EnumVal,
    numBanks: Int,
    mobile: Boolean,
    lineNumber: Int,
    // Who's program did this instruction come from originally?
    // Note this is different than bot.playerColor, which is the color of the bot.
    // For example, if a blue bot infects a red bot with a bank that has as bad create instruction,
    // then bot.playerColor == red and playerColor, here, == blue.
    playerColor: PlayerColor.EnumVal)(implicit val config: Config) extends Instruction {

  if (numBanks < 1 && config.compiler.safetyChecks) {
    throw new IllegalArgumentException("numBanks < 1 == " + numBanks)
  }

  val instructionSet = InstructionSet.Basic

  // TODO: will need to change to def if take ParamValue objects as params
  // TODO: what if numBanks, etc., is wonky? Then we get wonky requiredCycles. Check for that.
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

      return Some(BirthAnimationProgress(
        bot.id,
        cycleNum,
        requiredCycles,
        bot.row,
        bot.col,
        destRow,
        destCol,
        bot.direction))
    }

  def errorCheck(bot: Bot): Option[Animation] = {
    if (numBanks <= 0 || numBanks > config.sim.maxBanks) {

      val errorCode = ErrorCode.InvalidParameter
      val message = s"<p><span class='display-failure'>Error at line ${lineNumber + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it attempted to " +
        s"execute a <tt>create</tt> instruction with <tt>numBanks</tt> equal to ${numBanks}. " +
        s"<tt>numBanks</tt> must be greater than 0 and less than (or equal to) " +
        s"${config.sim.maxBanks}.</p>"
      val errorMessage = ErrorMessage(errorCode, lineNumber, message)

      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
    } else {
      None
    }
  }

  def execute(bot: Bot): Option[Animation] = {

    val error: Option[Animation] = errorCheck(bot)

    if (error.nonEmpty) {
      bot.board.removeBot(bot)
      return error
    }

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        val emptyProgram = Program.emptyProgram(numBanks)

        val active = false

        val newBot = Bot(
          bot.board,
          bot.playerColor,
          row,
          col,
          bot.direction,
          emptyProgram,
          childInstructionSet,
          mobile,
          active)

        bot.board.addBot(newBot)

        Some(BirthAnimationSucceed(
          bot.id,
          newBot.id,
          newBot.playerColor,
          newBot.row,
          newBot.col,
          newBot.direction))
      }
      case Some(_) => Some(BirthAnimationFail(bot.id))
    }
  }
}
