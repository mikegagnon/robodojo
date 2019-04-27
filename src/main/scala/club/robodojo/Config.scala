package club.robodojo

import scala.collection.mutable
import java.net.URLDecoder
import scala.scalajs.js


object Config {
  val default = new Config(Map[String,Any]())
}

case class ProgramRef(headerName: String, programName: String)

case class BotPositionConfig(row: Int, col: Int)
case class BotConfig(rank: String, name: String, position: Option[BotPositionConfig])
case class CompetitionConfig(bot1: BotConfig, bot2: BotConfig)

// TODO: change defs to vals?
// TODO: remove all SVG config vals
class Config(params: Map[String, Any] = Map[String, Any]()) {

  val competition: Option[CompetitionConfig] = if (params.contains("competition")) {
    val competitionMap = params("competition").asInstanceOf[js.Dictionary[Any]].toMap
    val bot1Map = competitionMap("bot1").asInstanceOf[js.Dictionary[Any]].toMap
    val bot1 = BotConfig(bot1Map("rank").asInstanceOf[String], bot1Map("name").asInstanceOf[String], Some(BotPositionConfig(bot1Map("row").asInstanceOf[Int], bot1Map("col").asInstanceOf[Int])))
    val bot2Map = competitionMap("bot2").asInstanceOf[js.Dictionary[Any]].toMap
    val bot2 = BotConfig(bot2Map("rank").asInstanceOf[String], bot2Map("name").asInstanceOf[String], Some(BotPositionConfig(bot2Map("row").asInstanceOf[Int], bot2Map("col").asInstanceOf[Int])))
    Some(CompetitionConfig(bot1, bot2))
  } else {
    None
  }

  val id: String = params.getOrElse("id", "robodojo").asInstanceOf[String]

  val defaultLocation = "foo?bh=Rank%203&bp=The%20Overkiller&rh=Rank%205&rp=Seuche3"

  // From dom.window.location
  val location: String = params.getOrElse("location", defaultLocation).asInstanceOf[String]

  val urlParams: Map[String, String] =
    try {

      var parts = location.split("\\?")

      if (parts.length != 2) {
        parts = defaultLocation.split("\\?")
      }

      parts(1)
        .split("&")
        .map { pair: String =>
          val keyVal = pair.split("=")
          if (keyVal.length != 2) {
            throw new IllegalArgumentException()
          } else {
            (URLDecoder.decode(keyVal(0), "UTF-8"), URLDecoder.decode(keyVal(1), "UTF-8"))
          }
        }
        .toMap

    } catch {
      case e: IllegalArgumentException => Map[String, String]()
    }

  val programRefBlue: Option[ProgramRef] =
    if (urlParams.contains("bh") && urlParams.contains("bp")) {
      Some(ProgramRef(urlParams("bh"), urlParams("bp")))
    } else {
      None
    }

  val programRefRed: Option[ProgramRef] =
    if (urlParams.contains("rh") && urlParams.contains("rp")) {
      Some(ProgramRef(urlParams("rh"), urlParams("rp")))
    } else {
      None
    }

  // simulation constants
  object sim {
    val maxNumVariables = params.getOrElse("sim.maxNumVariables", 20).asInstanceOf[Int]

    val numRows: Int = params.getOrElse("sim.numRows", 100).asInstanceOf[Int]
    val numCols: Int = params.getOrElse("sim.numCols", 100).asInstanceOf[Int]

    object board {

      val blueRow: Int = params.getOrElse("sim.board.blueRow", -1).asInstanceOf[Int]
      val blueCol: Int = params.getOrElse("sim.board.blueCol", -1).asInstanceOf[Int]
      val blueDir: String = params.getOrElse("sim.board.blueDir", "NoDir").asInstanceOf[String]

      val redRow: Int = params.getOrElse("sim.board.redRow", -1).asInstanceOf[Int]
      val redCol: Int = params.getOrElse("sim.board.redCol", -1).asInstanceOf[Int]
      val redDir: String = params.getOrElse("sim.board.redDir", "NoDir").asInstanceOf[String]

