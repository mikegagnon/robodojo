package club.robodojo

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.

sealed trait Animation {
  val botId: Long
}

/* Begin MoveAnimation and BirthAnimation definitions *********************************************/

sealed trait MoveAnimation extends Animation

sealed trait BirthAnimation extends Animation

// The move animation and the create animation (animateMoveProgress and animateBirthProgress,
// respectively) do the same thing. They animate a bot moving forward, and possibly wrapping around
// the torus. The only difference is that for the move animation, the image of bot moves forward,
// whereas for the birth animation, the image of the bot's child moves forward. Therefore, we
// implement animateBotImageProgress, which is generic in the sense that it animates both
// move and birth. To make this generic code work, we define AnimationProgress which is the parent
// of MoveAnimationProgress and BirthAnimationProgress.
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

// Like AnimationProgress, except for the case where the instruction succeeds
sealed trait AnimationProgressSucceed extends Animation

// Like AnimationProgressSucceed, except for the case when the instruction fails
sealed trait AnimationProgressFail extends Animation

case class MoveAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends AnimationProgress with MoveAnimation

case class BirthAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgress

case class MoveAnimationSucceed(
  botId: Long) extends MoveAnimation with AnimationProgressSucceed

case class BirthAnimationSucceed(
  botId: Long,
  newBotId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgressSucceed

case class MoveAnimationFail(
  botId: Long) extends BirthAnimation with AnimationProgressFail

case class BirthAnimationFail(
  botId: Long) extends BirthAnimation with AnimationProgressFail

/* End MoveAnimation and BirthAnimation definitions ***********************************************/

case class TurnAnimation(
  botId: Long,
  cycleNum: Int,
  oldDirection: Direction.EnumVal,
  leftOrRight: Direction.EnumVal) extends Animation
