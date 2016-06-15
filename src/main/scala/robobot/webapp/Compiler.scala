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