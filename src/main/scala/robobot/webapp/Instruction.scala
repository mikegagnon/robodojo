package robobot.webapp

sealed abstract class Instruction {
  val instructionSet: Int
  val requiredCycles: Int

  def cycle(bot: Bot, cycleNum: Int) : Option[Animation]

}

case class MoveInstruction(implicit val config: Config) extends Instruction {
  val instructionSet = 0

  val requiredCycles = config.sim.moveCycles

  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =

    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimation(bot, destRow, destCol, cycleNum))
    }

  // TODO: test
  def execute(bot: Bot): Option[Animation] = {

    val start = RowCol(bot.row, bot.col)

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)

    bot.board.matrix(row)(col) match {
      case None => {
        bot.board.moveBot(bot, row, col)
        Some(MoveAnimation(bot, row, col, requiredCycles))
      }
      case Some(_) => Some(MoveAnimation(bot, bot.row, bot.col, requiredCycles))
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
// TODO: reimplement
// TODO: rename direction param
case class TurnInstruction(direction: Int)(implicit val config: Config) extends Instruction {

    val instructionSet = 0
    val requiredCycles = config.sim.turnCycles
    val turnDirection = direction match {
        case 0 => Direction.Left
        case _ => Direction.Right
      }

    def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
      if (cycleNum == requiredCycles) {
        return execute(bot)
      } else if (cycleNum > requiredCycles) {
        throw new IllegalArgumentException("cycleNum > requiredCycles")
      } else {
        return Some(TurnAnimation(bot, bot.direction, turnDirection, cycleNum))
      }


    def getNewDirection(currentDir: Direction.EnumVal): Direction.EnumVal =
      direction match {
        case 0 => Direction.rotateLeft(currentDir)
        case _ => Direction.rotateRight(currentDir)
      }

    def execute(bot: Bot): Option[Animation] = {

      val oldDirection = bot.direction

      bot.direction = getNewDirection(bot.direction)

      Some(TurnAnimation(bot, oldDirection, turnDirection, requiredCycles))
    }
}