package robobot.webapp

object Compiler {

  case class TokenLine(tokens: Array[String], lineNumber: Int) {

    override def equals(that: Any): Boolean =
      that match {
        case tl: TokenLine => tokens.sameElements(tl.tokens) && lineNumber == tl.lineNumber
        case _ => false
      }

    override def toString(): String = s"TokenLine([${tokens.mkString(",")}], ${lineNumber})"

  }

  case class CompileTokenLineResult(
    instruction: Option[Instruction],
    // Some((description of error, line number))
    errorMessage: Option[(String, Int)])

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

  def unrecognizedInstruction(tl: TokenLine) =
    CompileTokenLineResult(None,
        Some((s"Unrecognized instruction name: ${tl.tokens(0)}", tl.lineNumber)))

  def compileMove(tl: TokenLine)(implicit config: Config): CompileTokenLineResult =

    // TODO: reorder so length == 1 goes first. All should follow this style
    if (tl.tokens.length > 1) {
      // TODO: all compile instruction functions should follow this style
      val errorMessage = "The move instruction does not take any parameters"
      return CompileTokenLineResult(None, Some(errorMessage, tl.lineNumber))
    } else if (tl.tokens.length == 1) {
      return CompileTokenLineResult(Some(MoveInstruction()), None)
    } else {
      throw new IllegalStateException("This code shouldn't be reachable")
    }

  // TODO: test
  def compile(text: String)(implicit config: Config): Map[Int, Bank] = {

    val lines: Array[TokenLine] = tokenize(text)
    var banks = Map[Int, Bank](0 -> new Bank) 
    var bankNumber = 0

    def compileBank(tl: TokenLine): CompileTokenLineResult =
      if (tl.tokens.length > 2) {
        return CompileTokenLineResult(None,
          Some(("bank directive takes only one optional parameter", tl.lineNumber)))
      } else {
        bankNumber += 1
        banks += (bankNumber -> new Bank)
        return CompileTokenLineResult(None, None)
      }

    // TODO: very friendly error messages
    lines.foreach { case (tl: TokenLine) =>
      val result: CompileTokenLineResult = tl.tokens(0) match {
        case "bank" => compileBank(tl)
        case "move" => compileMove(tl)
        case _ => unrecognizedInstruction(tl)
      }
    }

    return banks
  }

}