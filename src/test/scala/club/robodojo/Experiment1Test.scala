package club.robodojo

import utest._

import scala.collection.immutable.IndexedSeq
import scala.scalajs.js
import scala.util.Random

class Experiment1(numCycles: Int) {

  val params = Map[String, Any](
    "sim.numRows" -> 100,
    "sim.numCols" -> 100,
    "monoculture" ->  js.Dictionary[Any](
      "numBots" -> 2000,
      "program" -> """bank main

set #20, 0

@top
move
scan #1
turn 1
move
scan #2
turn 0
comp #1, 0
jump @foundbot
jump @top

@foundbot
comp #2, 0
jump @foundbot2
jump @top

@foundbot2
add #20, 1
comp #20, 100
jump @top
turn 0
set #20, 0

jump @top""")
  )

  implicit val config = new Config(params)
  val board = new Board()(config)

  val program: Program = Compiler.compile(config.monoculture.get.program, PlayerColor.Blue) match {
    case Right(p : Program) => p
    case _ => throw new RuntimeException("bad program")
  }

  val numBots = config.monoculture.get.numBots
    (1 to numBots).foreach { _ =>
      addBot()
    }

  def addBot() {
    var row = Random.nextInt(config.sim.numRows)
    var col = Random.nextInt(config.sim.numCols)

    while (board.matrix(row)(col).nonEmpty) {
      row = Random.nextInt(config.sim.numRows)
      col = Random.nextInt(config.sim.numCols)
    }

    val direction =
      Random.nextInt(4) match {
        case 0 => Direction.Up
        case 1 => Direction.Down
        case 2 => Direction.Left
        case 3 => Direction.Right
        case _ => throw new IllegalStateException("This code shouldn't be reachable")
      }

    val instructionSet = InstructionSet.Super
    val mobile = true
    val active: Short = 1

    val bot = Bot(board, PlayerColor.Blue, row, col, direction, program, instructionSet, mobile, active)

    board.addBot(bot)
  }


  def run() = {
    (1 to numCycles).foreach { _ =>
      board.cycle()
    }
  }
}

object Experiment1Test extends TestSuite {

  implicit val config = Config.default

  val tests = this {
    "Experiment 1"-{
      val experiment = new Experiment1(10000)
      experiment.run()
      val values = experiment.board.matrix.map { row =>
        row.map {
          case Some(_) => "1"
          case _ => " "
        }.mkString("")
      }.mkString("\n")
      println(values)
    }
  }
}