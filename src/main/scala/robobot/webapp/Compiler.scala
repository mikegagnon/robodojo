package robobot.webapp

object Compiler {

  case class TokenLine(tokens: Array[String], lineNumber: Int)

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

  def compile(text: String): Map[Int, Bank] = {

    null
  }

}