package club.robodojo

// TODO: cleanup and document

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.

sealed trait Animation {
  val botId: Long
}

// TODO: rename?
sealed trait AnimationProgress extends Animation {
  val botId: Long
  val cycleNum: Int
  val requiredCycles: Int
  val oldRow: Int
  val oldCol: Int
  val newRow: Int
  val newCol: Int
  val direction: Direction.EnumVal
}

sealed trait AnimationProgressSucceed extends Animation {
  val botId: Long
  val row: Int
  val col: Int
  val direction: Direction.EnumVal
}

sealed trait MoveAnimation extends Animation

// TODO: document
case class MoveAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends AnimationProgress with MoveAnimation

// TODO: rm cycleNum?
case class MoveAnimationFail(
  botId: Long,
  cycleNum: Int) extends BirthAnimation

// TODO: are these fields really necessary
case class MoveAnimationSucceed(
  botId: Long,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends MoveAnimation with AnimationProgressSucceed

case class TurnAnimation(
  botId: Long,
  cycleNum: Int,
  oldDirection: Direction.EnumVal,
  leftOrRight: Direction.EnumVal) extends Animation

sealed trait BirthAnimation extends Animation

case class BirthAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgress

// TODO: rm cycleNum?
case class BirthAnimationFail(
  botId: Long,
  cycleNum: Int) extends BirthAnimation

// TODO: are all these fields necessary?
case class BirthAnimationSucceed(
  botId: Long,
  newBotId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgressSucceed
