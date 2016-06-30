package club.robodojo

import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.IndexedSeq

case class BankBuilder(
  var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction](),
  var sourceMap: Option[SourceMap] = None)

case class Bank(
  val instructions: IndexedSeq[Instruction] = IndexedSeq(),
  val sourceMap: Option[SourceMap] = None)

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

  def deepCopy(): Program = {
    val newBanks = banks.map{ case (k, v) => (k, v) }
    return Program(newBanks)
  }

}