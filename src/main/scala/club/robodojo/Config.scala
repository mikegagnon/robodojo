package club.robodojo

object Config {
  val default = new Config(Map[String,Any]())
}

// TODO: change defs to vals?
// TODO: remove all SVG config vals
class Config(params: Map[String, Any] = Map[String, Any]()) {

  val id: String = params.getOrElse("id", "robodojo").asInstanceOf[String]

  // simulation constants
  object sim {
    val maxNumVariables = params.getOrElse("sim.maxNumVariables", 20).asInstanceOf[Int]

    val numRows: Int = params.getOrElse("sim.numRows", 3).asInstanceOf[Int]
    val numCols: Int = params.getOrElse("sim.numCols", 6).asInstanceOf[Int]

    // instruction constants
    val moveCycles = params.getOrElse("sim.moveCycles", 18).asInstanceOf[Int]
    val turnCycles = params.getOrElse("sim.turnCycles", 5).asInstanceOf[Int]

    val maxBanks = params.getOrElse("sim.maxBanks", 50).asInstanceOf[Int]

    val maxPlayers = params.getOrElse("sim.maxPlayers", 4).asInstanceOf[Int]
  }

  object compiler {
    val maxLineLength = params.getOrElse("compiler.maxLineLength", 1000).asInstanceOf[Int]
  }

  // TODO: get rid of useless ids
  object editor {
    val divId = id + "-editor-div"
    val textAreaId = divId + "-textArea"
    val consoleDivId = divId + "-console"
    val codemirrorDivId = divId + "-codemirror-div"
    val compilerOutputId = divId + "-compiler-output"
    val selectBotButtonId = consoleDivId + "-selectBotButton"

    val defaultPrograms = Map(
      0 -> "bank Main\nmove\nmove\nturn 1\nmove\nmove\nmove\nturn 0",
      1 -> "bank Main\nmove",
      2 -> "",
      3 -> "")
  }

  object viz {

    val boardWrapperDivId = id + "-board-wrapper"

    // TODO: shouldn't this (and more) go into object controller, outside of viz
    def consoleDivId = id + "-console"

    val cellSize = params.getOrElse("viz.cellSize", 32).asInstanceOf[Int]
    val framesPerSecond = params.getOrElse("viz.framesPerSecond", 30).asInstanceOf[Int]
    val cyclesPerSecond = params.getOrElse("viz.cyclesPerSecond", 30).asInstanceOf[Int]

    object canvas {
      val canvasId = id + "-canvas"
      def width = cellSize * sim.numCols
      def height = cellSize * sim.numRows
    }

    object border {
      val stroke = params.getOrElse("viz.border.stroke", "#444").asInstanceOf[String]

      // TODO: delete?
      val thickness = params.getOrElse("viz.border.thickness", 1).asInstanceOf[Int]
    }

    object preload {
      val blueBotId = "blueBotId"
      val blueBotPath = "./img/bluebot.png"
      val redBotId = "redBotId"
      val redBotPath = "./img/redbot.png"
      val greenBotId = "greenBotId"
      val greenBotPath = "./img/greenbot.png"
      val yellowBotId = "yellowBotId"
      val yellowBotPath = "./img/yellowbot.png"

    }

    // grid lines
    object grid {
      val stroke = params.getOrElse("viz.grid.stroke", "#ccc").asInstanceOf[String]
    }

  }

}
