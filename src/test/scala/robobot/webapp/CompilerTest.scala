package robobot.webapp

import utest._

object CompilerTest extends TestSuite {


  val tests = this {

    "tokenize"-{

      val text = """

      a   b    c d ; foo    


      x     y   x   ;


      """

      Compiler.tokenize(text)
        .foreach { line: Array[String] =>
          println(line.mkString(","))
        }

    }
  }
}