package club.robodojo

import scala.collection.mutable.ArrayBuffer

// NOTE: Although instructions and sourceMap are var, they never get modified after compilation.
// So they are safe to shallow copy in Program.deepCopy.
// TODO: BankBuilder?
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

  def deepCopy(): Program = {
    val newBanks = banks.map{ case (k, v) => (k, v) }
    return Program(newBanks)
  }

}