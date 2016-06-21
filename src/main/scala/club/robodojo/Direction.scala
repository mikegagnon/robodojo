package club.robodojo

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

  // TESTED
  // Assuming the bot is ar (row, col), pointing in direction dir, then where would it try to move
  // if it executed a move instruction?
  def dirRowCol(direction: Direction.EnumVal, row: Int, col: Int)(implicit config: Config): RowCol
      = {

    if (row < 0 || row >= config.sim.numRows || col < 0 || col >= config.sim.numCols) {
      throw new IllegalArgumentException("row, col is out of bounds")
    }

    val rc = direction match {
      case Direction.Up => RowCol(row - 1, col)
      case Direction.Down => RowCol(row + 1, col)
      case Direction.Left => RowCol(row, col - 1)
      case Direction.Right => RowCol(row, col + 1)
      case Direction.NoDir =>
        throw new IllegalArgumentException("Cannot compute dirRowCol for NoDir")
    }

    if (rc.row == -1) {
      RowCol(config.sim.numRows - 1, rc.col)
    } else if (rc.row == config.sim.numRows) {
      RowCol(0, rc.col)
    } else if (rc.col == -1)  {
      RowCol(rc.row, config.sim.numCols - 1)
    } else if (rc.col == config.sim.numCols) {
      RowCol(rc.row, 0)
    } else {
      rc
    }
  }

}
