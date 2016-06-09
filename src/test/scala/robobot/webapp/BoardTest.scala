package robobot.webapp

import utest._

object BoardTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {

    "addBot"-{

      val board = new Board()

      "successfully add Bot at 0,0"-{
        val bot = Bot(board, 0, 0)
        board.addBot(bot)
      }

      "successfully add Bot at numRows-1,numCols-1"-{
        val bot = Bot(board, config.numRows - 1, config.numCols - 1)
        board.addBot(bot)
      }

      "unsuccessfully add Bot at -1,-1"-{
        val bot = Bot(board, -1, -1)
        intercept[IllegalArgumentException] {
          board.addBot(bot)
        }
      }

      "unsuccessfully add Bot at numRows, numCols"-{
        val bot = Bot(board, config.numRows, config.numCols)
        intercept[IllegalArgumentException] {
          board.addBot(bot)
        }
      }

      "unsuccessfully add Bot because bot is already in matrix"-{
        val board = new Board()
        val bot = Bot(board, 0, 0)
        board.addBot(bot)
        bot.row = 1
        intercept[IllegalArgumentException] {
          board.addBot(bot)
        }
      }
    }

    "moveBot"-{
      "successfully"-{
        val board = new Board()
        val bot = Bot(board, 1, 1)
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
        val bot1 = Bot(board, 1, 1)
        val bot2 = Bot(board, 0, 1)
        board.addBot(bot1)
        board.addBot(bot2)
        board.matrix(1)(1) ==> Some(bot1)
        board.matrix(0)(1) ==> Some(bot2)
        intercept[IllegalArgumentException] {
          board.moveBot(bot1, 0, 1)
        }
      }
    }
  }
}