      val greenRow: Int = params.getOrElse("sim.board.greenRow", -1).asInstanceOf[Int]
      val greenCol: Int = params.getOrElse("sim.board.greemCol", -1).asInstanceOf[Int]
      val greenDir: String = params.getOrElse("sim.board.greemDir", "NoDir").asInstanceOf[String]

      val yellowRow: Int = params.getOrElse("sim.board.yellowRow", -1).asInstanceOf[Int]
      val yellowCol: Int = params.getOrElse("sim.board.yellowCol", -1).asInstanceOf[Int]
      val yellowDir: String = params.getOrElse("sim.board.yellowDir", "NoDir").asInstanceOf[String]

    }

    // Update according to http://robocom.rrobek.de/help/instr1.html
    object cycleCount {

      val durMove = params.getOrElse("sim.cycleCount.durMove", 18).asInstanceOf[Int]
      val durTurn = params.getOrElse("sim.cycleCount.durTurn", 5).asInstanceOf[Int]
      val durSet = params.getOrElse("sim.cycleCount.durSet", 5).asInstanceOf[Int]
      val durJump = params.getOrElse("sim.cycleCount.durJump", 2).asInstanceOf[Int]
      val durBJump = params.getOrElse("sim.cycleCount.durBJump", 5).asInstanceOf[Int]
      val durCrash = params.getOrElse("sim.cycleCount.durCrash", 1).asInstanceOf[Int]
      val durScan = params.getOrElse("sim.cycleCount.durScan", 6).asInstanceOf[Int]
      val durComp = params.getOrElse("sim.cycleCount.durComp", 3).asInstanceOf[Int]
      val durAdd = params.getOrElse("sim.cycleCount.durAdd", 6).asInstanceOf[Int]
      val durSub = params.getOrElse("sim.cycleCount.durSub", 6).asInstanceOf[Int]

      val durRemoteAccessCost = params.getOrElse("sim.durRemoteAccessCost", 8).asInstanceOf[Int]

      // For the create instruction
      val durCreate1 = params.getOrElse("sim.cycleCount.durCreate1", 50).asInstanceOf[Int]
      val durCreate2 = params.getOrElse("sim.cycleCount.durCreate2", 20).asInstanceOf[Int]
      val durCreate3 = params.getOrElse("sim.cycleCount.durCreate3", 2).asInstanceOf[Int]
      val durCreate4 = params.getOrElse("sim.cycleCount.durCreate4", 0).asInstanceOf[Int]
      val durCreate5 = params.getOrElse("sim.cycleCount.durCreate5", 40).asInstanceOf[Int]
      val durCreate6 = params.getOrElse("sim.cycleCount.durCreate6", 80).asInstanceOf[Int]
      val maxCreateDur = params.getOrElse("sim.cycleCount.maxCreateDur", 1500).asInstanceOf[Int]

      val durTrans1 = params.getOrElse("sim.cycleCount.durTrans1", 5).asInstanceOf[Int]
      val durTrans2 = params.getOrElse("sim.cycleCount.durTrans2", 3).asInstanceOf[Int]
    }

    val maxBanks = params.getOrElse("sim.maxBanks", 50).asInstanceOf[Int]

    val maxPlayers = params.getOrElse("sim.maxPlayers", 4).asInstanceOf[Int]
  }

  object compiler {
    val maxLineLength = params.getOrElse("compiler.maxLineLength", 1000).asInstanceOf[Int]
    val maxBankInstructions = params.getOrElse("compiler.maxBankInstructions", 1000).asInstanceOf[Int]
    val safetyChecks = params.getOrElse("compiler.safetyChecks", true).asInstanceOf[Boolean]
  }

  object debugger {
    val divId = id + "-debugger-div"
    val textAreaId = divId + "-textArea"
    val outputId = divId + "-output"
    val fontSize = params.getOrElse("debugger.fontSize", "11px").asInstanceOf[String]
    val highlightBorderColor = "black"
    val highlightColor = "yellow"
  }

  object editor {
    val divId = id + "-editor-div"
    val textAreaId = divId + "-textArea"
    val consoleDivId = divId + "-console-div"
    val outputId = divId + "-output"
    val selectBotModal = divId + "-selectBotModal"
    val closeModalButton = divId + "-closeModalButton"
    val modalBodyId = divId + "-modalBody"
    val selectBotButtonId = consoleDivId + "-select-bot-button"
    val fontSize = params.getOrElse("editor.fontSize", "11px").asInstanceOf[String]

