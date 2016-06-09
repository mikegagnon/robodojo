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
  }
}