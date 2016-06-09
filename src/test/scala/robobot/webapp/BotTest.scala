package robobot.webapp

import utest._

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
      "one move instruction"-{
        val board = new Board()

        // A bot with only the move instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions :+= MoveInstruction()
        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        board.matrix(0)(0) ==> Some(bot1)

        Range(0, config.moveCycles -1).foreach { (_) => bot1.cycle() }

        board.matrix(0)(0) ==> Some(bot1)

        bot1.cycle()

        board.matrix(0)(0) ==> None
        board.matrix(0)(1) ==> Some(bot1)
      }

      "one turn instruction"-{
        val board = new Board()

        // A bot with only the turn instruction instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions :+= TurnInstruction(0)
        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        bot1.direction ==> Direction.Right

        Range(0, config.turnCycles -1).foreach { (_) => bot1.cycle() }

        bot1.direction ==> Direction.Right

        bot1.cycle()

        bot1.direction ==> Direction.Up


      }
    }
  }
}