package club.robodojo

import scala.collection.mutable.ArrayBuffer

case class TokenLine(tokens: Array[String], lineNumber: Int) {

  // TESTED
  override def equals(that: Any): Boolean =
    that match {
      case tl: TokenLine => tokens.sameElements(tl.tokens) && lineNumber == tl.lineNumber
      case _ => false
    }
  
  // TESTED
  override def hashCode: Int = {
    var x = 13
    tokens.foreach { str: String => x *= (str.hashCode + 13)}

    return x * (lineNumber.hashCode + 13)
  }

  override def toString(): String = s"TokenLine([${tokens.mkString(",")}], ${lineNumber})"
}

object ErrorCode {
  sealed trait EnumVal
  case object UnrecognizedInstruction extends EnumVal
  case object TooManyParams extends EnumVal
  case object MissingParams extends EnumVal
  case object WrongParamType extends EnumVal
  case object UndeclaredBank extends EnumVal
  case object MaxBanksExceeded extends EnumVal
  case object EmptyBanks extends EnumVal
  case object MalformedCreate extends EnumVal
  case object BadInstructionSetParam extends EnumVal
  case object BadNumBanksParam extends EnumVal
  case object BadMobileParam extends EnumVal
}

// TODO: underline the offensive text in the program text?
// TODO: hyperlink error messages?
case class ErrorMessage(errorCode: ErrorCode.EnumVal, lineNumber: Int, message: String)

case class CompileLineResult(
  instruction: Option[Instruction],
  errorMessage: Option[ErrorMessage])

object Compiler {

  // TESTED
  def tokenize(text: String)(implicit config: Config): Array[TokenLine] =
    text
      .split("\n")
      // Slice the string so only the first config.compiler.maxLineLength characters are kept
      .map { line: String =>
        line.slice(0, config.compiler.maxLineLength)
      }
      // Remove comments
      .map { line: String => line.replaceAll(";.*", "") }
      .map { line: String => line.replaceAll(",", " , ")}
      .map { line: String => line.toLowerCase }
      // Separate into tokens
      .map { line: String => line.split("""\s+""") }
      // Drop empty tokens
      .map { tokens: Array[String] =>
        tokens.filter { _ != "" }
      }
      // Pair lines with line numbers
      .zipWithIndex
      .map { case (tokens: Array[String], lineNumber: Int) =>
        TokenLine(tokens, lineNumber)
      }
      // Drop empty lines
      .filter { case TokenLine(tokens, _) =>
        tokens.nonEmpty
      }
      .filter { case TokenLine(tokens, _) =>
        val head = tokens.head
        !(head == "name" || head == "author" || head == "country")
      }

  def unrecognizedInstruction(tl: TokenLine) = {
    val message = s"Unrecognized instruction: <tt>${tl.tokens(0)}</tt>."
    val errorMessage = ErrorMessage(ErrorCode.UnrecognizedInstruction, tl.lineNumber, message)
    CompileLineResult(None, Some(errorMessage))
  }

  // TESTED
  def compileBank(tl: TokenLine): CompileLineResult =
    if (tl.tokens.length > 2) {
      val message = "Too many parameters: the <tt>bank</tt> directive requires exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length < 2) {
      val message = "Missing parameter: the <tt>bank</tt> directive requires exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.MissingParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else {
      CompileLineResult(None, None)
    }

  // TESTED
  def compileMove(tl: TokenLine)(implicit config: Config): CompileLineResult =
    if (tl.tokens.length > 1) {
      val message = "Too many parameters: the <tt>move</tt> instruction does not take any " +
        "parameters."
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length == 1) {
      val instruction = MoveInstruction()
      CompileLineResult(Some(instruction), None)
    } else {
      throw new IllegalStateException("This code shouldn't be reachable")
    }

