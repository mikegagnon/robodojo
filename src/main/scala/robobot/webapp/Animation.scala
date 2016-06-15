package robobot.webapp

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.

sealed trait Animation {
  val botId: Long
  val cycleNum: Int
}

case class MoveAnimation(
  botId: Long,
  cycleNum: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends Animation

// TODO: comment
// You might think that TurnAnimation doe
case class TurnAnimation(
  botId: Long,
  cycleNum: Int,
  oldDirection: Direction.EnumVal,
  row: Int,
  col: Int,
  leftOrRight: Direction.EnumVal) extends Animation
