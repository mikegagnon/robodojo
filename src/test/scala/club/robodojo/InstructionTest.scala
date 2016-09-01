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

        val moveInstruction = MoveInstruction(defaultSourceMap, 0, PlayerColor.Blue)

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

        val moveInstruction = MoveInstruction(defaultSourceMap, 0, PlayerColor.Blue)

        moveInstruction.execute(bot1)

        board.matrix(0)(0) ==> Some(bot1)
        board.matrix(0)(1) ==> Some(bot2)
        bot1.row ==> 0
        bot1.col ==> 0
      }

      "immobile"-{
        val board = new Board()

        val bot = Bot(board, PlayerColor.Blue, 0, 0)
        bot.mobile = false

        val moveInstruction = MoveInstruction(defaultSourceMap, 0, PlayerColor.Blue)

        val result = moveInstruction.execute(bot).get

        result match {
          case error: FatalErrorAnimation =>
            error.errorMessage.errorCode ==> ErrorCode.CannotMoveImmobile
          case _ => assert(false)
        }
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

        val iSet =
          if(instructionSet == InstructionSet.Basic) {
            IntegerParam(0)
          } else if (instructionSet == InstructionSet.Advanced) {
            IntegerParam(1)
          } else {
            IntegerParam(2)
          }
        val banks = IntegerParam(numBanks.toShort)
        val mob = if (mobile) IntegerParam(1) else IntegerParam(0)
        val bot = Bot(board, color, 0, 0, Direction.Right)
        bot.instructionSet = InstructionSet.Super
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
        "red, advanced, 3, true"-{
          val board = new Board()
          val newBot = testCase(board, PlayerColor.Red, InstructionSet.Advanced, 3, true).get
          newBot.playerColor ==> PlayerColor.Red
          newBot.instructionSet ==> InstructionSet.Advanced
          newBot.program.banks.size ==> 3
          newBot.mobile ==> true
        }
        "red, super, 3, true"-{
          val board = new Board()
          val newBot = testCase(board, PlayerColor.Red, InstructionSet.Super, 3, true).get
          newBot.playerColor ==> PlayerColor.Red
          newBot.instructionSet ==> InstructionSet.Super
          newBot.program.banks.size ==> 3
          newBot.mobile ==> true
        }
      }

      "unsuccessfully"-{
        "space occupied"-{
          val board = new Board()
          val bot = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)
          board.addBot(bot)
          val newBot = testCase(board,PlayerColor.Red, InstructionSet.Basic, 5, false).get
          newBot.playerColor ==> PlayerColor.Blue
        }
        "bad insufficient instruction set"-{

          val board = new Board()
          val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right)
          bot.instructionSet = InstructionSet.Advanced
          board.addBot(bot)

          val iSet = IntegerParam(0)
          val banks = IntegerParam(1)
          val mob = IntegerParam(1)

          val instruction = CreateInstruction(defaultSourceMap, iSet, banks, mob, 0, PlayerColor.Blue)

          val result: Option[Animation] = instruction.execute(bot)

          result match {
            case Some(FatalErrorAnimation(_, _, _, _,
                ErrorMessage(ErrorCode.InsufficientInstructionSet, _, _))) => assert(true)
            case _ => assert(false)
          }

        }
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

        // $InstrSet advanced
        testSourceParam(InstrSetKeyword(true), 1) { (bot1, _) =>
          bot1.instructionSet = InstructionSet.Advanced
        }

        // $InstrSet super
        testSourceParam(InstrSetKeyword(true), 2) { (bot1, _) =>
          bot1.instructionSet = InstructionSet.Super
        }

        // %InstrSet with remote basic
        testSourceParam(InstrSetKeyword(false), 0) { (_, bot2) =>
          bot2.instructionSet = InstructionSet.Basic
        }

        // %InstrSet with remote Advanced
        testSourceParam(InstrSetKeyword(false), 1) { (_, bot2) =>
          bot2.instructionSet = InstructionSet.Advanced
        }

        // %InstrSet with remote Advanced
        testSourceParam(InstrSetKeyword(false), 2) { (_, bot2) =>
          bot2.instructionSet = InstructionSet.Super
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
    // Test cases:
    //  successful transfer (1 -> 1, 1 -> 2, 2->1, 2->2)
    //  successful transfer, and bot is currently executing overwritten bank
    //  attempt transfer, but there is no origination bank
    //  attempt transfer, but there is no receiving bank
    //  attempt transfer, but there is no receiving bot
    //  attempt transfer, but bot has Basic instruction set
    "TransInstruction.execute"-{

      def successTransTest(sourceBank: Int, destBank: Int): Unit = {
        val board = new Board()

        val program1: Program = Compiler.compile(s"""
          bank one
            trans ${sourceBank}, ${destBank}
            set #1, 1
            move

          bank two
            set %active, 1
            turn 1

          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)
        bot1.instructionSet = InstructionSet.Advanced

        val program2: Program = Compiler.compile("""
          bank one
            move
            move

          bank two
            move
            move
          """, PlayerColor.Blue).right.get

        val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right, program2)
        bot2.bankIndex = 1
        bot2.instructionIndex = 1
        bot2.cycleNum = 5

        board.addBot(bot1)
        board.addBot(bot2)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        assert(bot2.program.banks(destBank - 1) != bot1.program.banks(sourceBank - 1))

        instruction.execute(bot1)

        bot2.program.banks(destBank - 1) ==> bot1.program.banks(sourceBank - 1)

        if (destBank - 1 == bot2.bankIndex) {
          bot2.instructionIndex ==> 0
          bot2.cycleNum ==> 1
        } else {
          bot2.instructionIndex ==> 1
          bot2.cycleNum ==> 5
        }
      }

      "successful transfers"-{
        successTransTest(1, 1)
        successTransTest(1, 2)
        successTransTest(2, 1)
        successTransTest(2, 2)
      }

      def failTransTest(sourceBank: Int): Unit = {
        val board = new Board()

        val program1: Program = Compiler.compile(s"""
          bank one
            trans ${sourceBank}, 1
            set #1, 1
            move

          bank two
            set %active, 1
            turn 1

          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)
        bot1.instructionSet = InstructionSet.Advanced
        val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)

        board.addBot(bot1)
        board.addBot(bot2)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        val result: Animation = instruction.execute(bot1).get

        result match {
          case error: FatalErrorAnimation =>
            error.errorMessage.errorCode ==> ErrorCode.InvalidParameter
          case _ => assert(false)
        }
      }

      "no source bank"-{
        failTransTest(0)
        failTransTest(3)
      }

      "no receiving bot"-{
        val board = new Board()

        val program1: Program = Compiler.compile(s"""
          bank one
            trans 1, 1
          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)
        bot1.instructionSet = InstructionSet.Advanced

        board.addBot(bot1)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        val result: Option[Animation] = instruction.execute(bot1)

        result ==> None
      }

      "no receiving bank"-{
        val board = new Board()

        val program1: Program = Compiler.compile(s"""
          bank one
            trans 1, -1
          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)
        bot1.instructionSet = InstructionSet.Advanced

        val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)

        board.addBot(bot1)
        board.addBot(bot2)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        val result: Option[Animation] = instruction.execute(bot1)

        result ==> None
      }

      "insufficient instruction set"-{
        val board = new Board()

        val program1: Program = Compiler.compile(s"""
          bank one
            trans 1, 1
          """, PlayerColor.Blue).right.get

        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program1)
        bot1.instructionSet = InstructionSet.Basic

        val bot2 = Bot(board, PlayerColor.Blue, 0, 1, Direction.Right)

        board.addBot(bot1)
        board.addBot(bot2)

        val instruction: Instruction = bot1.program.banks(0).instructions(0)

        val result: Animation = instruction.execute(bot1).get

        result match {
          case error: FatalErrorAnimation =>
            error.errorMessage.errorCode ==> ErrorCode.InsufficientInstructionSet
          case _ => assert(false)
        }
      }
    }

    "jump instruction execute"-{

      def testSuccess(jump: Int, jumpTo: Int): Unit = {
        val board = new Board()

        val program: Program = Compiler.compile(s"""
            bank main
              move
              jump ${jump}
              move
          """, PlayerColor.Blue).right.get

        val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program)
        board.addBot(bot)

        val instruction = bot.program.banks(0).instructions(1)

        bot.instructionIndex = 1

        instruction.execute(bot)

        bot.instructionIndex ==> jumpTo
      }

      "success"-{
        testSuccess(-1, -1)
        testSuccess(0, 0)
        testSuccess(1, 1)
      }

      def testSuccessJumpLast(jump: Int, jumpTo: Int): Unit = {
        val board = new Board()

        val program: Program = Compiler.compile(s"""
            bank main
              move
              move
              jump ${jump}
          """, PlayerColor.Blue).right.get

        val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program)
        board.addBot(bot)

        val instruction = bot.program.banks(0).instructions(2)

        bot.instructionIndex = 2
        bot.cycleNum = instruction.getRequiredCycles(bot)

        bot.cycle()

        bot.instructionIndex ==> jumpTo
      }

      "success with jump as last instruction"-{
        testSuccessJumpLast(-2, 0)
        testSuccessJumpLast(-1, 1)
        testSuccessJumpLast(0, 2)
      }

      def testAutoReboot(jump: Int, jumpToBank: Int, jumpToInstruction: Int): Unit = {
        val board = new Board()

        val program: Program = Compiler.compile(s"""
            bank main
              move
              move

            bank two
              move
              jump ${jump}
              move
              move
          """, PlayerColor.Blue).right.get

        val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program)
        board.addBot(bot)

        val instruction = bot.program.banks(1).instructions(1)

        bot.bankIndex = 1
        bot.instructionIndex = 1
        bot.cycleNum = instruction.getRequiredCycles(bot)

        bot.cycle()

        bot.bankIndex ==> jumpToBank
        bot.instructionIndex ==> jumpToInstruction
      }

      "jump out of bounds causes autoreboot"-{
        testAutoReboot(10, 0, 0)
        testAutoReboot(2, 1, 3)
        testAutoReboot(3, 0, 0)
        testAutoReboot(-1, 1, 0)
        testAutoReboot(-2, 0, 0)
      }

      def testJumpToLabel(jump: String, jumpTo: Int): Unit = {
        val board = new Board()

        val program: Program = Compiler.compile(s"""
            bank main
              @a
              move
              @b
              jump ${jump}
              @c
              move
              @d
              move
          """, PlayerColor.Blue).right.get

        val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program)
        board.addBot(bot)

        val instruction = bot.program.banks(0).instructions(1)

        bot.instructionIndex = 1
        bot.cycleNum = instruction.getRequiredCycles(bot)

        bot.cycle()

        bot.instructionIndex ==> jumpTo
      }

      "jump to label"-{
       testJumpToLabel("@a", 0)
       testJumpToLabel("@b", 1)
       testJumpToLabel("@c", 2)
       testJumpToLabel("@d", 3)

      }
    }

    "bjump instruction execute"-{

      def testSuccess(
          programStr: String,
          bankIndex: Int,
          instructionIndex: Int,
          newBankIndex: Int,
          newInstructionIndex: Int): Unit = {

        val board = new Board()

        val program: Program = Compiler.compile(programStr, PlayerColor.Blue).right.get

        val bot = Bot(board, PlayerColor.Blue, 0, 0, Direction.Right, program)
        board.addBot(bot)

        val instruction = bot.program.banks(bankIndex).instructions(instructionIndex)

        bot.bankIndex = bankIndex
        bot.instructionIndex = instructionIndex
        bot.cycleNum = instruction.getRequiredCycles(bot)

        bot.cycle()

        bot.bankIndex ==> newBankIndex
        bot.instructionIndex ==> newInstructionIndex

      }

      "bank jumping"-{
        testSuccess(
          """
          bank main
          bjump 1, 3
          move
          move
          """,
          0, 0,
          0, 2)
      }
    }
  }
}
