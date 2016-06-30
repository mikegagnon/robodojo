package club.robodojo

import scala.collection.mutable.ArrayBuffer

case class BankBuilder(
    var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction](),
    var sourceMap: Option[SourceMap] = None) {
  
  // TODO: use sourceMap builder
  def build() = Bank(instructions.toIndexedSeq, sourceMap)
}


case class Bank(
  // TODO: instructions is immutable?
  val instructions: IndexedSeq[Instruction],
  val sourceMap: Option[SourceMap])

object Program {

  def emptyProgram(numBanks: Int): Program = {
    if (numBanks < 1) {
      throw new IllegalArgumentException("Empty programs must have at least one bank")
    }

    var banks = Map[Int, Bank]()
    0 until numBanks foreach { bankNum =>
      banks += bankNum -> Bank(IndexedSeq[Instruction](), None)
    }

    return Program(banks)
  }
}

case class Program(var banks: Map[Int, Bank]) {
  def isEmpty(): Boolean = banks.isEmpty
}