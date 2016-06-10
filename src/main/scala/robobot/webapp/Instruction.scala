package robobot.webapp

sealed abstract class Instruction {
  val instructionSet: Int
  val cycles: Int

  def execute(bot: Bot) : Option[Animation]
}

case class MoveInstruction(implicit val config: Config) extends Instruction {
  val instructionSet = 0
  val cycles = config.sim.moveCycles

  // TODO: test
  def execute(bot: Bot) = {

    val start = RowCol(bot.row, bot.col)

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)

    bot.board.matrix(row)(col) match {
      case None => {
        bot.board.moveBot(bot, row, col)
        Some(MoveAnimation(start, RowCol(row, col)))
      }
      case Some(_) => None
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
case class TurnInstruction(direction: Int)(implicit val config: Config) extends Instruction {

    val instructionSet = 0
    val cycles = config.sim.turnCycles

    def execute(bot: Bot) = {

      val start = bot.direction

      bot.direction = direction match {
        case 0 => Direction.rotateLeft(bot.direction)
        case _ => Direction.rotateRight(bot.direction)
      }

      val end = bot.direction

      Some(TurnAnimation(start, end))
    }
}