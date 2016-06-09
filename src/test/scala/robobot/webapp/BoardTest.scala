package robobot.webapp

import utest._

object BoardTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {

    "addBot"-{

      val board = new Board()
      val bot = new Bot(board)

      "successfully add Bot at 0,0"-{
        bot.row = 0
        bot.col = 0
        board.addBot(bot)
      }

      "successfully add Bot at numRows-1,numCols-1"-{
        bot.row = config.numRows - 1
        bot.col = config.numCols - 1
        board.addBot(bot)
      }

      "unsuccessfully add Bot at -1,-1"-{
        bot.row = -1
        bot.col = -1
        intercept[IllegalArgumentException] {
          board.addBot(bot)
        }
      }

      "unsuccessfully add Bot at numRows, numCols"-{
        bot.row = config.numRows
        bot.col = config.numCols
        intercept[IllegalArgumentException] {
          board.addBot(bot)
        }
      }
    }

    "moveBot"-{
      val board = new Board()
      val bot = new Bot(board)
    }
  }
}