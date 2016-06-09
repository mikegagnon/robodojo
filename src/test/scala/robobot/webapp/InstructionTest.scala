package robobot.webapp

import utest._

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  def tests = TestSuite {
    
    "Variable with Int == 0"-{
      Variable(Left(0))
    }

    "Variable with Int == simMaxNumVariables - 1"-{
      Variable(Left(config.simMaxNumVariables - 1))
    }

    "Variable with Int == -1"-{
      intercept[IllegalArgumentException] {
        Variable(Left(-1))
      }
    }

    "Variable with Int == simMaxNumVariables"-{
      intercept[IllegalArgumentException] {
        Variable(Left(config.simMaxNumVariables))
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
