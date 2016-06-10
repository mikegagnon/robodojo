package robobot.webapp

object Config {
  val default = new Config(Map[String,Any]())
}

// TODO: change defs to vals?
// TODO: remove all SVG config vals
class Config(params: Map[String, Any]) {

  val id: String = params.getOrElse("id", "robobot").asInstanceOf[String]

  // simulation constants
  object sim {
    val maxNumVariables = params.getOrElse("sim.maxNumVariables", 20).asInstanceOf[Int]

    val numRows: Int = params.getOrElse("sim.numRows", 3).asInstanceOf[Int]
    val numCols: Int = params.getOrElse("sim.numCols", 6).asInstanceOf[Int]

    // instruction constants
    val moveCycles = params.getOrElse("sim.moveCycles", 18).asInstanceOf[Int]
    val turnCycles = params.getOrElse("sim.turnCycles", 5).asInstanceOf[Int]
  }

  object viz {

    def consoleDivId = id + "-console"

    val cellSize = params.getOrElse("viz.cellSize", 32).asInstanceOf[Int]

    // SVG border element
    object canvas {
      def width = cellSize * sim.numCols
      def height = cellSize * sim.numRows
    }

    // grid lines
    object grid {
      val stroke = params.getOrElse("viz.grid.stroke", "#ccc").asInstanceOf[String]
    }

  }

}
