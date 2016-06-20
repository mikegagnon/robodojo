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
    // TODO: change to dur
    // TODO: put these in child object?
    val moveCycles = params.getOrElse("sim.moveCycles", 18).asInstanceOf[Int]
    val turnCycles = params.getOrElse("sim.turnCycles", 5).asInstanceOf[Int]

    // For the create instruction
    val durCreate1 = params.getOrElse("sim.durCreate1", 5).asInstanceOf[Int]
    val durCreate2 = params.getOrElse("sim.durCreate2", 1).asInstanceOf[Int]
    val durCreate3 = params.getOrElse("sim.durCreate3", 1).asInstanceOf[Int]
    val durCreate3a = params.getOrElse("sim.durCreate3a", 5).asInstanceOf[Int]
    val durCreate4 = params.getOrElse("sim.durCreate4", 0).asInstanceOf[Int]
    val durCreate5 = params.getOrElse("sim.durCreate5", 5).asInstanceOf[Int]
    val maxCreateDur = params.getOrElse("sim.maxCreateDur", 50).asInstanceOf[Int]

    val maxBanks = params.getOrElse("sim.maxBanks", 50).asInstanceOf[Int]

    val maxPlayers = params.getOrElse("sim.maxPlayers", 4).asInstanceOf[Int]
  }

  object compiler {
    val maxLineLength = params.getOrElse("compiler.maxLineLength", 1000).asInstanceOf[Int]
  }

  object editor {
    val divId = id + "-editor-div"
    val textAreaId = divId + "-textArea"
    val consoleDivId = divId + "-console-div"
    val compilerOutputId = divId + "-compiler-output"
    val selectBotButtonId = consoleDivId + "-select-bot-button"

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

    val lookAheadCycles = params.getOrElse("viz.lookAheadCycles", sim.maxCreateDur).asInstanceOf[Int]

  }

}
