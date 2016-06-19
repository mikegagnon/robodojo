package club.robodojo

import scala.collection.mutable.ArrayBuffer

case class Bank(var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]())

object Program {

  // TODO: TEST
  def emptyProgram(numBanks: Int): Program = {
    var banks = Map[Int, Bank]()
    1 to numBanks foreach { bankNum =>
      banks += bankNum -> Bank(ArrayBuffer[Instruction]())
    }

    return Program(banks)
  }
}

case class Program(var banks: Map[Int, Bank]) {
  def isEmpty(): Boolean = banks.isEmpty
}