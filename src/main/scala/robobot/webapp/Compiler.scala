package robobot.webapp

import scala.collection.mutable.ArrayBuffer

case class TokenLine(tokens: Array[String], lineNumber: Int) {
  override def equals(that: Any): Boolean =
    that match {
      case tl: TokenLine => tokens.sameElements(tl.tokens) && lineNumber == tl.lineNumber
      case _ => false
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
}

// TODO: underline the offensive text in the program text?
// TODO: hyperlink error messages?
case class ErrorMessage(errorCode: ErrorCode.EnumVal, lineNumber: Int, message: String)

case class CompileLineResult(
  instruction: Option[Instruction],
  errorMessage: Option[ErrorMessage])


object Compiler {

  // TODO: deal with commas. How about replace all commas with " , "
  def tokenize(text: String)(implicit config: Config): Array[TokenLine] =
    text
      .split("\n")
      // Slice the string so only the first config.compiler.maxLineLength characters are kept
      .map { line: String =>
        line.slice(0, config.compiler.maxLineLength)
      }
      // Remove comments
      .map { line: String => line.replaceAll(";.*", "") }
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
        !(head == "Name" || head == "Author" || head == "Country")
      }

  def unrecognizedInstruction(tl: TokenLine) = {
    val message = s"Unrecognized instruction name: ${tl.tokens(0)}"
    val errorMessage = ErrorMessage(ErrorCode.UnrecognizedInstruction, tl.lineNumber, message)
    CompileLineResult(None, Some(errorMessage))
  }

  def compileBank(tl: TokenLine): CompileLineResult =
    if (tl.tokens.length > 2) {
      val message = "Too many parameters: the <tt>bank</tt> directive takes exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length < 2) {
      val message = "Too few parameters: the <tt>bank</tt> directive takes exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.MissingParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else {
      CompileLineResult(None, None)
    }

  def compileMove(tl: TokenLine)(implicit config: Config): CompileLineResult =

    if (tl.tokens.length > 1) {
      val message = "The move instruction does not take any parameters"
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length == 1) {
      val instruction = MoveInstruction()
      CompileLineResult(Some(instruction), None)
    } else {
      throw new IllegalStateException("This code shouldn't be reachable")
    }

  def compileTurn(tl: TokenLine)(implicit config: Config): CompileLineResult =

    if (tl.tokens.length < 2) {
      val message = "Missing parameter: the <tt>turn</tt> instruction requires an integer parameter"
      val errorCode = ErrorCode.MissingParams
      val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length > 2) {
      val message = "Too many parameters: the <tt>turn</tt> instruction requires exactly one " +
        "integer parameter"
      val errorCode = ErrorCode.TooManyParams
      val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
      CompileLineResult(None, Some(errorMessage))   
    } else {

      // TODO: take non-literal params?
      try {
        val leftOrRight = tl.tokens(1).toInt
        val instruction = TurnInstruction(leftOrRight)
        CompileLineResult(Some(instruction), None)
      } catch {
        case _ : NumberFormatException => {
          val message = "Wrong parameter type: the <tt>turn</tt> instruction requires an integer " +
            "parameter"
          val errorCode = ErrorCode.WrongParamType
          val errorMessage = ErrorMessage(errorCode, tl.lineNumber, message)
          CompileLineResult(None, Some(errorMessage))
        }
      }

    }

  // TODO: test
  def compile(text: String)(implicit config: Config): Either[ArrayBuffer[ErrorMessage], Program] = {

    val lines: Array[TokenLine] = tokenize(text)
    var banks = Map[Int, Bank]()
    var bankNumber = -1

    // TODO: get ride of error and just check for nonempty errors
    var error = false
    var errors = ArrayBuffer[ErrorMessage]()

    // TODO: very friendly error messages
    lines.foreach { case (tl: TokenLine) =>
      val result: CompileLineResult = tl.tokens(0) match {
        // TODO: ensure bankNumber < maxBanks
        case "bank" => {
          if (bankNumber == config.sim.maxBanks - 1) {
              val errorMessage = ErrorMessage(ErrorCode.MaxBanksExceeded, tl.lineNumber,
                s"Too many banks: you may only have ${config.sim.maxBanks} banks.")
              CompileLineResult(None, Some(errorMessage))
          } else {
            bankNumber += 1
            // TODO: predeclare all banks to empty banks?
            banks += (bankNumber -> new Bank)
            compileBank(tl)
          }

        }
        case "move" => compileMove(tl)
        case "turn" => compileTurn(tl)
        case _ => unrecognizedInstruction(tl)
      }

      result.errorMessage match {
        case Some(errorMessage) => {
          error = true
          banks = Map[Int, Bank]()
          errors += errorMessage
        }
        case None => ()
      }

      if (!error) {
        result.instruction.foreach { instruction: Instruction =>
          if (bankNumber >= 0) {
            banks(bankNumber).instructions += instruction
          } else {
            // TODO: test
            error = true
            errors += ErrorMessage(ErrorCode.UndeclaredBank, tl.lineNumber, "Undeclared " +
              "bank: you must place a <tt>bank</tt> directive before you place any instructions")
          }
        }
      }

    }

    if (error) {
      Left(errors)
    } else {
      Right(Program(banks))
    }
  }

}