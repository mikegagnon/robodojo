package club.robodojo

import scala.collection.mutable.ArrayBuffer

case class Bank(var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]())

case class Program(var banks: Map[Int, Bank])