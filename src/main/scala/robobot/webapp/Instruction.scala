package robobot.webapp

sealed abstract class Instruction {
  val instructionSet: Int
  val cycles: Int

  def execute(bot: Bot) : Unit
}

case class MoveInstruction(implicit val config: Config) extends Instruction {
  val instructionSet = 0
  val cycles = config.moveCycles

  // TODO: test
  def execute(bot: Bot) {

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)

    bot.board.matrix(row)(col) match {
      case None => bot.board.moveBot(bot, row, col)
      case Some(_) => ()
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
    case Left(v) => if (v < 0 || v >= config.simMaxNumVariables) {
      throw new IllegalArgumentException("variable value out of range: " + v)
    }
    case _ => ()
  }
}

case class TurnInstruction(direction: ParamValue)(implicit val config: Config) extends Instruction {
    val instructionSet = 0
    val cycles = config.turnCycles

    def execute(bot: Bot) {

    }
}