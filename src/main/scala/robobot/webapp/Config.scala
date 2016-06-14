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
    val framesPerSecond = params.getOrElse("viz.framesPerSecond", 30).asInstanceOf[Int]
    val cyclesPerSecond = params.getOrElse("viz.cyclesPerSecond", 100).asInstanceOf[Int]

    object canvas {
      val canvasId = id + "-canvas"
      def width = cellSize * sim.numCols
      def height = cellSize * sim.numRows
    }

    object border {
      val stroke = params.getOrElse("viz.border.stroke", "#444").asInstanceOf[String]
      val thickness = params.getOrElse("viz.border.thickness", 2).asInstanceOf[Int]
    }

    object preload {
      val blueBotId = "blueBotId"
      val blueBotPath = "./img/bluebot.png"
    }

    // grid lines
    object grid {
      val stroke = params.getOrElse("viz.grid.stroke", "#ccc").asInstanceOf[String]
    }

  }

}
