package club.robodojo

import utest._

import scala.collection.mutable.{ArrayBuffer, WrappedArray}

// TODO: change to lineIndex
object CompilerTest extends TestSuite {

  import Compiler._

  val defaultSourceMap = SourceMapInstruction(1, 0)

  val tests = this {

    implicit val config = new Config

    "getParam"-{
      "success"-{
        "writeable"-{
          {
            val instructionName = "foo"
            val parameterIndex = 1
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "%active"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(ActiveKeyword(false))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(WriteableParamType, ReadableParamType, ReadableParamType)
            val token = "#active"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(ActiveKeyword(true))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 1
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType)
            val token = "#1"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(RegisterParam(0))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 1
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "#" + config.sim.maxNumVariables

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(RegisterParam(config.sim.maxNumVariables - 1))

            result ==> expectedResult
          }
        }
        "readable"-{
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType)
            val token = "#1"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(RegisterParam(0))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 1
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "#" + config.sim.maxNumVariables

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(RegisterParam(config.sim.maxNumVariables - 1))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 2
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "#active"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(ActiveKeyword(true))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "%active"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(ActiveKeyword(false))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "$banks"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(BanksKeyword(true))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 2
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "%banks"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(BanksKeyword(false))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 2
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "$instrset"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(InstrSetKeyword(true))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "%instrset"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(InstrSetKeyword(false))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "$mobile"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(MobileKeyword(true))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 2
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "%mobile"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(MobileKeyword(false))

            result ==> expectedResult
          }
          {
            val instructionName = "foo"
            val parameterIndex = 0
            val lineNumber = 5
            val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
            val token = "$fields"

            val result = getParam(instructionName, parameterIndex, lineNumber, types, token)
            val expectedResult = Right(FieldsKeyword())

            result ==> expectedResult
          }
        }
      }
      "fail"-{
        {
          val instructionName = "foo"
          val parameterIndex = 1
          val lineNumber = 5
          val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
          val token = "active"

          val result = getParam(instructionName, parameterIndex, lineNumber, types, token)

          result match {
            case Left(error) => error.errorCode ==> ErrorCode.WrongParamType
            case Right(param) => assert(false)
          }
        }
        {
          val instructionName = "foo"
          val parameterIndex = 1
          val lineNumber = 5
          val types = Seq(ReadableParamType, WriteableParamType, ReadableParamType)
          val token = "$banks"

          val result = getParam(instructionName, parameterIndex, lineNumber, types, token)

          result match {
            case Left(error) => error.errorCode ==> ErrorCode.WrongParamType
            case Right(param) => assert(false)
          }
        }
      }
    }

    "getCommaIndices"-{
      getCommaIndices(0) ==> Seq[Int]()
      getCommaIndices(1) ==> Seq[Int]()
      getCommaIndices(2) ==> Seq(2)
      getCommaIndices(3) ==> Seq(2, 4)
    }

    "foundCommas"-{
      foundCommas(0, TokenLine(Array("move"), "move", 1)) ==> true
      foundCommas(1, TokenLine(Array("turn", "0"), "turn 0", 1)) ==> true
      foundCommas(2, TokenLine(Array("set", "#1", ",", "#2"), "set #1, #2", 1)) ==> true
      foundCommas(2, TokenLine(Array("set", "#1", "x", "#2"), "set #1 x #2", 1)) ==> false
      foundCommas(3, TokenLine(Array("create", "1", ",", "2", ",", "3"), "create 1,2,3", 1)) ==> true
      foundCommas(3, TokenLine(Array("create", "1", "x", "2", ",", "3"), "create 1 x 2,3", 1)) ==> false
      foundCommas(3, TokenLine(Array("create", "1", ",", "2", "x", "3"), "create 1,2 x3", 1)) ==> false
      foundCommas(3, TokenLine(Array("create", "1", "x", "2", "x", "3"), "create 1 x 2 x 3", 1)) ==> false
    }

    "parseParams"-{
      "success"-{
        {
          val result = parseParams("turn", TokenLine(Array("turn", "#1"), "turn #1", 0),
            ReadableParamType)
          result ==> Right(Seq(RegisterParam(0)))
        }
        {
          val result = parseParams("set", TokenLine(Array("set", "#active", ",", "#2"),
            "set #active, #2", 0),
            WriteableParamType, ReadableParamType)
          result ==> Right(Seq(ActiveKeyword(true), RegisterParam(1)))
        }
        {
          val result = parseParams("create", TokenLine(Array("create", "1", ",", "#5", ",",
            "$banks"), "create 1, #5, $banks", 0), ReadableParamType, ReadableParamType,
            ReadableParamType)
          result ==> Right(Seq(IntegerParam(1), RegisterParam(4), BanksKeyword(true)))
        }
      }
      "fail"-{
        {
          val result = parseParams("turn", TokenLine(Array("foo", "$fields"), "foo $fields", 0),
            WriteableParamType)
          result match {
            case Left(error: ErrorMessage) => error.errorCode ==> ErrorCode.WrongParamType
            case Right(_) => assert(false)
          }
        }
        {
          val result = parseParams("turn", TokenLine(Array("foo"), "foo", 0), WriteableParamType)
          result match {
            case Left(error: ErrorMessage) => error.errorCode ==> ErrorCode.MalformedInstruction
            case Right(_) => assert(false)
          }
        }
        {
          val result = parseParams("set", TokenLine(Array("foo", "#1", "#2", ","), "foo #1, #2", 0),
            WriteableParamType, WriteableParamType)
          result match {
            case Left(error: ErrorMessage) => error.errorCode ==> ErrorCode.MalformedInstruction
            case Right(_) => assert(false)
          }
        }
      }
    }

    "TokenLine.equals"-{
      "simple case"-{
        "equals"-{
          val a = TokenLine(Array(), "", 0)
          val b = TokenLine(Array(), "", 0)
          a ==> b
          a.hashCode ==> b.hashCode
        }

        "unequal lineNumbers"-{
          val a = TokenLine(Array(), "", 0)
          val b = TokenLine(Array(), "", 1)
          assert(a != b)
          assert(a.hashCode != b.hashCode)
        }

        "unequal token array"-{
          val a = TokenLine(Array(), "", 0)
          val b = TokenLine(Array("a"), "a", 0)
          assert(a != b)
          assert(a.hashCode != b.hashCode)
        }
      }

      "one token"-{
        "equals"-{
          val a = TokenLine(Array("a"), "a", 1)
          val b = TokenLine(Array("a"), "a", 1)
          a ==> b
        }

        "unequal token array"-{
          val a = TokenLine(Array("b"), "b", 0)
          val b = TokenLine(Array("a"), "a", 0)
          assert(a != b)
        }
      }

      "two tokens"-{
        "equals"-{
          val a = TokenLine(Array("a", "b"), "a b", 2)
          val b = TokenLine(Array("a", "b"), "a b", 2)
          a ==> b
        }
        "not equals"-{
          val a = TokenLine(Array("a", "b"), "a b", 2)
          val b = TokenLine(Array("a", "c"), "a c", 2)
          assert(a != b)
        }
      }

    }

    "tokenize"-{

      "split"-{

        "zero lines"-{
          val text = ""
          val result = Compiler.tokenize(text)
          val expectedResult = Array(TokenLine(Array[String](), "", 0))
          assert(result.sameElements(expectedResult))
        }

        "one line"-{
          val text = "1"
          val result = Compiler.tokenize(text)
          val expectedResult = Array(TokenLine(Array("1"), text, 0))
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
              TokenLine(Array("1"), "1", 0),
              TokenLine(Array("2"), "2", 1),
              TokenLine(Array("3"), "3", 2))

          assert(result.sameElements(expectedResult))
        }
      }

      "slice"-{

        def testSlice(maxLineLength: Int, text: String, expectedSliced: String): Unit = {
          val config = new Config(Map("compiler.maxLineLength" -> maxLineLength))
          val result = Compiler.tokenize(text)(config)
          val expectedResult = Array(TokenLine(Array(expectedSliced), expectedSliced, 0))
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
          val expectedResult = Array(TokenLine(Array("a", "b", "c"), text, 0))
          val result = Compiler.tokenize(text)
          assert(result.sameElements(expectedResult))
        }
        "trailing semicolon"-{
          val text = "a b c;"
          val expectedResult = Array(TokenLine(Array("a", "b", "c"), text, 0))
          val result = Compiler.tokenize(text)
          assert(result.sameElements(expectedResult))
        }
        "line containing only semicolon"-{
          val text = " ; "
          val result = Compiler.tokenize(text)
          val expectedResult = Array(TokenLine(Array[String](), text, 0))
          assert(result.sameElements(expectedResult))
        }
      }

      "Replace ',' with ' , '"-{
        val text = "1,2,3"
        val expectedResult = Array(TokenLine(Array("1", ",", "2", ",", "3"), "1,2,3", 0))
        val result = Compiler.tokenize(text)
        assert(result.sameElements(expectedResult))
      }

      "To lower case"-{
        val text = "ABC xYz"
        val expectedResult = Array(TokenLine(Array("abc", "xyz"), "ABC xYz", 0))
        val result = Compiler.tokenize(text)
        assert(result.sameElements(expectedResult))
      }

      "Remove empty tokens"-{
          val text = """1
          2
          3
          """
          val result = Compiler.tokenize(text)
          val expectedResult =
            Array(
              TokenLine(Array("1"), "1", 0),
              TokenLine(Array("2"), "          2", 1),
              TokenLine(Array("3"), "          3", 2),
              TokenLine(Array(), "          ", 3))

          assert(result.sameElements(expectedResult))
      }
    }

    "getErrorWrongParamType"-{
      val result = getErrorWrongParamType(
        "create",
        "foo",
        1,
        3,
        Array(ReadableParamType, ReadableParamType, ReadableParamType))
      
      val expectedMessage = "<b>Wrong parameter type</b>: the <tt>create</tt> instruction must " + 
        "be of the form: <tt>create a, b, c</tt>, where <tt>a</tt> is a <i>readable " + 
        "parameter</i>, and <tt>b</tt> is a <i>readable parameter</i>, and <tt>c</tt> is a " +
        "<i>readable parameter</i>. Your second parameter, <tt>foo</tt>, is not a readable " +
        "parameter."

      result ==> ErrorMessage(ErrorCode.WrongParamType, 3, expectedMessage)
    }

    // TODO: test
    "parseParams"-{
      "success"-{
        val result = parseParams(
          "create",
          TokenLine(Array("create", "1", ",", "2", ",", "3"), "create 1,2,3", 5),
          ReadableParamType, ReadableParamType, ReadableParamType)
        val expectedResult = Right(Seq(IntegerParam(1), IntegerParam(2), IntegerParam(3)))
        result ==> expectedResult

      }
    }

    "compile"-{

      def testInstruction(
          instruction: String,
          result: Either[ErrorCode.EnumVal, Instruction]) : Unit = {

        val text = "bank Main\n" + instruction
        val compiledResult = Compiler.compile(text, PlayerColor.Blue)

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
            val expectedProgram = Program(Map(0->
              Bank(ArrayBuffer(compiledInstruction),
              Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", instruction))))))
            compiledResult match {
              case Left(_) => assert(false)
              case Right(program) => (program ==> expectedProgram)
            }
          }
        }
      }

      def testBankFail(program: String, expectedErrorCode: ErrorCode.EnumVal): Unit = {
        Compiler.compile(program, PlayerColor.Blue) match {
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
        Compiler.compile(program, PlayerColor.Blue) match {
          case Left(_) => assert(false)
          case Right(program) => (program ==> expectedProgram)
        }

      def testProgramFail(program: String, expectedErrorCode: ErrorCode.EnumVal)
          (implicit config: Config): Unit =
        Compiler.compile(program, PlayerColor.Blue) match {
          case Left(errorMessages) => {
            errorMessages.length ==> 1
            errorMessages.head match {
              case ErrorMessage(errorCode, _, _) => expectedErrorCode ==> errorCode
              case _ => assert(false)
            }
          }
          case Right(_) => assert(false)
        }

      // TODO: move downs
      "move"-{
        "success"-{
          testInstruction("move", Right(MoveInstruction(defaultSourceMap)))
        }
        "fail"-{
          testInstruction("move foo", Left(ErrorCode.TooManyParams))
        }
      }

      // TODO: move down
      "turn"-{
        "success 1"-{
          testInstruction("turn 1", Right(TurnInstruction(defaultSourceMap, IntegerParam(1))))
        }
        "success 2"-{
          testInstruction("turn 2", Right(TurnInstruction(defaultSourceMap, IntegerParam(2))))
        }
        "success -1"-{
          testInstruction("turn -1", Right(TurnInstruction(defaultSourceMap, IntegerParam(-1))))
        }
        "success #2"-{
          testInstruction("turn #2", Right(TurnInstruction(defaultSourceMap, RegisterParam(1))))
        }
        "success %banks"-{
          testInstruction("turn %banks",
            Right(TurnInstruction(defaultSourceMap, BanksKeyword(false))))
        }
        "fail turn left"-{
          testInstruction("turn left", Left(ErrorCode.WrongParamType))
        }
        "fail turn 1 foo"-{
          testInstruction("turn 1 foo", Left(ErrorCode.MalformedInstruction))
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
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer[Instruction](), Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank 1")))),
                                            1 -> Bank(ArrayBuffer[Instruction](), Some(SourceMap(PlayerColor.Blue, 1, ArrayBuffer("bank 2")))),
                                            2 -> Bank(ArrayBuffer[Instruction](), Some(SourceMap(PlayerColor.Blue, 2, ArrayBuffer("bank 3")))),
                                            3 -> Bank(ArrayBuffer[Instruction](), Some(SourceMap(PlayerColor.Blue, 3, ArrayBuffer("bank 4")))),
                                            4 -> Bank(ArrayBuffer[Instruction](), Some(SourceMap(PlayerColor.Blue, 4, ArrayBuffer("bank 5"))))))
          testProgram(text , expectedProgram)(config)
        }
        "success 1 instruction"-{
          val text = "bank Main\nmove"
          val expectedProgram = Program(
            Map(0 -> Bank(ArrayBuffer(MoveInstruction(defaultSourceMap)), Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", "move"))))))
          testProgram(text, expectedProgram)
        }
        "success 2 instructions"-{
          val text = "bank Main\nmove\nmove"
          val expectedProgram = Program(
              Map(0 -> Bank(ArrayBuffer(MoveInstruction(defaultSourceMap),
                                        MoveInstruction(SourceMapInstruction(2, 0))),
                            Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", "move", "move"))))))
          testProgram(text, expectedProgram)
        }
        "success 2 banks"-{
          val text = "bank Main\nmove\nbank foo"
          val expectedProgram = Program(
            Map(0 -> Bank(ArrayBuffer(MoveInstruction(defaultSourceMap)), Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", "move")))),
                1 -> Bank(ArrayBuffer(), Some(SourceMap(PlayerColor.Blue, 1, ArrayBuffer("bank foo"))))))
          testProgram(text, expectedProgram)
        }
        "success 3 banks"-{
          val text = "bank Main\nmove\nbank foo \nbank foo"
          val expectedProgram = Program(
            Map(0 -> Bank(ArrayBuffer(MoveInstruction(defaultSourceMap)), Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", "move")))),
                1 -> Bank(ArrayBuffer(), Some(SourceMap(PlayerColor.Blue, 1, ArrayBuffer("bank foo ")))),
                2 -> Bank(ArrayBuffer(), Some(SourceMap(PlayerColor.Blue, 2, ArrayBuffer("bank foo"))))))
          testProgram(text, expectedProgram)
        }
        "success 3 non-empty banks"-{
          val text = "bank Main\nmove\nbank foo\nmove\nmove\nbank foo\nmove"
          val expectedProgram = Program(Map(0 -> Bank(ArrayBuffer(MoveInstruction(defaultSourceMap)), Some(SourceMap(PlayerColor.Blue, 0, ArrayBuffer("bank Main", "move")))),
                                            1 -> Bank(ArrayBuffer(MoveInstruction(SourceMapInstruction(1, 1)), MoveInstruction(SourceMapInstruction(2, 1))), Some(SourceMap(PlayerColor.Blue, 1, ArrayBuffer("bank foo", "move", "move")))),
                                            2 -> Bank(ArrayBuffer(MoveInstruction(SourceMapInstruction(1, 2))), Some(SourceMap(PlayerColor.Blue, 2, ArrayBuffer("bank foo", "move"))) )))
          testProgram(text, expectedProgram)
        }
      }

      // TODO: move down
      // TODO: test for params
      "create"-{
        "fail"-{
          "too many tokens"-{
            val text = "create 1, 1, 1 X"
            testProgramFail(text, ErrorCode.MalformedInstruction)(config)
          }
          "missing comma 1"-{
            val text = "create 1 x 1 , 1"
            testProgramFail(text, ErrorCode.MalformedInstruction)(config)
          }
          "missing comma 2"-{
            val text = "create 1 , 1 x 1"
            testProgramFail(text, ErrorCode.MalformedInstruction)(config)
          }
          "non-integer params 1"-{
            val text = "create a, 1, 1"
            testProgramFail(text, ErrorCode.WrongParamType)(config)
          }
          "non-integer params 2"-{
            val text = "create 1, a, 1"
            testProgramFail(text, ErrorCode.WrongParamType)(config)
          }
          "non-integer params 3"-{
            val text = "create 1, 1, a"
            testProgramFail(text, ErrorCode.WrongParamType)(config)
          }
          "instruction set invalid 1"-{
            val text = "create -1, 1, 1"
            testProgramFail(text, ErrorCode.BadInstructionSetParam)(config)
          }
          "instruction set invalid 1"-{
            val text = "create -1, 1, 1"
            testProgramFail(text, ErrorCode.BadInstructionSetParam)(config)
          }
          "instruction set invalid 2"-{
            val text = "create 2, 1, 1"
            testProgramFail(text, ErrorCode.BadInstructionSetParam)(config)
          }
          "numBanks invalid 1"-{
            val text = "create 1, 0, 1"
            testProgramFail(text, ErrorCode.BadNumBanksParam)(config)
          }
          "numBanks invalid 2"-{
            val text = s"create 1, ${config.sim.maxBanks + 1}, 1"
            testProgramFail(text, ErrorCode.BadNumBanksParam)(config)
          }
          "mobile invalid 1"-{
            val text = "create 1, 1, -1"
            testProgramFail(text, ErrorCode.BadMobileParam)(config)
          }
          "mobile invalid 2"-{
            val text = "create 1, 1, 2"
            testProgramFail(text, ErrorCode.BadMobileParam)(config)
          }
        }
        "succeed"-{
          "instructionSet == 0"-{
            testInstruction("create 0, 1, 1",
              Right(CreateInstruction(defaultSourceMap, IntegerParam(0), IntegerParam(1), IntegerParam(1), 1, PlayerColor.Blue)))
          }
          "instructionSet == 1"-{
            testInstruction("create 1, 1, 1",
              Right(CreateInstruction(defaultSourceMap, IntegerParam(1), IntegerParam(1), IntegerParam(1), 1, PlayerColor.Blue)))
          }
          "numBanks == max"-{
            testInstruction(s"create 1, ${config.sim.maxBanks} , 1",
              Right(CreateInstruction(
                defaultSourceMap,
                IntegerParam(1),
                IntegerParam(config.sim.maxBanks.toShort),
                IntegerParam(1),
                1,
                PlayerColor.Blue)))
          }
          "mobile = false"-{
            testInstruction("create 1, 1, 0",
              Right(CreateInstruction(defaultSourceMap, IntegerParam(1), IntegerParam(1), IntegerParam(0), 1, PlayerColor.Blue)))
          }
          "register params"-{
            testInstruction("create #1, #2, #3",
              Right(CreateInstruction(defaultSourceMap, RegisterParam(0), RegisterParam(1), RegisterParam(2), 1, PlayerColor.Blue)))
          }
        }
      }

      "compileSet"-{
        "fail"-{
          testProgramFail(s"set #${config.sim.maxNumVariables + 1}, %mobile", ErrorCode.WrongParamType)
          testProgramFail("set #-1, %mobile", ErrorCode.WrongParamType)
          testProgramFail("set #0, foo", ErrorCode.WrongParamType)
          testProgramFail("set #0 #1", ErrorCode.MalformedInstruction)
          testProgramFail("set $banks, #1", ErrorCode.WrongParamType)
        }
        "succeed"-{
          testInstruction("set #2, 5",
            Right(SetInstruction(defaultSourceMap, RegisterParam(1), IntegerParam(5))))
          testInstruction(s"set #${config.sim.maxNumVariables}, %mobile",
            Right(SetInstruction(defaultSourceMap, RegisterParam(config.sim.maxNumVariables - 1), MobileKeyword(false))))
          testInstruction("set #Active, %banks",
            Right(SetInstruction(defaultSourceMap, ActiveKeyword(true), BanksKeyword(false))))
          testInstruction("set %ACTIVE, $fields",
            Right(SetInstruction(defaultSourceMap, ActiveKeyword(false), FieldsKeyword())))
        }
      }

      "isRegister"-{
        implicit val config = new Config(Map("sim.maxNumVariables" -> 10))

        assert(Compiler.isRegister("#1"))
        assert(Compiler.isRegister("#2"))
        assert(Compiler.isRegister("#3"))
        assert(Compiler.isRegister("#4"))
        assert(Compiler.isRegister("#5"))
        assert(Compiler.isRegister("#6"))
        assert(Compiler.isRegister("#7"))
        assert(Compiler.isRegister("#8"))
        assert(Compiler.isRegister("#9"))
        assert(Compiler.isRegister("#10"))

        assert(!Compiler.isRegister("#-1"))
        assert(!Compiler.isRegister("#0"))
        assert(!Compiler.isRegister("#11"))
        assert(!Compiler.isRegister("5"))
        assert(!Compiler.isRegister("#foo"))
      }

      "getWriteable"-{
        getWriteable("#active") ==> ActiveKeyword(true)
        getWriteable("%active") ==> ActiveKeyword(false)
        getWriteable("#1") ==> RegisterParam(0)
        getWriteable("#" + config.sim.maxNumVariables) ==>
          RegisterParam(config.sim.maxNumVariables - 1)

        intercept[IllegalArgumentException] {
          getWriteable("#0")
        }

        intercept[IllegalArgumentException] {
          getWriteable("#" + config.sim.maxNumVariables + 1)
        }

        intercept[IllegalArgumentException] {
          getWriteable("%banks")
        }

        intercept[IllegalArgumentException] {
          getWriteable("$fields")
        }
      }

      "getReadable"-{
        getReadable("#active") ==> ActiveKeyword(true)
        getReadable("%active") ==> ActiveKeyword(false)
        getReadable("$banks") ==> BanksKeyword(true)
        getReadable("%banks") ==> BanksKeyword(false)
        getReadable("$instrset") ==> InstrSetKeyword(true)
        getReadable("%instrset") ==> InstrSetKeyword(false)
        getReadable("$mobile") ==> MobileKeyword(true)
        getReadable("%mobile") ==> MobileKeyword(false)
        getReadable("$fields") ==> FieldsKeyword()
        getReadable("#1") ==> RegisterParam(0)
        getReadable("5") ==> IntegerParam(5)
        getReadable("-11") ==> IntegerParam(-11)

        intercept[IllegalArgumentException] {
          getWriteable("foo")
        }
      }

      // TODO: TODO: implement
      "parseParams"-{
        "success"-{

          // TODO
          /*
          import scala.language.postfixOps
          val instructionName = "foo"

          "foo 1, 2, 3"-{
            val tl = TokenLine(Array("foo", "1", ",", "2", ",", "3"), 5)
            val result = parseParams("foo", tl, ReadableParamType, ReadableParamType,
              ReadableParamType)
            val expectedResult = Right(Seq(IntegerParam(1), IntegerParam(2), IntegerParam(3)))
            result ==> expectedResult
          }

          "foo %active, #active, #6"-{
            val tl = TokenLine(Array("foo", "%active", ",", "#active", ",", "#7"), 5)
            val result = parseParams("foo", tl, WriteableParamType, ReadableParamType,
              ReadableParamType)
            val expectedResult = Right(Seq(ActiveKeyword(false), ActiveKeyword(true),
              RegisterParam(6)))
            result ==> expectedResult
          }
          */



        }

      }

      "set instruction"-{
        "fail"-{
          testProgramFail("set foo", ErrorCode.MalformedInstruction)
          testProgramFail("set #active 1", ErrorCode.MalformedInstruction)
          testProgramFail("set foo, 1", ErrorCode.WrongParamType)
          testProgramFail("set 1, 1", ErrorCode.WrongParamType)
          testProgramFail("set $banks, 1", ErrorCode.WrongParamType)
          testProgramFail("set %banks, 1", ErrorCode.WrongParamType)
          testProgramFail("set $instrset, 1", ErrorCode.WrongParamType)
          testProgramFail("set %instrset, 1", ErrorCode.WrongParamType)
          testProgramFail("set $mobile, 1", ErrorCode.WrongParamType)
          testProgramFail("set %mobile, 1", ErrorCode.WrongParamType)
          testProgramFail("set $fields, 1", ErrorCode.WrongParamType)
          testProgramFail("set #1, $foo", ErrorCode.WrongParamType)
        }
        "succeed"-{

          def testDestParam(param: String, keyword: WriteableParam): Unit =
            testInstruction("set " + param + ", 1",
              Right(SetInstruction(defaultSourceMap, keyword, IntegerParam(1))))

          testDestParam("#1", RegisterParam(0))
          testDestParam("#2", RegisterParam(1))
          testDestParam("#" + config.sim.maxNumVariables,
            RegisterParam(config.sim.maxNumVariables - 1))
          testDestParam("#Active", ActiveKeyword(true))
          testDestParam("%Active", ActiveKeyword(false))

          def testSourceParam(param: String, keyword: ReadableParam): Unit =
            testInstruction("set #1, " + param,
              Right(SetInstruction(defaultSourceMap, RegisterParam(0), keyword)))

          testSourceParam("5", IntegerParam(5))
          testSourceParam("#Active", ActiveKeyword(true))
          testSourceParam("%Active", ActiveKeyword(false))
          testSourceParam("$Banks", BanksKeyword(true))
          testSourceParam("%Banks", BanksKeyword(false))
          testSourceParam("$InstrSet", InstrSetKeyword(true))
          testSourceParam("%InstrSet", InstrSetKeyword(false))
          testSourceParam("$Mobile", MobileKeyword(true))
          testSourceParam("%Mobile", MobileKeyword(false))
          testSourceParam("$Fields", FieldsKeyword())
        }
      }
    }
  }
}