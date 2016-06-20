package club.robodojo

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.

sealed trait Animation {
  val botId: Long
}

sealed trait BotImageMoveAnimation extends Animation {
  val botId: Long
  val cycleNum: Int
  val requiredCycles: Int
  val oldRow: Int
  val oldCol: Int
  val newRow: Int
  val newCol: Int
  val direction: Direction.EnumVal
}

// TODO: document
case class MoveAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends BotImageMoveAnimation

/*case class MoveAnimationSucceed(
  botId: Long,
  newBotId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends BirthAnimation
*/
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
  direction: Direction.EnumVal) extends BirthAnimation with BotImageMoveAnimation

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
