package club.robodojo

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.

sealed trait Animation {
  val botId: Long
}

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

// TODO: is this necessary?
sealed trait BirthAnimation extends Animation

case class BirthAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends BirthAnimation

case class BirthAnimationFail(
  botId: Long,
  cycleNum: Int) extends BirthAnimation

case class BirthAnimationSucceed(
  botId: Long,
  newBotId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends BirthAnimation
