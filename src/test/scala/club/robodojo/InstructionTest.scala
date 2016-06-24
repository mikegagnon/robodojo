package club.robodojo

import utest._
import scala.collection.mutable.ArrayBuffer

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  def tests = TestSuite {

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
        val instruction = CreateInstruction(instructionSet, numBanks, mobile, 0, PlayerColor.Blue)
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

    // TODO: simplify
    // TODO: test animation
    // TODO: Tests:
    //    Dest params:
    //      registers (in bounds, out of bounds)
    //      #active
    //      %active
    //    Source params:
    //      IntegerParam
    //      #active
    //      %active
    //      $banks
    //      %banks
    //      $instrset
    //      %instrset
    //      $mobile
    //      %mobile
    //      $fields
    "SetInstruction.execute"-{

      "destination params"-{

        def testDestinationParam(destination: WriteableParam, source: Short)(test: (Bot => Unit)): Unit = {
          val instruction = SetInstruction(destination, IntegerParam(source))
          val board = new Board()
          val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right)
          board.addBot(bot1)
          val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)
          board.addBot(bot2)
          instruction.execute(bot1)
          test(bot1)
        }

        // destination register
        testDestinationParam(RegisterParam(0), 5) { _.registers(0) ==> 5 }

        // destination #active
        testDestinationParam(ActiveKeyword(true), 5) { _.active ==> 5 }

        // destination %active
        testDestinationParam(ActiveKeyword(false), 5) { _.board.matrix(0)(1).get.active ==> 5 }
      }
      "source params"-{

        def testSourceParam(source: ReadableParam, expectedValue: Short)
            (setup: ((Bot, Bot) => Unit)): Unit = {
          val instruction = SetInstruction(RegisterParam(0), source)
          val board = new Board()
          val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right)
          board.addBot(bot1)
          val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)
          board.addBot(bot2)

          setup(bot1, bot2)

          instruction.execute(bot1)
          bot1.registers(0) ==> expectedValue
        }

        // source Integer already covered by tests in "destination params"

        // #active
        testSourceParam(ActiveKeyword(true), 1) { (_, _) => () }

        // %active with remote bot
        testSourceParam(ActiveKeyword(false), 5) { (bot1, bot2) =>
          bot2.active = 5
        }

        // %active without remote bot
        testSourceParam(ActiveKeyword(false), 0) {
          (bot1, bot2) => bot1.direction = Direction.Left
        }

        // $banks
        testSourceParam(BanksKeyword(true), 2) { (bot1, _) =>
            bot1.program.banks = Map[Int, Bank](
              0 -> Bank(ArrayBuffer()),
              1 -> Bank(ArrayBuffer()))
        }

      }
    }
  }
}
