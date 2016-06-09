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
    }

    "moveBot"-{
      val board = new Board()
      val bot = new Bot(board)
    }
  }
}