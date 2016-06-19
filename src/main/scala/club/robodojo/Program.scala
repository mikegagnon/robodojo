package club.robodojo

import scala.collection.mutable.ArrayBuffer

case class Bank(var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]())

case class Program(var banks: Map[Int, Bank]) {

  // TODO: is this the right function name?
  def isEmpty(): Boolean = banks.isEmpty
}