package robobot.webapp

import utest._

object InstructionTest extends TestSuite {

  implicit val config = Config.default

  def tests = TestSuite {
    
    "Variable with Int == 0"-{
      Variable(Left(0))
    }

    "Variable with Int == simMaxNumVariables - 1"-{
      Variable(Left(config.simMaxNumVariables - 1))
    }

    "Variable with Int == -1"-{
      intercept[IllegalArgumentException] {
        Variable(Left(-1))
      }
    }

    "Variable with Int == simMaxNumVariables"-{
      intercept[IllegalArgumentException] {
        Variable(Left(config.simMaxNumVariables))
      }
    }

  }
}