package robobot.webapp

sealed abstract class Instruction

sealed trait InstructionSet {
  val instructionSet: Int
}

object MoveInstruction extends InstructionSet {
  val instructionSet = 0
}

case class MoveInstruction() extends Instruction

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

object TurnInstruction extends InstructionSet {
  val instructionSet = 0
}

case class TurnInstruction(direction: ParamValue) extends Instruction