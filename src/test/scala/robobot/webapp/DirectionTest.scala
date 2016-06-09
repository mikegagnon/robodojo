package robobot.webapp

import utest._

object DirectionTest extends TestSuite {

  implicit val config = Config.default
    
  val tests = this {

    "dirRowCol"-{
      
      "no wrap around"-{

        "dir=Up"-{
          Direction.dirRowCol(Direction.Up, 1, 1) ==> RowCol(0, 1)
        }

        "dir=Down"-{
          Direction.dirRowCol(Direction.Down, 1, 1) ==> RowCol(2, 1)
        }

        "dir=Left"-{
          Direction.dirRowCol(Direction.Left, 1, 1) ==> RowCol(1, 0)
        }

        "dir=Right"-{
          Direction.dirRowCol(Direction.Right, 1, 1) ==> RowCol(1, 2)
        }
      }

      "wrap around"-{

        "dir=Up"-{
          Direction.dirRowCol(Direction.Up, 0, 0) ==> RowCol(config.numRows - 1, 0)
        }

        "dir=Down"-{
          Direction.dirRowCol(Direction.Down, config.numRows - 1, 0) ==> RowCol(0, 0)
        }

        "dir=Left"-{
          Direction.dirRowCol(Direction.Left, 0, 0) ==> RowCol(0, config.numCols - 1)
        }

        "dir=Right"-{
          Direction.dirRowCol(Direction.Right, 0, config.numCols - 1) ==> RowCol(0, 0)
        }
      }

      "exceptions"-{

        "out of bounds" -{

          "above"-{
            intercept[IllegalArgumentException] {
              Direction.dirRowCol(Direction.Up, -1, 0)
            }
          }

          "below"-{
            intercept[IllegalArgumentException] {
              Direction.dirRowCol(Direction.Up, config.numRows, 0)
            }
          }

          "to the left"-{
            intercept[IllegalArgumentException] {
              Direction.dirRowCol(Direction.Up, 0, -1)
            }
          }

          "to the right"-{
            intercept[IllegalArgumentException] {
              Direction.dirRowCol(Direction.Up, 0, config.numCols)
            }
          }
        }

        "NoDir"-{
          intercept[IllegalArgumentException] {
            Direction.dirRowCol(Direction.NoDir, 0, 0)
          }
        }
      }

    }
  }
}