package robobot.webapp

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution.

sealed trait Animation {
  val bot: Bot
  val cycleNum: Int
}

case class MoveAnimation(
  bot: Bot,
  row: Int,
  col: Int,
  cycleNum: Int) extends Animation

case class TurnAnimation(
  bot: Bot,
  oldDirection: Direction.EnumVal,
  leftOrRight: Direction.EnumVal,
  cycleNum: Int) extends Animation
