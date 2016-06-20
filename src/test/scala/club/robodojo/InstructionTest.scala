package club.robodojo

import utest._

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  def tests = TestSuite {

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
        val bot = Bot(board, PlayerColor.Blue, 0, 0)
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

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0)
        val bot2 = Bot(board, PlayerColor.Blue, 0, 1)

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
      val bot = Bot(board, PlayerColor.Blue, 0, 0)

      "turn left"-{

        bot.direction = Direction.Up

        val turnInstruction = TurnInstruction(Direction.Left)

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

        val turnInstruction = TurnInstruction(Direction.Right)

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

    "CreateInstruction.execute"-{

      def testCase(
          board: Board,
          color: PlayerColor.EnumVal,
          instructionSet: InstructionSet.EnumVal,
          numBanks: Int,
          mobile: Boolean): Option[Bot] = {
        val bot = Bot(board, color, 0, 0, Direction.Right)
        val instruction = CreateInstruction(instructionSet, numBanks, mobile)
        instruction.execute(bot)
        return board.matrix(0)(1)
      }

      "successfully"-{
        "blue, basic, 5, false"-{
          val board = new Board()
          val newBot = testCase(board,PlayerColor.Blue, InstructionSet.Basic, 5, false).get
          newBot.playerColor ==> PlayerColor.Blue
          newBot.instructionSet ==> InstructionSet.Basic
          newBot.program.banks.size ==> 5
          newBot.mobile ==> false
        }
        "red, extended, 3, true"-{
          val board = new Board()
          val newBot = testCase(board, PlayerColor.Red, InstructionSet.Extended, 3, true).get
          newBot.playerColor ==> PlayerColor.Red
          newBot.instructionSet ==> InstructionSet.Extended
          newBot.program.banks.size ==> 3
          newBot.mobile ==> true
        }
      }

      "unsuccessfully"-{
        val board = new Board()
        val bot = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)
        board.addBot(bot)
        val newBot = testCase(board,PlayerColor.Red, InstructionSet.Basic, 5, false).get
        newBot.playerColor ==> PlayerColor.Blue
      }

    }
  }
}
