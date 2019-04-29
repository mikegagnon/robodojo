package club.robodojo

import utest._

import scala.collection.immutable.IndexedSeq
import scala.scalajs.js
import scala.util.Random

class Experiment1(numBots: Int, numCycles: Int) {

  val params = Map[String, Any](
    "sim.numRows" -> 100,
    "sim.numCols" -> 100,
    "monoculture" ->  js.Dictionary[Any](
      "numBots" -> numBots,
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

  //val numBots = 
    config.monoculture.get.numBots
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

  def binaryMap = board.matrix.map { row =>
      row.map {
        case Some(_) => "1"
        case _ => " "
      }.mkString("")
    }.mkString("\n")

  // TODO: document
  def getCount(distance: Int, rCenter: Int, cCenter: Int) : Double = {
    var rStart = rCenter - distance
    var rEnd = rCenter + distance
    var cSTart = cCenter - distance
    var cEnd = cCenter + distance

    // TODO: edge case off by one error?
    val count = (rStart to rEnd).flatMap { r =>
      (cSTart to cEnd).map { c =>

        // TODO: off by one?
        val rWrapped = if (r < 0) {
          board.config.sim.numRows + r
        } else if (r >= board.config.sim.numRows) {
          r % board.config.sim.numRows
        } else {
          r
        }

        val cWrapped = if (c < 0) {
          board.config.sim.numCols + c
        } else if (c >= board.config.sim.numCols) {
          c % board.config.sim.numCols
        } else {
          c
        }

        board.matrix(rWrapped)(cWrapped) match {
          case Some(_) => 1
          case _ => 0
        }

      }
    }.sum

    count
  }



  def assignGroups = {

    val matrixGroups = Array.fill[Option[Int]](config.sim.numRows, config.sim.numCols)(None)
    var nextId = 0

    def assignToGroup(id: Int, r: Int, c: Int) : Boolean = {

      val rr: Int = if (r == -1) {
        config.sim.numRows - 1
      } else if (r == config.sim.numRows) {
        0
      } else {
        r
      }

      val cc: Int = if (c == -1) {
        config.sim.numCols - 1
      } else if (c == config.sim.numCols) {
        0
      } else {
        c
      }

      board.matrix(rr)(cc) match {
        case None => false // already visited
        case Some(_) => matrixGroups(rr)(cc) match {
          case None => {
            matrixGroups(rr)(cc) = Some(id)
            assignToGroup(id, rr - 1, cc)
            assignToGroup(id, rr + 1, cc)
            assignToGroup(id, rr, cc - 1)
            assignToGroup(id, rr, cc + 1)
            true
          }
          case Some(_) => false
        }
      }

    }

    (0 until board.config.sim.numRows).foreach { r => 
      (0 until board.config.sim.numCols).foreach { c =>
        if (assignToGroup(nextId, r, c)) {
          nextId += 1
        }
        /*board.matrix(r)(c) match {
          case Some(_) => assignToGroup(matrixGroups, r, c)
          case _ => ()
        }*/
      }
    }

    matrixGroups.map { row =>
        row.map {
          case Some(groupId) => (groupId % 10).toString
          case None => " "
        }.mkString("")
      }.mkString("\n")
  }

  def densityMap(distance: Int) = {
    var min: Option[Double] = None
    var max: Option[Double] = None
    val counts = (0 until board.config.sim.numRows).map { r => 
      (0 until board.config.sim.numCols).map { c =>
        val d = getCount(distance, r, c)
        if (min == None) {
          min = Some(d)
          max = Some(d)
        }

        if (d < min.get)  {
          min = Some(d)
        } else if (d > max.get) {
          max = Some(d)
        }
        d
      }
    }

    val diff = max.get - min.get

    counts.map { row =>
      row.map { count =>
        ((count - min.get.toDouble) / diff.toDouble * 10.0).toInt
      }
    }

  }
}

object Experiment1Test extends TestSuite {

  implicit val config = Config.default

  val tests = this {
    "Experiment 1"-{
      val experiment = new Experiment1(1000, 10000)
      experiment.run()
      println(experiment.binaryMap)
      println(experiment.assignGroups)
      /*val dm = experiment.densityMap(50).map { row =>
        row.map { count =>
          count//"%1d".format(count)
        }.mkString("")
      }.mkString("\n")
      println(dm)*/
    }
  }
}
