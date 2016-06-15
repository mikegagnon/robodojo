package robobot.webapp

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution.

sealed trait Animation {
  val botId: Long
  val cycleNum: Int
}

// TODO: move cycleNum to below botId
case class MoveAnimation(
  botId: Long,
  cycleNum: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends Animation

case class TurnAnimation(
  botId: Long,
  cycleNum: Int,
  oldDirection: Direction.EnumVal,
  leftOrRight: Direction.EnumVal) extends Animation
