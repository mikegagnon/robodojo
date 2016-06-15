package robobot.webapp

import utest._

object CompilerTest extends TestSuite {

  import Compiler._

  implicit class EnrichedTokenLine(tl: TokenLine) {
    
    def prettyString(): String = {
      val tokens = tl.tokens.mkString(",")
      s"TokenLine([${tokens}], ${tl.lineNumber})"
    }

    // TODO: test
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