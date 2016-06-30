package club.robodojo

import utest._

import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.IndexedSeq

object BotTest extends TestSuite {

  implicit val config = Config.default

  val defaultSourceMap = SourceMapInstruction(0, 0)

  val tests = this {

    // TODO: factor out common code
    "cycle"-{

      def getBot(row: Int, col: Int, dir: Direction.EnumVal,
          instr: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]()) = {

        val board = new Board()
        val bot = Bot(board, PlayerColor.Blue, row, col)
        bot.direction = dir
        board.addBot(bot)
        val bank0 = new Bank(instr.toIndexedSeq)
        bot.program.banks += (0 -> bank0)
        bot
      }

      "empty bank leads to tapout"-{
        val bot = getBot(0, 0, Direction.Up)

        bot.board.matrix(0)(0) ==> Some(bot)
        bot.direction ==> Direction.Up

        bot.cycle()
        
        bot.board.matrix(0)(0) ==> None
      }

      "one move instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(MoveInstruction(defaultSourceMap)))

        bot.board.matrix(0)(0) ==> Some(bot)
        for ( _ <- 0 to config.sim.cycleCount.durMove - 1) { bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(0)(1) ==> Some(bot)
      }

      "one turn instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(defaultSourceMap,
          IntegerParam(0))))

        bot.direction ==> Direction.Right
        for ( _ <- 0 to config.sim.cycleCount.durTurn - 1) { bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Up
      }

      "turn instruction followed by move instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(defaultSourceMap,
          IntegerParam(1)), MoveInstruction(defaultSourceMap)))

        // Turn instruction
        bot.direction ==> Direction.Right
        for ( _ <- 0 to config.sim.cycleCount.durTurn - 1) { bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Down

        // Move instruction
        bot.board.matrix(0)(0) ==> Some(bot)
        for ( _ <- 0 to config.sim.cycleCount.durMove - 1) { bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(1)(0) ==> Some(bot)
      }

      "turn instruction followed by move instruction, and then repeat"-{

        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(defaultSourceMap,
          IntegerParam(1)), MoveInstruction(defaultSourceMap)))

        // Turn instruction
        bot.direction ==> Direction.Right
        for ( _ <- 0 to config.sim.cycleCount.durTurn - 1) { bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Down

        // Move instruction
        bot.board.matrix(0)(0) ==> Some(bot)
        for ( _ <- 0 to config.sim.cycleCount.durMove - 1) { bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(1)(0) ==> Some(bot)

        // Turn instruction
        bot.direction ==> Direction.Down
        for ( _ <- 0 to config.sim.cycleCount.durTurn - 1) { bot.cycle() }
        bot.direction ==> Direction.Down
        bot.cycle()
        bot.direction ==> Direction.Left

        // Move instruction
        bot.board.matrix(1)(0) ==> Some(bot)
        for ( _ <- 0 to config.sim.cycleCount.durMove - 1) { bot.cycle() }
        bot.board.matrix(1)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(1)(0) ==> None
        bot.board.matrix(1)(config.sim.numCols - 1) ==> Some(bot)
      }
    }

    "registers"-{
      val board = new Board()
      val bot = Bot(board, PlayerColor.Blue, 0, 0)
      bot.registers.length ==> config.sim.maxNumVariables
      bot.registers.foreach {
          _ ==> 0.toShort
      }
    }

    "getRemote"-{
      "looking at another bot"-{
        val board = new Board()
        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Up)
        val bot2 = Bot(board, PlayerColor.Blue, config.sim.numRows - 1, 0)
        board.addBot(bot1)
        board.addBot(bot2)
        bot1.getRemote() ==> Some(bot2)
      }
      "not looking at another bot"-{
        val board = new Board()
        val bot1 = Bot(board, PlayerColor.Blue, 0, 0, Direction.Down)
        board.addBot(bot1)
        bot1.getRemote() ==> None
      }
    }
  }
}