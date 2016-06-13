package robobot.webapp

// TODO: do these classes really contain the values Viz needs to use?

// TODO: s/sealed abstract class/sealed trait/g ?
sealed trait Animation

case class MoveAnimation(bot: Bot, row: Double, col: Double) extends Animation

case class TurnAnimation(bot: Bot, angle: Double) extends Animation
