package club.robodojo

import utest._
import scala.collection.immutable.IndexedSeq

object ProgramTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {
    "Program deepCopy"-{
      val color = PlayerColor.Blue
      val bank0 = Bank(IndexedSeq(MoveInstruction(SourceMapInstruction(0, 0))), Some(SourceMap(color, 0, IndexedSeq("move"))))
      val bank1 = Bank(IndexedSeq(TurnInstruction(SourceMapInstruction(0, 1), IntegerParam(0))), Some(SourceMap(color, 1, IndexedSeq("turn 0"))))
      val program = Program(Map(0 ->  bank0, 1 -> bank1))

      val newProgram = program.deepCopy()

      // Make sure program and newProgram are distinct objects
      program.banks -= 0
      assert(program != newProgram)

      program.banks += 0 -> bank0
      program ==> newProgram

    }
  }

}