    val defaultPrograms = Map(
      0 -> "bank Main\nmove",
      1 -> "",
      2 -> "",
      3 -> "")

    // preloadedPrograms(headerName)(programName) == programBody
    val preloadedPrograms: mutable.LinkedHashMap[String, mutable.LinkedHashMap[String, String]] = {
        
        // EXAMPLE INPUT:
        /*
        "sim.editor.preload.header.0.name": "Header 0",
        "sim.editor.preload.header.1.name": "Header 1",
        "sim.editor.preload.header.2.name": "Header 2",

        "sim.editor.preload.header.0.program.0.name" : "Program 0 name",
        "sim.editor.preload.header.0.program.0.body" : "Program 0 body",

        "sim.editor.preload.header.0.program.1.name" : "Program 1 name",
        "sim.editor.preload.header.0.program.1.body" : "Program 1 body",
        
        "sim.editor.preload.header.1.program.0.name" : "Program 2 name",
        "sim.editor.preload.header.1.program.0.body" : "Program 2 body",
        */

        var preload = mutable.LinkedHashMap[String, mutable.LinkedHashMap[String, String]]()

        var headerIndex = 0
        var headerName: Option[String] =
          params.get(s"sim.editor.preload.header.${headerIndex}.name").asInstanceOf[Option[String]]

        while (headerName.nonEmpty) {

          preload += (headerName.get -> mutable.LinkedHashMap[String, String]())

          var programIndex = 0
          var programPrefix = s"sim.editor.preload.header.${headerIndex}.program.${programIndex}"
          var programName: Option[String] =
            params.get(programPrefix + ".name").asInstanceOf[Option[String]]
          var programBody: Option[String] =
            params.get(programPrefix + ".body").asInstanceOf[Option[String]]

          while (programName.nonEmpty && programBody.nonEmpty) {

            preload(headerName.get) += (programName.get -> programBody.get)

            programIndex += 1
            programPrefix = s"sim.editor.preload.header.${headerIndex}.program.${programIndex}"
            programName = params.get(programPrefix + ".name").asInstanceOf[Option[String]]
            programBody = params.get(programPrefix + ".body").asInstanceOf[Option[String]]
          }
          
          headerIndex += 1
          headerName =
            params.get(s"sim.editor.preload.header.${headerIndex}.name").asInstanceOf[Option[String]]
        }

        preload
    }
  }

  object viz {

    val backgroundColor = params.getOrElse("viz.backgroundColor", "#fff").asInstanceOf[String]

    val boardWrapperDivId = id + "-board-wrapper"

    // TODO: shouldn't this (and more) go into object controller, outside of viz
    def consoleDivId = id + "-console"

    def playPauseButtonId = consoleDivId + "-playPauseButton"
    def playPauseSpanId = consoleDivId + "-playPause"

    def stepButtonId = consoleDivId + "stepButton"
    def stepSpanId = consoleDivId + "-step"

    def debugButtonId = consoleDivId + "-debugButton"
    def debugSpanId = consoleDivId + "-debug"

    def editorButtonId = consoleDivId + "-editorButton"
    def editorSpanId = consoleDivId + "-editor"

    def speedId = consoleDivId + "-speed"

    def cycleCounterId = consoleDivId + "-cycleCounter"

    def victorColorId = consoleDivId + "-victorColor"

    val cellSize = params.getOrElse("viz.cellSize", 32).asInstanceOf[Int]
    val framesPerSecond = params.getOrElse("viz.framesPerSecond", 15).asInstanceOf[Int]
    val cyclesPerSecond = params.getOrElse("viz.cyclesPerSecond", 5000).asInstanceOf[Int]
    val maxCyclesPerTick = params.getOrElse("viz.maxCyclesPerTick", 5000).asInstanceOf[Int]
    val maxCyclesPerSecond = params.getOrElse("viz.maxCyclesPerSecond", 5000).asInstanceOf[Int]

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

  }

}
