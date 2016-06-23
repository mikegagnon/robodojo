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

    // Update according to http://robocom.rrobek.de/help/instr1.html
    object cycleCount {

      val durMove = params.getOrElse("sim.moveCycles", 20).asInstanceOf[Int]
      val durTurn = params.getOrElse("sim.turnCycles", 8).asInstanceOf[Int]

      // For the create instruction
      val durCreate1 = params.getOrElse("sim.durCreate1", 100).asInstanceOf[Int]
      val durCreate2 = params.getOrElse("sim.durCreate2", 25).asInstanceOf[Int]
      val durCreate3 = params.getOrElse("sim.durCreate3", 1).asInstanceOf[Int]
      val durCreate3a = params.getOrElse("sim.durCreate3a", 120).asInstanceOf[Int]
      val durCreate4 = params.getOrElse("sim.durCreate4", 40).asInstanceOf[Int]
      val durCreate5 = params.getOrElse("sim.durCreate5", 100).asInstanceOf[Int]
      val maxCreateDur = params.getOrElse("sim.maxCreateDur", 1500).asInstanceOf[Int]
    }

    val maxBanks = params.getOrElse("sim.maxBanks", 50).asInstanceOf[Int]

    val maxPlayers = params.getOrElse("sim.maxPlayers", 4).asInstanceOf[Int]
  }

  object compiler {
    val maxLineLength = params.getOrElse("compiler.maxLineLength", 1000).asInstanceOf[Int]
    val safetyChecks = params.getOrElse("compiler.safetyChecks", true).asInstanceOf[Boolean]
  }

  object editor {
    val divId = id + "-editor-div"
    val textAreaId = divId + "-textArea"
    val consoleDivId = divId + "-console-div"
    val outputId = divId + "-output"
    val selectBotButtonId = consoleDivId + "-select-bot-button"

    val defaultPrograms = Map(
      0 -> "bank Main\ncreate 1,1,1\nmove\nmove\nturn 1\nmove\nmove\nmove\nturn 0",
      1 -> "bank Main\nmove",
      2 -> "",
      3 -> "")
  }

  object viz {

    val backgroundColor = params.getOrElse("viz.backgroundColor", "#fff").asInstanceOf[String]

    val boardWrapperDivId = id + "-board-wrapper"

    // TODO: shouldn't this (and more) go into object controller, outside of viz
    def consoleDivId = id + "-console"

    val cellSize = params.getOrElse("viz.cellSize", 32).asInstanceOf[Int]
    val framesPerSecond = params.getOrElse("viz.framesPerSecond", 30).asInstanceOf[Int]
    val cyclesPerSecond = params.getOrElse("viz.cyclesPerSecond", 250).asInstanceOf[Int]
    val maxCyclesPerTick = params.getOrElse("viz.maxCyclesPerTick", 200).asInstanceOf[Int]

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

    // TODO: update this to be the max of all max instruction durations
    val lookAheadCycles = sim.cycleCount.maxCreateDur
  }

}
