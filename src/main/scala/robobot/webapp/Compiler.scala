package robobot.webapp

object Compiler {

  def tokenize(text: String): Array[Array[String]] = {
    text
      .split("\n")
      // Remove comments
      .map { line: String => line.replaceAll(";.*", "") }
      // Separate into tokens
      .map { line: String => line.split("""\s+""") }
      // Drop empty tokens
      .map { line: Array[String] =>
        line.filter { _ != "" }
      }

  }

  def compile(text: String): Map[Int, Bank] = {

    null
  }

}