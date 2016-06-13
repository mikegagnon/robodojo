package robobot.webapp

// TODO: do these classes really contain the values Viz needs to use?

// TODO: s/sealed abstract class/sealed trait/g ?
sealed trait Animation

case class MoveAnimation(botId: Long, row: Double, col: Double) extends Animation

case class TurnAnimation(start: Direction.EnumVal, end: Direction.EnumVal) extends Animation
