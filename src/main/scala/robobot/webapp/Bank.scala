package robobot.webapp

import scala.collection.mutable.ArrayBuffer

case class Bank(var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]())

// TODO: replace instances of banks with Program
case class Program(val banks: Map[Int, Bank])