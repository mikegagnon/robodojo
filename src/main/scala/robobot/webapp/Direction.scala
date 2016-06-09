package robobot.webapp

// TODO: where to put this?
case class RowCol(row: Int, col:Int)

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

  def dirRowCol(direction: EnumVal, row: Int, col: Int)(implicit config: Config) = {

    if (row < 0 || row >= config.numRows || col < 0 || col >= config.numCols) {
      throw new IllegalArgumentException("row, col is out of bounds")
    }

    val rc = direction match {
      case Up => RowCol(row - 1, col)
      case Down => RowCol(row + 1, col)
      case Left => RowCol(row, col - 1)
      case Right => RowCol(row, col + 1)
      case NoDir => throw new IllegalArgumentException("Cannot compute dirRowCol for NoDir")
    }

    if (rc.row == -1) {
      RowCol(config.numRows - 1, rc.col)
    } else if (rc.row == config.numRows) {
      RowCol(0, rc.col)
    } else if (rc.col == -1)  {
      RowCol(rc.row, config.numCols - 1)
    } else if (rc.col == config.numCols) {
      RowCol(rc.row, 0)
    } else {
      rc
    }
  }


}