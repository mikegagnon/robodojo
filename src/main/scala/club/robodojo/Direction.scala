package club.robodojo

// TODO: where to put this?
case class RowCol(row: Int, col:Int)

object Direction {
  sealed trait EnumVal
  case object Up extends EnumVal
  case object Down extends EnumVal
  case object Left extends EnumVal
  case object Right extends EnumVal
  case object NoDir extends EnumVal

  def toAngle(direction: EnumVal): Int =
    direction match {
      case Up => 0
      case Down => 180
      case Left => 270
      case Right => 90
      case NoDir => throw new IllegalArgumentException("Cannot convert NoDir to an angle")
    }

  val rotateLeft = Map[Direction.EnumVal, Direction.EnumVal](
    Up -> Left,
    Left -> Down,
    Down -> Right,
    Right -> Up
  )

  val rotateRight = Map[Direction.EnumVal, Direction.EnumVal](
    Up -> Right,
    Right -> Down,
    Down -> Left,
    Left -> Up
  )

}
