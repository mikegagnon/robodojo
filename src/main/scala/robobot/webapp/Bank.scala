package robobot.webapp

import scala.collection.mutable.ArrayBuffer


// TODO: ensure instructions.length < max bank size
case class Bank(var instructions: ArrayBuffer[Instruction] = ArrayBuffer[Instruction]())

// TODO: replace instances of banks with Program
case class Program(val banks: Map[Int, Bank])