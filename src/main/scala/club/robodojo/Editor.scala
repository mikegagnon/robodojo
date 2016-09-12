package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery
import scala.collection.mutable


import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

import com.scalawarrior.scalajs.createjs

import scala.util.Random

class Editor(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  // files(playerColor) == the string representation of playerColor's program
  var files: HashMap[PlayerColor.EnumVal, String] = HashMap(
    PlayerColor.Blue -> config.editor.defaultPrograms(0),
    PlayerColor.Red -> config.editor.defaultPrograms(1),
    PlayerColor.Green -> config.editor.defaultPrograms(2),
    PlayerColor.Yellow -> config.editor.defaultPrograms(3))

  // programs(playerColor) == the result of compiling playerColor's program
  var programs: HashMap[PlayerColor.EnumVal, Either[ArrayBuffer[ErrorMessage], Program]] = HashMap(
    PlayerColor.Blue -> Left(ArrayBuffer()),
    PlayerColor.Red -> Left(ArrayBuffer()),
    PlayerColor.Green -> Left(ArrayBuffer()),
    PlayerColor.Yellow -> Left(ArrayBuffer()))

  // Only one program-string can be viewed at a time. currentPlayerColor is the color of the
  // program currently being viewed.
  var currentPlayerColor: PlayerColor.EnumVal = PlayerColor.Blue

  addHtml()

  val cmEditor = CodeMirrorDojo.getCmEditor(config.editor.textAreaId, None)

  jQuery(s"#${config.editor.divId} .CodeMirror").css("font-size", config.editor.fontSize)

  cmEditor.getDoc().setValue(files(currentPlayerColor))

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    val programSelection =
      config
        .editor
        .preloadedPrograms
        .flatMap { case (headerName: String, program: mutable.LinkedHashMap[String, String]) =>

          s"<p><b>${headerName}</b></p>" ::
          program.map { case (programName: String, programBody: String) =>
            s"<button type='button' class='btn btn-default modal-body-button'>${programName}</button>"
          }.toList

        }
        .mkString(" ")

    // TODO: make outputId scrollable. Make sure to put checks in so it doesn't get overly
    // flooded.
    // TODO: put output in popup.
    val html = s"""
      <div class = "window" id="${config.editor.divId}">
        <span class="window-name">Editor</span>

          <div class = "editor-console">

            <div class="dropdown select-bot-dropdown">
              <button id="${config.editor.selectBotButtonId}"
                      class="btn btn-default dropdown-toggle dark-border"
                      type="button"
                      data-toggle="dropdown">
              ${PlayerColor.Blue} bot
              <span class="caret"></span></button>
              <ul class="dropdown-menu">
                <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(0, '${config.id}')">Blue bot</a></li>
                <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(1, '${config.id}')">Red bot</a></li>
                <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(2, '${config.id}')">Green bot</a></li>
                <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(3, '${config.id}')">Yellow bot</a></li>
              </ul>
            </div> <!-- end select-bot-dropdown-->

            <button type="button"
                    class="btn btn-default dark-border"
                    onclick='club.robodojo.App().clickCompile("${config.id}")'>
              Compile
            </button>

            <button type="button"
                    class="btn btn-default dark-border"
                    data-toggle="modal"
                    data-target="#selectBotModal">
              Select program
            </button>

            <!-- Select-bot Modal -->
            <div class="modal fade" id="selectBotModal" tabindex="-1" role="dialog" aria-labelledby="selectBotModalLabel">
              <div class="modal-dialog" role="document">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="selectBotModalLabel">Select program</h4>
                  </div>
                  <div class="modal-body" id="${config.editor.modalBodyId}">
                    ${programSelection}
                  </div>
                  <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                  </div>
                </div>
              </div>
            </div>





          </div> <!-- end editor-console -->

          <div class="dark-border light-background">
            <div class="code-mirror-div">
              <textarea id='${config.editor.textAreaId}'></textarea>
            </div>
            <div id='${config.editor.outputId}' class="compiler-output">
            </div>
            <div style='clear: both;'></div>
          </div> <!-- end codemirror -->

      </div> <!-- end editor -->
      """

    jQuery("#" + config.id).append(html)
  }

  // playerColor is the color that has been selected from the dropdown
  def clickSelectBotDropdown(playerColor: PlayerColor.EnumVal): Unit = {

    val newDropDownText = playerColor.toString + " bot <span class='caret'></span>"

    jQuery("#" + config.editor.selectBotButtonId)
      .html(newDropDownText)

    // Save the file
    files += currentPlayerColor -> cmEditor.getDoc().getValue()

    // Open the new file
    currentPlayerColor = playerColor
    cmEditor.getDoc().setValue(files(currentPlayerColor))

    jQuery("#" + config.editor.outputId).html("")

  }

  def addBot(board: Board, playerColor: PlayerColor.EnumVal): Unit = {

    val defaultRow =
      playerColor match {
        case PlayerColor.Blue => config.sim.board.blueRow
        case PlayerColor.Red => config.sim.board.redRow
        case PlayerColor.Green => config.sim.board.greenRow
        case PlayerColor.Yellow => config.sim.board.yellowRow
      }

  val defaultCol =
      playerColor match {
        case PlayerColor.Blue => config.sim.board.blueCol
        case PlayerColor.Red => config.sim.board.redCol
        case PlayerColor.Green => config.sim.board.greenCol
        case PlayerColor.Yellow => config.sim.board.yellowCol
      }

    // Cycle through row, col until you find an empty spot
    var row = if (defaultRow >= 0) defaultRow else Random.nextInt(config.sim.numRows)
    var col = if (defaultCol >= 0) defaultCol else Random.nextInt(config.sim.numCols)

    while (board.matrix(row)(col).nonEmpty) {
      row = Random.nextInt(config.sim.numRows)
      col = Random.nextInt(config.sim.numCols)
    }

    val defaultDir =
      playerColor match {
        case PlayerColor.Blue => Direction.fromString(config.sim.board.blueDir)
        case PlayerColor.Red => Direction.fromString(config.sim.board.redDir)
        case PlayerColor.Green => Direction.fromString(config.sim.board.greenDir)
        case PlayerColor.Yellow => Direction.fromString(config.sim.board.yellowDir)
      }

    val direction =
      defaultDir match {
        case Direction.NoDir => {
          Random.nextInt(4) match {
            case 0 => Direction.Up
            case 1 => Direction.Down
            case 2 => Direction.Left
            case 3 => Direction.Right
            case _ => throw new IllegalStateException("This code shouldn't be reachable")
          }
        }
        case dir: Direction.EnumVal => dir
      }

    val program = programs(playerColor) match {
      case Left(_) => throw new IllegalStateException("This code shouldn't be reachable")
      case Right(program) => program
    }

    val instructionSet = InstructionSet.Super
    val mobile = true
    val active: Short = 1

    val bot = Bot(board, playerColor, row, col, direction, program, instructionSet, mobile, active)

    board.addBot(bot)
  }

  def clickCompile(): Unit = {

    // Save the current program-string
    files += currentPlayerColor -> cmEditor.getDoc().getValue()

    // Compile each file
    PlayerColor.colors.foreach { playerColor =>
      programs += playerColor -> Compiler.compile(files(playerColor), playerColor)
    }

    val newBoard = new Board()

    // Display result of compilation. If the compilation succeeded
    programs(currentPlayerColor) match {
      case Left(errors) => displayErrors(errors)
      case Right(_) => displaySuccess()
    }

    // For playerColor's whose program succeeded in compilation: add a bot to the board
    PlayerColor.colors.foreach { playerColor =>
      programs(playerColor) match {
        case Left(_) => ()
        case Right(_) => addBot(newBoard, playerColor)
      }
    }

    viz.newBoard(newBoard)

    controller.drawPlay()

    controller.debugger.reset()
    createjs.Ticker.paused = true
  }

  def displaySuccess(): Unit = {
    val html = s"""<p class="display-success">Your program successfully compiled.</p>"""

    jQuery("#" + config.editor.outputId).html(html)
  }

  // TODO: display errors in a popup?
  def displayErrors(errors: ArrayBuffer[ErrorMessage]): Unit = {

    val header = if (errors.length == 1) {
        s"""<p class="display-failure">There is 1 error in your program.</p>"""
      } else {
        s"""<p class="display-failure">There are ${errors.length} errors in your program.</p>"""
      }

    val html = header +
        errors.map { error: ErrorMessage =>
          s"""<p><span class="line-error">Line ${error.lineIndex + 1}</span>. ${error.message}</p>"""
        }
        .mkString("\n")

    jQuery("#" + config.editor.outputId)
      .html(html)

  }
}
