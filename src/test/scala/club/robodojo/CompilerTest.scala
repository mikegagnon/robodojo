package club.robodojo

import utest._

import scala.collection.mutable.ArrayBuffer

object CompilerTest extends TestSuite {

  import Compiler._

  val tests = this {

    implicit val config = new Config

    "TokenLine.equals"-{
      "simple case"-{
        "equals"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 0)
          a ==> b
          a.hashCode ==> b.hashCode
        }

        "unequal lineNumbers"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array(), 1)
          assert(a != b)
          assert(a.hashCode != b.hashCode)
        }

        "unequal token array"-{
          val a = TokenLine(Array(), 0)
          val b = TokenLine(Array("a"), 0)
          assert(a != b)
          assert(a.hashCode != b.hashCode)
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

      "split"-{

        "zero lines"-{
          val text = ""
          val result = Compiler.tokenize(text)
          val expectedResult = Array[TokenLine]()
          assert(result.sameElements(expectedResult))
        }

        "one line"-{
          val text = "1"
          val result = Compiler.tokenize(text)
          val expectedResult = Array(TokenLine(Array("1"), 0))
          assert(result.sameElements(expectedResult))
        }

        "three lines"-{
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

      "slice"-{

        def testSlice(maxLineLength: Int, text: String, expectedSliced: String): Unit = {
          val config = new Config(Map("compiler.maxLineLength" -> maxLineLength))
          val result = Compiler.tokenize(text)(config)
          val expectedResult = Array(TokenLine(Array(expectedSliced), 0))
          assert(result.sameElements(expectedResult))
        }

        "Exactly maxLineLength characters"-{
          testSlice(5, "12345", "12345")
        }
        "One character too many"-{
          testSlice(4, "12345", "1234")
        }
      }

      "remove comments"-{
        "commented out text"-{
          val text = "a b c ; x y z"
          val expectedResult = Array(TokenLine(Array("a", "b", "c"), 0))
          val result = Compiler.tokenize(text)
          assert(result.sameElements(expectedResult))
        }
        "trailing semicolon"-{
          val text = "a b c;"
          val expectedResult = Array(TokenLine(Array("a", "b", "c"), 0))
          val result = Compiler.tokenize(text)
          assert(result.sameElements(expectedResult))
        }
        "line containing only semicolon"-{
          val text = " ; "
          val result = Compiler.tokenize(text)
          result.length ==> 0
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
        assert(result.sameElements(expectedResult))
      }

      "Filter out Name, Author, Country"-{
        val text = """
          1
          Name Foo Bar
          Name
          2
          Author Mufaso Max
          3
          Country The Moon
          Country
          4
          """

        val result = Compiler.tokenize(text)
        val expectedResult =
          Array(
            TokenLine(Array("1"), 1),
            TokenLine(Array("2"), 4),
            TokenLine(Array("3"), 6),
            TokenLine(Array("4"), 9))

          assert(result.sameElements(expectedResult))
      }
    }

    "compile"-{

      def testInstruction(
          instruction: String,
          result: Either[ErrorCode.EnumVal, Instruction]) : Unit = {

        val text = "bank Main\n" + instruction
        val compiledResult = Compiler.compile(text)

        result match {
          case Left(expectedErrorCode) =>
            compiledResult match {
              case Right(_) => assert(false)
              case Left(errorMessages) => {
                errorMessages.length ==> 1
                errorMessages.head match {
                  case ErrorMessage(errorCode, 1, _) => expectedErrorCode ==> errorCode
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

      def testBankFail(program: String, expectedErrorCode: ErrorCode.EnumVal): Unit = {
        Compiler.compile(program) match {
          case Left(errorMessages) => {
            errorMessages.length ==> 1
            errorMessages.head match {
              case ErrorMessage(errorCode, 0, _) => expectedErrorCode ==> errorCode
              case _ => assert(false)
            }
          }
          case _ => assert(false)
        }
      }

      def testProgram(program: String, expectedProgram: Program)(implicit config: Config): Unit =
        Compiler.compile(program) match {
          case Left(_) => assert(false)
          case Right(program) => (program ==> expectedProgram)
        }

      def testProgramFail(program: String, expectedErrorCode: ErrorCode.EnumVal)
          (implicit config: Config): Unit =
        Compiler.compile(program) match {
          case Left(errorMessages) => {
            errorMessages.length ==> 1
            errorMessages.head match {
              case ErrorMessage(errorCode, _, _) => expectedErrorCode ==> errorCode
              case _ => assert(false)
            }
          }
          case Right(_) => assert(false)
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
          testInstruction("turn 1 foo", Left(ErrorCode.TooManyParams))
        }
      }
      "bank"-{

        "fail: too many params"-{
          testBankFail("bank foo bar", ErrorCode.TooManyParams)
        }
        "fail: too few params"-{
          testBankFail("bank", ErrorCode.MissingParams)
        }
        "fail: undeclared bank"-{
          testBankFail("move", ErrorCode.UndeclaredBank)
        }
        "fail: too many banks"-{
          val config = new Config(Map("sim.maxBanks" -> 5))
          // One over the limit
          val text = "bank 1\nbank 2\nbank 3\nbank 4\nbank 5\nbank 6"
          testProgramFail(text, ErrorCode.MaxBanksExceeded)(config)
        }
        "success: num Banks == max Banks"-{
           val config = new Config(Map("sim.maxBanks" -> 5))

          // Exactly at the limit
          val text = "bank 1\nbank 2\nbank 3\nbank 4\nbank 5"
          val expectedProgram = Program(Map(0 -> Bank(),
                                            1 -> Bank(),
                                            2 -> Bank(),
                                            3 -> Bank(),
                                            4 -> Bank()))
          testProgram(text , expectedProgram)(config)
        }
        "success 1 instruction"-{
          val text = "bank Main\nmove"
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(MoveInstruction()))))
          testProgram(text, expectedProgram)
        }
        "success 2 instructions"-{
          val text = "bank Main\nmove\nmove"
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(MoveInstruction(),
                                                                 MoveInstruction()))))
          testProgram(text, expectedProgram)
        }
        "success 2 banks"-{
          val text = "bank Main\nmove\nbank foo"
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(MoveInstruction())),
                                            1 -> Bank(ArrayBuffer())))
          testProgram(text, expectedProgram)
        }
        "success 3 banks"-{
          val text = "bank Main\nmove\nbank foo \nbank foo"
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(MoveInstruction())),
                                            1 -> Bank(ArrayBuffer()),
                                            2 -> Bank(ArrayBuffer())))
          testProgram(text, expectedProgram)
        }
        "success 3 non-empty banks"-{
          val text = "bank Main\nmove\nbank foo\nmove\nmove\nbank foo\nmove"
          val move = MoveInstruction()
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(move)),
                                            1 -> Bank(ArrayBuffer(move, move)),
                                            2 -> Bank(ArrayBuffer(move))))
          testProgram(text, expectedProgram)
        }
      }
    }
  }
}