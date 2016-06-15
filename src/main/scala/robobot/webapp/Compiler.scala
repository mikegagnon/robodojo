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

  // TODO: test
  def compile(text: String): Map[Int, Bank] = {

    val lines: Array[TokenLine] = tokenize(text)
    var bankNumber = 0

    def compileBank(tl: TokenLine): CompileTokenLineResult =
      if (tl.tokens.length > 2) {
        return CompileTokenLineResult(None,
          Some(("bank directive takes only one optional parameter", tl.lineNumber)))
      } else {
          bankNumber += 1
          return CompileTokenLineResult(None, None)
      }

    def unrecognizedInstruction(tl: TokenLine) =
      CompileTokenLineResult(None,
          Some((s"Unrecognized instruction name: ${tl.tokens(0)}", tl.lineNumber)))

    // TODO: very friendly error messages
    lines.foreach { case (tl: TokenLine) =>

      val result: CompileTokenLineResult = tl.tokens(0) match {
        case "bank" => compileBank(tl)
        case _ => unrecognizedInstruction(tl)
      }
    }

    null
  }

}