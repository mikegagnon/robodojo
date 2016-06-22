package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

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

  val cmEditor: org.denigma.codemirror.Editor = getCmEditor()

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    // TODO: change name of compilerOutputId to something else, since more than just the compiler
    // writes to it.
    // TODO: make compilerOutput scrollable. Make sure to put checks in so it doesn't get overly
    // flooded

    val html = s"""
      <div class = "editor">

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

          </div> <!-- end editor-console -->

          <div class="dark-border light-background">
            <div class="code-mirror-div">
              <textarea id='${config.editor.textAreaId}'></textarea>
            </div>
            <div id='${config.editor.compilerOutputId}' class="compiler-output">
            </div>
            <div style='clear: both;'></div>
          </div> <!-- end codemirror -->

      </div> <!-- end editor -->
      """

    jQuery("#" + config.id).append(html)
  }

  def getCmEditor(): org.denigma.codemirror.Editor = {
    val mode = "clike"
    val params: EditorConfiguration = EditorConfig.mode(mode).lineNumbers(true)

    dom.document.getElementById(config.editor.textAreaId) match {
      case el:HTMLTextAreaElement =>
        val cmEditor = CodeMirror.fromTextArea(el,params)
        cmEditor.getDoc().setValue(files(currentPlayerColor))
        cmEditor
      case _=> throw new IllegalStateException("Could not find textarea for editor")
    }
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

    jQuery("#" + config.editor.compilerOutputId).html("")

  }

  def addBot(board: Board, playerColor: PlayerColor.EnumVal): Unit = {
    val row = Random.nextInt(config.sim.numRows)
    val col = Random.nextInt(config.sim.numCols)
    val dirNum = Random.nextInt(4)
    val direction = dirNum match {
      case 0 => Direction.Up
      case 1 => Direction.Down
      case 2 => Direction.Left
      case 3 => Direction.Right
      case _ => throw new IllegalStateException("This code shouldn't be reachable")
    }

    val program = programs(playerColor) match {
      case Left(_) => throw new IllegalStateException("This code shouldn't be reachable")
      case Right(program) => program
    }

    val instructionSet = InstructionSet.Extended
    val mobile = true
    val active = true

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
    createjs.Ticker.paused = true
  }

  def displaySuccess(): Unit = {
    val html = s"""<p class="display-success">Your program successfully compiled.</p>"""

    jQuery("#" + config.editor.compilerOutputId).html(html)
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
          s"""<p><span class="line-error">Line ${error.lineNumber + 1}</span>. ${error.message}</p>"""
        }
        .mkString("\n")

    jQuery("#" + config.editor.compilerOutputId)
      .html(html)

  }
}
