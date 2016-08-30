package club.robodojo

import utest._

import scala.collection.immutable.IndexedSeq

object BoardTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {

    "addBot"-{

      val board = new Board()

      "successfully add Bot at 0,0"-{
        val bot = Bot(board, PlayerColor.Blue, 0, 0)
        board.addBot(bot)
      }

      "successfully add Bot at numRows-1,numCols-1"-{
        val bot = Bot(board, PlayerColor.Blue, config.sim.numRows - 1, config.sim.numCols - 1)
        board.addBot(bot)
      }

      "unsuccessfully add Bot because bot is already in matrix"-{
        val board = new Board()
        val bot1 = Bot(board, PlayerColor.Blue, 0, 0)
        board.addBot(bot1)
        val bot2 = Bot(board, PlayerColor.Blue, 0, 0)
        intercept[IllegalArgumentException] {
          board.addBot(bot2)
        }
      }
    }

    "moveBot"-{
      "successfully"-{
        val board = new Board()
        val bot = Bot(board, PlayerColor.Blue, 1, 1)
        board.addBot(bot)
        board.matrix(1)(1) ==> Some(bot)
        board.moveBot(bot, 0, 1)
        bot.row ==> 0
        bot.col ==> 1
        board.matrix(1)(1) ==> None
        board.matrix(0)(1) ==> Some(bot)
      }
      "unsuccessfully"-{
        val board = new Board()
        val bot1 = Bot(board, PlayerColor.Blue, 1, 1)
        val bot2 = Bot(board, PlayerColor.Blue, 0, 1)
        board.addBot(bot1)
        board.addBot(bot2)
        board.matrix(1)(1) ==> Some(bot1)
        board.matrix(0)(1) ==> Some(bot2)
        intercept[IllegalArgumentException] {
          board.moveBot(bot1, 0, 1)
        }
      }
    }

    "removeBot"-{
      val board = new Board()
      val bot1 = Bot(board, PlayerColor.Blue, 1, 1)
      board.addBot(bot1)
      val bot2 = Bot(board, PlayerColor.Blue, 2, 1)
      board.addBot(bot2)

      board.removeBot(bot1)

      board.matrix(1)(1) ==> None
      board.bots.length ==> 1
      board.bots(0).id ==> bot2.id
    }

    "board.deepCopy"-{
      val board = new Board()

      val color = PlayerColor.Blue
      val bank0 = Bank(IndexedSeq(MoveInstruction(SourceMapInstruction(0, 0), 0, PlayerColor.Blue )), Some(SourceMap(color, 0, IndexedSeq("move"))))
      val bank1 = Bank(IndexedSeq(TurnInstruction(SourceMapInstruction(0, 1), IntegerParam(0))), Some(SourceMap(color, 1, IndexedSeq("turn 0"))))
      val program = Program(Map(0 ->  bank0, 1 -> bank1))

      val bot = Bot(board, color, 1, 1, Direction.Right, program, InstructionSet.Advanced, true, 5)
      bot.registers(1) = 5
      board.addBot(bot)

      val newBoard = board.deepCopy()
      newBoard.cycleNum ==> board.cycleNum
      val newBot = newBoard.matrix(1)(1).get

      // TODO: factor out common code
      assert(newBot != bot)
      newBot.board ==> newBoard
      newBot.id ==> bot.id
      newBot.row ==> bot.row
      newBot.col ==> bot.col
      newBot.direction ==> bot.direction

      // Make sure program are distinct objects
      newBot.program.banks -= 0
      assert(newBot.program != bot.program)

      newBot.program.banks += 0 -> bank0
      newBot.program ==> bot.program

      newBot.instructionSet ==> bot.instructionSet
      newBot.mobile ==> bot.mobile
      newBot.active ==> bot.active
      newBot.bankIndex ==> bot.bankIndex
      newBot.instructionIndex ==> bot.instructionIndex
      newBot.cycleNum ==> bot.cycleNum
      newBot.requiredCycles ==> bot.requiredCycles

      // Make sure registers are distinct objects
      newBot.registers(1) = 7
      assert(newBot.registers != bot.registers)

      newBot.registers(1) = 5
      newBot.registers ==> bot.registers

      newBoard.bots.length ==> 1

      assert(newBoard.bots(0) != bot)
      newBoard.bots(0) ==> newBot

    }
  }
}