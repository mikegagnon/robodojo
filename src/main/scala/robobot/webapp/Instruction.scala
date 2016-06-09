package robobot.webapp

sealed abstract class Instruction

sealed trait InstructionSet {
  val instructionSet: Int
}

object MoveInstruction extends InstructionSet {
  val instructionSet = 0
}

case class MoveInstruction() extends Instruction

object TurnInstruction extends InstructionSet {
  val instructionSet = 0
}

// TODO: where does the definition of Variable belong?
case class Variable(id: Int)

case class TurnInstruction(direction: Either[Int, Variable]) extends Instruction