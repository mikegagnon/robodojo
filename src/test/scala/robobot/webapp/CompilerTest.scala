package robobot.webapp

import utest._

object CompilerTest extends TestSuite {

  import Compiler._

  val tests = this {

    "TokenLine.equals"-{
      "simple case"-{
        "equals"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 0)
          a ==> b
        }

        "unequal lineNumbers"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 1)
          assert(a != b)
        }

        "unequal token array"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array("a"), 0)
          assert(a != b)
        }
      }

      "one token"-{
        "equals"-{
          val a = TokenLine(Array("a"), 1)
          val b = TokenLine(Array("a"), 1)
          a ==> b
        }

        "unequal token array"-{
          val a = TokenLine(Array("b"), 0)
          val b = TokenLine(Array("a"), 0)
          assert(a != b)
        }
      }

      "two tokens"-{
        "equals"-{
          val a = TokenLine(Array("a", "b"), 2)
          val b = TokenLine(Array("a", "b"), 2)
          a ==> b
        }
        "not equals"-{
          val a = TokenLine(Array("a", "b"), 2)
          val b = TokenLine(Array("a", "c"), 2)
          assert(a != b)
        }
      }

    }

    "tokenize"-{

      "split empty text into zero lines" - {
        val text = ""
        val result = Compiler.tokenize(text)
        val expectedResult = Array[TokenLine]()
        assert(result.sameElements(expectedResult))
      }

      "split text into one line" - {
        val text = "1"
        val result = Compiler.tokenize(text)
        val expectedResult = Array(TokenLine(Array("1"), 0))
        assert(result.sameElements(expectedResult))
      }

      "split three lines" - {
        val text =
"""1
2
3"""
        val result = Compiler.tokenize(text)
        val expectedResult =
          Array(
            TokenLine(Array("1"), 0),
            TokenLine(Array("2"), 1),
            TokenLine(Array("3"), 2))

        assert(result.sameElements(expectedResult))
      }
    }
  }
}