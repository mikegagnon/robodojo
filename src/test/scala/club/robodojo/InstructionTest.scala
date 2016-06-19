package club.robodojo

import utest._

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  def tests = TestSuite {
    "dirRowCol"-{

      "no wrap around"-{

        "dir=Up"-{
          MoveInstruction.dirRowCol(Direction.Up, 1, 1) ==> RowCol(0, 1)
        }

        "dir=Down"-{
          MoveInstruction.dirRowCol(Direction.Down, 1, 1) ==> RowCol(2, 1)
        }

        "dir=Left"-{
          MoveInstruction.dirRowCol(Direction.Left, 1, 1) ==> RowCol(1, 0)
        }

        "dir=Right"-{
          MoveInstruction.dirRowCol(Direction.Right, 1, 1) ==> RowCol(1, 2)
        }
      }

      "wrap around"-{

        "dir=Up"-{
          MoveInstruction.dirRowCol(Direction.Up, 0, 0) ==> RowCol(config.sim.numRows - 1, 0)
        }

        "dir=Down"-{
          MoveInstruction.dirRowCol(Direction.Down, config.sim.numRows - 1, 0) ==> RowCol(0, 0)
        }

        "dir=Left"-{
          MoveInstruction.dirRowCol(Direction.Left, 0, 0) ==> RowCol(0, config.sim.numCols - 1)
        }

        "dir=Right"-{
          MoveInstruction.dirRowCol(Direction.Right, 0, config.sim.numCols - 1) ==> RowCol(0, 0)
        }
      }

      "exceptions"-{

        "out of bounds" -{

          "above"-{
            intercept[IllegalArgumentException] {
              MoveInstruction.dirRowCol(Direction.Up, -1, 0)
            }
          }

          "below"-{
            intercept[IllegalArgumentException] {
              MoveInstruction.dirRowCol(Direction.Up, config.sim.numRows, 0)
            }
          }

          "to the left"-{
            intercept[IllegalArgumentException] {
              MoveInstruction.dirRowCol(Direction.Up, 0, -1)
            }
          }

          "to the right"-{
            intercept[IllegalArgumentException] {
              MoveInstruction.dirRowCol(Direction.Up, 0, config.sim.numCols)
            }
          }
        }

        "NoDir"-{
          intercept[IllegalArgumentException] {
            MoveInstruction.dirRowCol(Direction.NoDir, 0, 0)
          }
        }
      }
    }


    "Variable"-{
      "Variable with Int == 0"-{
        Variable(Left(0))
      }

      "Variable with Int == sim.maxNumVariables - 1"-{
        Variable(Left(config.sim.maxNumVariables - 1))
      }

      "Variable with Int == -1"-{
        intercept[IllegalArgumentException] {
          Variable(Left(-1))
        }
      }

      "Variable with Int == sim.maxNumVariables"-{
        intercept[IllegalArgumentException] {
          Variable(Left(config.sim.maxNumVariables))
        }
      }
    }

    "MoveInstruction.execute"-{
      "successfully"-{
        val board = new Board()
        val bot = Bot(board, 0, 0)
        bot.direction = Direction.Right

        val moveInstruction = MoveInstruction()

        moveInstruction.execute(bot)

        board.matrix(0)(0) ==> None
        board.matrix(0)(1) ==> Some(bot)
        bot.row ==> 0
        bot.col ==> 1
      }

      "unsuccessfully"-{
        val board = new Board()

        val bot1 = Bot(board, 0, 0)
        val bot2 = Bot(board, 0, 1)

        board.addBot(bot1)
        board.addBot(bot2)

        bot1.direction = Direction.Right

        val moveInstruction = MoveInstruction()

        moveInstruction.execute(bot1)

        board.matrix(0)(0) ==> Some(bot1)
        board.matrix(0)(1) ==> Some(bot2)
        bot1.row ==> 0
        bot1.col ==> 0
      }
    }

    "TurnInstruction.execute"-{

      val board = new Board()
      val bot = Bot(board, 0, 0)

      "turn left"-{

        bot.direction = Direction.Up

        val turnInstruction = TurnInstruction(0)

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Left

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Down

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Right

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Up

      }

      "turn right"-{

        bot.direction = Direction.Up

        val turnInstruction = TurnInstruction(1)

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Right

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Down

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Left

        turnInstruction.execute(bot)
        bot.direction ==> Direction.Up

      }
    }
  }
}
