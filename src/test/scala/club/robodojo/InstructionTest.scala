package club.robodojo

import utest._
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.IndexedSeq

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  val defaultSourceMap = SourceMapInstruction(0, 0)

  def tests = TestSuite {

    "MoveInstruction.execute"-{
      "successfully"-{
        val board = new Board()
        val bot = Bot(board, PlayerColor.Blue, 0, 0)
        bot.direction = Direction.Right

        val moveInstruction = MoveInstruction(defaultSourceMap)

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

        val moveInstruction = MoveInstruction(defaultSourceMap)

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

        val turnInstruction = TurnInstruction(defaultSourceMap, IntegerParam(0))

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

        val turnInstruction = TurnInstruction(defaultSourceMap, IntegerParam(1))

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

        val iSet = if(instructionSet == InstructionSet.Basic) IntegerParam(0) else IntegerParam(1)
        val banks = IntegerParam(numBanks.toShort)
        val mob = if (mobile) IntegerParam(1) else IntegerParam(0)
        val bot = Bot(board, color, 0, 0, Direction.Right)
        val instruction = CreateInstruction(defaultSourceMap, iSet, banks, mob, 0, PlayerColor.Blue)
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

    "SetInstruction.execute"-{

      val board = new Board()
      val bot = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)

      "getRequiredCycles"-{

        SetInstruction(defaultSourceMap, RegisterParam(0), RegisterParam(0))
          .getRequiredCycles(bot) ==> config.sim.cycleCount.durSet

        SetInstruction(defaultSourceMap, ActiveKeyword(false), RegisterParam(0))
          .getRequiredCycles(bot) ==>
          config.sim.cycleCount.durSet + config.sim.cycleCount.durRemoteAccessCost

        SetInstruction(defaultSourceMap, ActiveKeyword(true), BanksKeyword(false))
          .getRequiredCycles(bot) ==>
          config.sim.cycleCount.durSet + config.sim.cycleCount.durRemoteAccessCost

        SetInstruction(defaultSourceMap, ActiveKeyword(false), BanksKeyword(false))
          .getRequiredCycles(bot) ==>
          config.sim.cycleCount.durSet + config.sim.cycleCount.durRemoteAccessCost * 2

      }

      "destination params"-{

        def testDestinationParam(destination: WriteableParam, source: Short)(test: (Bot => Unit)): Unit = {
          val instruction = SetInstruction(defaultSourceMap, destination, IntegerParam(source))
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
          val instruction = SetInstruction(defaultSourceMap, RegisterParam(0), source)
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
        testSourceParam(ActiveKeyword(false), 5) { (_, bot2) =>
          bot2.active = 5
        }

        // %active without remote bot
        testSourceParam(ActiveKeyword(false), 0) {
          (bot1, _) => bot1.direction = Direction.Left
        }

        // $banks
        testSourceParam(BanksKeyword(true), 2) { (bot1, _) =>
            bot1.program.banks = Map[Int, Bank](
              0 -> Bank(IndexedSeq()),
              1 -> Bank(IndexedSeq()))
        }

        // %banks with remote bot
        testSourceParam(BanksKeyword(false), 3) { (_, bot2) =>
            bot2.program.banks = Map[Int, Bank](
              0 -> Bank(IndexedSeq()),
              1 -> Bank(IndexedSeq()),
              2 -> Bank(IndexedSeq()))
        }

        // %banks without remote bot
        testSourceParam(BanksKeyword(false), 0) {
          (bot1, _) => bot1.direction = Direction.Left
        }

        // $InstrSet basic
        testSourceParam(InstrSetKeyword(true), 0) { (bot1, _) =>
          bot1.instructionSet = InstructionSet.Basic
        }

        // $InstrSet extended
        testSourceParam(InstrSetKeyword(true), 1) { (bot1, _) =>
          bot1.instructionSet = InstructionSet.Extended
        }

        // %InstrSet with remote basic
        testSourceParam(InstrSetKeyword(false), 0) { (_, bot2) =>
          bot2.instructionSet = InstructionSet.Basic
        }

        // %InstrSet with remote extended
        testSourceParam(InstrSetKeyword(false), 1) { (_, bot2) =>
          bot2.instructionSet = InstructionSet.Extended
        }

        // %InstrSet without remote
        testSourceParam(InstrSetKeyword(false), 0) { (bot1, _) =>
          bot1.direction = Direction.Left
        }

        // $Mobile true
        testSourceParam(MobileKeyword(true), 1) { (bot1, _) =>
          bot1.mobile = true
        }

        // $Mobile false
        testSourceParam(MobileKeyword(true), 0) { (bot1, _) =>
          bot1.mobile = false
        }

        // %Mobile remote true
        testSourceParam(MobileKeyword(false), 1) { (_, bot2) =>
          bot2.mobile = true
        }

        // %Mobile remote false
        testSourceParam(MobileKeyword(false), 0) { (_, bot2) =>
          bot2.mobile = false
        }

        // %Mobile without remote
        testSourceParam(MobileKeyword(false), 0) { (bot1, _) =>
          bot1.direction = Direction.Left
        }

        // $Fields
        testSourceParam(FieldsKeyword(), config.sim.numRows.toShort) { (_, _) => }

      }
    }

    // TODO: test getRequiredCycles
    "TransInstruction.execute"-{

      // TODO: better name
      "test1"-{
        val board = new Board()

        val program1: Program = Compiler.compile("""
          bank zero
            trans 0, 0
            set #1, 1
            move

          bank one
            set %active, 1
            turn 1

          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)

        val program2: Program = Compiler.compile("""
          bank zero:
            move
          """, PlayerColor.Blue).right.get

        val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right, program2)

        board.addBot(bot1)
        board.addBot(bot2)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        assert(bot2.program.banks(0) != bot1.program.banks(0))

        instruction.execute(bot1)

        bot2.program.banks(0) ==> bot1.program.banks(0)
      }
    }
  }
}