  // TESTED
  def compileTurn(tl: TokenLine)(implicit config: Config): CompileLineResult =
    if (tl.tokens.length < 2) {
      val message = "Missing parameter: the <tt>turn</tt> instruction requires an integer paramete."
      val errorCode = ErrorCode.MissingParams
      val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length > 2) {
      val message = "Too many parameters: the <tt>turn</tt> instruction requires exactly one " +
        "integer parameter."
      val errorCode = ErrorCode.TooManyParams
      val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))   
    } else {

      val param = tl.tokens(1)

      // TODO: take non-literal params?
      try {
        val leftOrRight = if (param.toInt == 0) {
          Direction.Left
        } else {
          Direction.Right
        }
        val instruction = TurnInstruction(leftOrRight)
        CompileLineResult(Some(instruction), None)
      } catch {
        case _ : NumberFormatException => {
          val message = "Wrong parameter type: the <tt>turn</tt> instruction requires an integer " +
            s"parameter. <tt>${param}</tt> is not an integer."
          val errorCode = ErrorCode.WrongParamType
          val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
          CompileLineResult(None, Some(errorMessage))
        }
      }
    }

  def isInt(value: String): Boolean =
    try {
      value.toInt
      true
    } catch {
      case _ : NumberFormatException => false
    }

  // TODO: make compliant with Robocom standard
  // TESTED
  def compileCreate(tl: TokenLine)(implicit config: Config): CompileLineResult =
    if (tl.tokens.length != 6 ||
        tl.tokens(2) != "," ||
        tl.tokens(4) != "," ||
        !isInt(tl.tokens(1)) ||
        !isInt(tl.tokens(3)) ||
        !isInt(tl.tokens(5))) {
      val message = "Malformed create instruction: the <tt>create</tt> instruction must be of " +
      "the form: <tt>create a, b, c</tt>, where <tt>a</tt>, <tt>b<tt>, and <tt>c</tt> are " +
      "integers."
      val errorCode = ErrorCode.MalformedCreate
      val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
      return CompileLineResult(None, Some(errorMessage))
    } else {

      val instructionSetToken = tl.tokens(1).toInt
      val numBanksToken = tl.tokens(3).toInt
      val mobileToken = tl.tokens(5).toInt

      // Check for errors
      if (config.compiler.safetyChecks) {
        if (instructionSetToken < 0 || instructionSetToken > 1) {
          val message = "Bad instruction-set parameter: the first parameter to the <tt>create</tt> " +
            "instruction, the instruction-set parameter, can only be 0 (signifying the " +
            "<i>Basic</i> instruction set) or 1 (signifying the <i>Extended</i> instruction set)."
          val errorCode = ErrorCode.BadInstructionSetParam
          val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
          return CompileLineResult(None, Some(errorMessage))
        } else if (numBanksToken < 1 || numBanksToken > 50) {
          val message = "Bad numBanks parameter: the second parameter to the <tt>create</tt> " +
            "instruction, the numBanks parameter, must be greater (or equal to) 1 and less than " +
            s"(or equal to) ${config.sim.maxBanks}"
          val errorCode = ErrorCode.BadNumBanksParam
          val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
          return CompileLineResult(None, Some(errorMessage))
        } else if (mobileToken < 0 || mobileToken > 1) {
          val message = "Bad mobile parameter: the third parameter to the <tt>create</tt> " +
            "instruction, the mobile parameter, can only be 0 (signifying immobility) or 1 " +
            "(signifying mobility)."
          val errorCode = ErrorCode.BadMobileParam
          val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
          return CompileLineResult(None, Some(errorMessage))
        }
      }

      val instructionSet = if (instructionSetToken == 0) {
          InstructionSet.Basic
        } else if (instructionSetToken == 1) {
          InstructionSet.Extended
        } else {
          if (config.compiler.safetyChecks) {
            throw new IllegalStateException("This code shouldn't be reachable")
          } else {
            InstructionSet.Basic
          }
        }

      val numBanks = numBanksToken
      val mobile = mobileToken == 1

      val instruction = CreateInstruction(instructionSet, numBanks, mobile)

      CompileLineResult(Some(instruction), None)
    }

  // TESTED
  def compile(text: String)(implicit config: Config): Either[ArrayBuffer[ErrorMessage], Program] = {

    val lines: Array[TokenLine] = tokenize(text)
    var banks = Map[Int, Bank]()
    var bankNumber = -1
    var errors = ArrayBuffer[ErrorMessage]()

    lines.foreach { case (tl: TokenLine) =>
      val result: CompileLineResult = tl.tokens(0) match {
        case "bank" => {
          if (bankNumber == config.sim.maxBanks - 1) {
              val errorMessage = ErrorMessage(ErrorCode.MaxBanksExceeded, tl.lineNumber,
                s"Too many banks: programs may only have ${config.sim.maxBanks} banks.")
              CompileLineResult(None, Some(errorMessage))
          } else {
            // TODO: predeclare all banks to empty banks?
            bankNumber += 1
            banks += (bankNumber -> new Bank)
            compileBank(tl)
          }
        }
        case "move" => compileMove(tl)
        case "turn" => compileTurn(tl)
        case "create" => compileCreate(tl)
        case _ => unrecognizedInstruction(tl)
      }

      result.errorMessage match {
        case Some(errorMessage) => {
          banks = Map[Int, Bank]()
          errors += errorMessage
        }
        case None => ()
      }

      if (errors.isEmpty) {
        result.instruction.foreach { instruction: Instruction =>
          if (bankNumber >= 0) {
            banks(bankNumber).instructions += instruction
          } else {
            errors += ErrorMessage(ErrorCode.UndeclaredBank, tl.lineNumber, "Undeclared " +
              "bank: you must place a <tt>bank</tt> directive before you place any instructions.")
          }
        }
      }

    }

    if (errors.nonEmpty) {
      Left(errors)
    } else if (banks.isEmpty) {
      Left(ArrayBuffer(ErrorMessage(ErrorCode.EmptyBanks, 0, "Your program is empty.")))
    } else {
      Right(Program(banks))
    }
  }

}