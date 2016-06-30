package club.robodojo

import scala.collection.mutable.ArrayBuffer

// TODO: builder pattern?
case class Bank(
  var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction](),
  var sourceMap: Option[SourceMap] = None)

object Program {

  def emptyProgram(numBanks: Int): Program = {
    if (numBanks < 1) {
      throw new IllegalArgumentException("Empty programs must have at least one bank")
    }

    var banks = Map[Int, Bank]()
    0 until numBanks foreach { bankNum =>
      banks += bankNum -> Bank(ArrayBuffer[Instruction]())
    }

    return Program(banks)
  }
}

case class Program(var banks: Map[Int, Bank]) {
  def isEmpty(): Boolean = banks.isEmpty
}