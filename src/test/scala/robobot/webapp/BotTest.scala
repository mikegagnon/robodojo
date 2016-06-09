package robobot.webapp

import utest._

import scala.collection.mutable.ArrayBuffer

object BotTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {

    "Bot.equals and Bot.hashCode"-{

      val board = new Board()

      "same bot"-{
        val a = new Bot(board)
        a ==> a
        a.hashCode ==> a.hashCode
      }

      "different bot"-{
        val a = new Bot(board)
        val b = new Bot(board)
        assert(a != b)
        assert(a.hashCode != b.hashCode)
      }
    }

    // TODO: factor out common code
    "cycle"-{

      def getBot(row: Int, col: Int, dir: Direction.EnumVal,
          instr: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]()) = {

        val board = new Board()
        val bot = Bot(board, row, col)
        bot.direction = dir
        board.addBot(bot)
        val bank0 = new Bank()
        bank0.instructions = instr
        bot.banks += (0 -> bank0)
        bot
      }

      "empty bank"-{
        val bot = getBot(0, 0, Direction.Up)

        bot.board.matrix(0)(0) ==> Some(bot)
        bot.direction ==> Direction.Up

        bot.cycle()
        bot.cycle()
        bot.cycle()
        
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.direction ==> Direction.Up
      }

      "one move instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(MoveInstruction()))

        bot.board.matrix(0)(0) ==> Some(bot)
        for ( _ <- 0 to config.moveCycles - 2) { bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(0)(1) ==> Some(bot)
      }

      "one turn instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(0)))

        bot.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Up
      }

      "turn instruction followed by move instruction"-{
        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(1), MoveInstruction()))

        // Turn instruction
        bot.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Down

        // Move instruction
        bot.board.matrix(0)(0) ==> Some(bot)
        Range(0, config.moveCycles -1).foreach { (_) => bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(1)(0) ==> Some(bot)
      }

      "turn instruction followed by move instruction, and then repeat"-{

        val bot = getBot(0, 0, Direction.Right, ArrayBuffer(TurnInstruction(1), MoveInstruction()))

        // Turn instruction
        bot.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot.cycle() }
        bot.direction ==> Direction.Right
        bot.cycle()
        bot.direction ==> Direction.Down

        // Move instruction
        bot.board.matrix(0)(0) ==> Some(bot)
        Range(0, config.moveCycles -1).foreach { (_) => bot.cycle() }
        bot.board.matrix(0)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(0)(0) ==> None
        bot.board.matrix(1)(0) ==> Some(bot)

        // Turn instruction
        bot.direction ==> Direction.Down
        Range(0, config.turnCycles -1).foreach { (_) => bot.cycle() }
        bot.direction ==> Direction.Down
        bot.cycle()
        bot.direction ==> Direction.Left

        // Move instruction
        bot.board.matrix(1)(0) ==> Some(bot)
        Range(0, config.moveCycles -1).foreach { (_) => bot.cycle() }
        bot.board.matrix(1)(0) ==> Some(bot)
        bot.cycle()
        bot.board.matrix(1)(0) ==> None
        bot.board.matrix(1)(config.numCols - 1) ==> Some(bot)

      }
    }
  }
}