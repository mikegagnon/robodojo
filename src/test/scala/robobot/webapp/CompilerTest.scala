package robobot.webapp

import utest._

import scala.collection.mutable.ArrayBuffer

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

      "split" - {

        "zero lines" - {
          val text = ""
          val result = Compiler.tokenize(text)
          val expectedResult = Array[TokenLine]()
          assert(result.sameElements(expectedResult))
        }

        "one line" - {
          val text = "1"
          val result = Compiler.tokenize(text)
          val expectedResult = Array(TokenLine(Array("1"), 0))
          assert(result.sameElements(expectedResult))
        }

        "three lines" - {
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

      "remove comments"-{
        "commented out text"-{
          val text = "a b c ; x y z"
          val expectedResult =
            Array(TokenLine(Array("a", "b", "c"), 0))
        }
        "trailing semicolon"-{
          val text = "a b c;"
          val expectedResult =
            Array(TokenLine(Array("a", "b", "c"), 0))
        }
        "line containing only semicolon"-{
          val text = " ; "
          val expectedResult =
            Array(TokenLine(Array(), 0))
        }
      }

      "Remove empty tokens"-{
          val text = """1
          2
          3
          """
          val result = Compiler.tokenize(text)
          val expectedResult =
            Array(
              TokenLine(Array("1"), 0),
              TokenLine(Array("2"), 1),
              TokenLine(Array("3"), 2))

          assert(result.sameElements(expectedResult))
      }

      "Drop empty lines"-{
        val text = """

        1

        2

        3


        """

        val result = Compiler.tokenize(text)
        val expectedResult =
          Array(
            TokenLine(Array("1"), 2),
            TokenLine(Array("2"), 4),
            TokenLine(Array("3"), 6))
        println(result.mkString("\n"))
        println(expectedResult.mkString("\n"))
        assert(result.sameElements(expectedResult))
      }
    }

    "compile"-{
      implicit val config = new Config

      def testInstruction(
          instruction: String,
          result: Either[ErrorCode.EnumVal, Instruction]) : Unit = {

        val text = "bank Main\n" + instruction
        val compiledResult = Compiler.compile(text)

        result match {
          case Left(errorCode) =>
            compiledResult match {
              case Right(_) => assert(false)
              case Left(errorMessages) => {
                errorMessages.length ==> 1
                errorMessages.head match {
                  case ErrorMessage(errorCode, 1, _) => assert(true)
                  case _ => assert(false)
                }
              }
            }
          case Right(compiledInstruction) => {
            val expectedProgram = Program(Map(0-> Bank(ArrayBuffer(compiledInstruction))))
            compiledResult match {
              case Left(_) => assert(false)
              case Right(program) => (program ==> expectedProgram)
            }
          }
        }
      }

      "move"-{
        "success"-{
          testInstruction("move", Right(MoveInstruction()))
        }
        "fail"-{
          testInstruction("move foo", Left(ErrorCode.TooManyParams))
        }
      }
      "turn"-{
        "success 1"-{
          testInstruction("turn 1", Right(TurnInstruction(1)))
        }
        "success 2"-{
          testInstruction("turn 2", Right(TurnInstruction(2)))
        }
        "success -1"-{
          testInstruction("turn -1", Right(TurnInstruction(-1)))
        }
        "fail turn left"-{
          testInstruction("turn left", Left(ErrorCode.WrongParamType))
        }
        "fail turn 1 foo"-{
          testInstruction("turn left", Left(ErrorCode.TooManyParams))
        }
      }
      "bank"-{
        val text = "bank Main\nmove"
        val expectedProgram = Program(Map(0-> Bank(ArrayBuffer(MoveInstruction()))))
        Compiler.compile(text) match {
          case Left(_) => assert(false)
          case Right(program) => (program ==> expectedProgram)
        }
      }
    }
  }
}