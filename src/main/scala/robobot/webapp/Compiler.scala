package robobot.webapp

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
}

case class ErrorMessage(errorCode: ErrorCode.EnumVal, lineNumber: Int, message: String)

case class CompileLineResult(
  instruction: Option[Instruction],
  errorMessage: Option[ErrorMessage])


object Compiler {

  // TODO: filter out name, author, and country
  def tokenize(text: String): Array[TokenLine] =
    text
      .split("\n")
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
      .filter { case TokenLine(tokens, lineNumber) =>
        tokens.nonEmpty
      }

  def unrecognizedInstruction(tl: TokenLine) = {
    val message = s"Unrecognized instruction name: ${tl.tokens(0)}"
    val errorMessage = ErrorMessage(ErrorCode.UnrecognizedInstruction, tl.lineNumber, message)
    CompileLineResult(None, Some(errorMessage))
  }

  def compileBank(tl: TokenLine): CompileLineResult =
    if (tl.tokens.length > 2) {
      val message = "The <tt>bank</tt> directive takes at most one parameter"
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineNumber, message)
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
  def compile(text: String)(implicit config: Config): Map[Int, Bank] = {

    val lines: Array[TokenLine] = tokenize(text)
    var banks = Map[Int, Bank](0 -> new Bank) 
    var bankNumber = 0
    var error = false
    var errors = 

    // TODO: very friendly error messages
    lines.foreach { case (tl: TokenLine) =>
      val result: CompileLineResult = tl.tokens(0) match {
        case "bank" => {
          bankNumber += 1
          banks += (bankNumber -> new Bank)
          compileBank(tl)
        }
        case "move" => compileMove(tl)
        case "turn" => compileTurn(tl)
        case _ => unrecognizedInstruction(tl)
      }

      result.errorMessage match {
        case Some(errorMessage) => {
          error = true
        }
        case None => ()
      }

    }

    return banks
  }

}