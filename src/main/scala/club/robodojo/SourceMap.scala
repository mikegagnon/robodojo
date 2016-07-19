package club.robodojo

import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.IndexedSeq

// These classes facilitate debugging by storing source code information.

// Every bank stores a SourceMap instance.
//    playerColor: the color of the program this bank originally came from
//    origBankIndex: relative to the original program
//    text: the entire source code for the bank
case class SourceMap(playerColor: PlayerColor.EnumVal, origBankIndex: Int, text: IndexedSeq[String])

// Every instruction stores a SourceMapInstruction instance.
//    lineIndex: the source code line index, relative to the bank it belongs to
//    bankIndex: same as SourceMap.bankIndex
case class SourceMapInstruction(lineIndex: Int, bankIndex: Int)
