package club.robodojo

import utest._

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
      // TODO: better name
      "test1"-{
        val board = new Board()
        val bot1 = Bot(board, PlayerColor.Blue, 1, 1)
        board.addBot(bot1)
        val bot2 = Bot(board, PlayerColor.Blue, 2, 1)
        board.addBot(bot2)

        board.removeBot(bot1)

        board.matrix(1,1) ==> None


      }
    }
  }
}