package robobot.webapp

import utest._

object CompilerTest extends TestSuite {

  import Compiler._

  // TODO: abort Enriched class
  implicit class EnrichedTokenLine(tl: TokenLine) {
    
    def prettyString(): String = {
      val tokens = tl.tokens.mkString(",")
      s"TokenLine([${tokens}], ${tl.lineNumber})"
    }

    def deepEquals(that: Any) =
      that match {
        case thatTl: TokenLine => {
          tl.tokens.sameElements(thatTl.tokens) && tl.lineNumber == thatTl.lineNumber
        }
        case _ => false
      }
  }

  // TODO: test
  def tokenLinesEquals(tls1: Array[TokenLine], tls2: Array[TokenLine]) =
    tls1.length == tls2.length &&
    tls1.zip(tls2).forall{ case (a:  TokenLine, b: TokenLine) =>
      a.deepEquals(b)
    }


  val tests = this {

    "deepEquals"-{
      "simple case"-{
        "equals"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 0)
          assert(a.deepEquals(b))
        }

        "unequal lineNumbers"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 1)
          assert(!a.deepEquals(b))
        }

        "unequal token array"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array("a"), 0)
          assert(!a.deepEquals(b))
        }
      }

      "one token"-{
        "equals"-{
          val a = TokenLine(Array("a"), 1)
          val b = TokenLine(Array("a"), 1)
          assert(a.deepEquals(b))
        }

        "unequal token array"-{
          val a = TokenLine(Array("b"), 0)
          val b = TokenLine(Array("a"), 0)
          assert(!a.deepEquals(b))
        }
      }

      "two tokens"-{
        "equals"-{
          val a = TokenLine(Array("a", "b"), 2)
          val b = TokenLine(Array("a", "b"), 2)
          assert(a.deepEquals(b))
        }
        "not equals"-{
          val a = TokenLine(Array("a", "b"), 2)
          val b = TokenLine(Array("a", "c"), 2)
          assert(!a.deepEquals(b))
        }
      }

    }

    "tokenize"-{

      "split empty text into zero lines" - {
        val text = ""
        val result = Compiler.tokenize(text)
        val expectedResult = Array[TokenLine]()
        assert(tokenLinesEquals(result, expectedResult))
      }

      "split text into one line" - {
        val text = "1"
        val result = Compiler.tokenize(text)
        val expectedResult = Array(TokenLine(Array("1"), 0))
        assert(tokenLinesEquals(result, expectedResult))
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

        assert(tokenLinesEquals(result, expectedResult))
      }
    }
  }
}