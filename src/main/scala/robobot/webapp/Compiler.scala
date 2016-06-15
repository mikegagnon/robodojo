package robobot.webapp

object Compiler {

  def tokenize(text: String): Array[Array[String]] = {
    text
      .split("\n")
      // Remove comments
      .map { line =>
        line.replaceAll(""";.*""", "")
      }
      .map { line =>
        line.split("""\s+""")
      }

  }

  def compile(text: String): Map[Int, Bank] = {

    null
  }

}