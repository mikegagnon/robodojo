package robobot.webapp

object Direction {
  sealed trait EnumVal
  case object Up extends EnumVal
  case object Down extends EnumVal
  case object Left extends EnumVal
  case object Right extends EnumVal
  case object NoDir extends EnumVal

  def toAngle(direction: EnumVal) = 
    direction match {
      case Up => 0
      case Down => 180
      case Left => 270
      case Right => 90
      case NoDir => throw new IllegalArgumentException("Cannot convert NoDir to an angle")
    }
}