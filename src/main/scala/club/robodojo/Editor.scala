package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

import com.scalawarrior.scalajs.createjs

// TODO: develop sane random number access
import scala.util.Random


// TODO: Is controller really needed?
class Editor(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  val playerToColor = Map(
    0 -> "Blue",
    1 -> "Red",
    2 -> "Green",
    3 -> "Yellow")

  var files = HashMap(
    0 -> config.editor.defaultPrograms(0),
    1 -> config.editor.defaultPrograms(1),
    2 -> config.editor.defaultPrograms(2),
    3 -> config.editor.defaultPrograms(3))

  // TODO: should this really be a member variable?
  // programs(playerNum) == the result of compiling playerNums program
  var programs: HashMap[Int, Either[ArrayBuffer[ErrorMessage], Program]] = HashMap(
    0 -> Left(ArrayBuffer()),
    1 -> Left(ArrayBuffer()),
    2 -> Left(ArrayBuffer()),
    3 -> Left(ArrayBuffer()))

  // TODO: better name?
  var currentFileNum = 0

  // TODO: is this really needed?
  val file = files(currentFileNum)

  val cmEditor: org.denigma.codemirror.Editor = initEditor()

  /** End initialization **************************************************************************/

  // TODO: use the initEditor pattern elsewhere
  def initEditor(): org.denigma.codemirror.Editor = {

    val html = s"""
      <div class = "editor">

          <div class = "editor-console">

            <div class="dropdown select-bot-dropdown">
              <button id="${config.editor.selectBotButtonId}"
                      class="btn btn-default dropdown-toggle dark-border"
                      type="button"
                      data-toggle="dropdown">
              ${playerToColor(0)} bot
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

    val mode = "clike"
    val params: EditorConfiguration = EditorConfig.mode(mode).lineNumbers(true)

    dom.document.getElementById(config.editor.textAreaId) match {
      case el:HTMLTextAreaElement =>
        val cmEditor = CodeMirror.fromTextArea(el,params)
        cmEditor.getDoc().setValue(file)
        cmEditor
      case _=> throw new IllegalStateException("Could not find textarea for editor")
    }
  }

  def clickSelectBotDropdown(playerNum: Int): Unit = {
    if (playerNum < 0 || playerNum >= config.sim.maxPlayers) {
      throw new IllegalArgumentException("playerNum is invalid")
    }

    val newDropDownText = playerToColor(playerNum) + " bot <span class='caret'></span>"

    jQuery("#" + config.editor.selectBotButtonId)
      .html(newDropDownText)

    // Save the file
    files += currentFileNum -> cmEditor.getDoc().getValue()

    // Open the new file
    currentFileNum = playerNum
    cmEditor.getDoc().setValue(files(currentFileNum))

    jQuery("#" + config.editor.compilerOutputId).html("")

  }

  def addBot(board: Board, playerNum: Int): Unit = {
    val row = Random.nextInt(config.sim.numRows)
    val col = Random.nextInt(config.sim.numCols)
    // TODO: add direction and program
    val bot = Bot(board, PlayerColor.numToColor(playerNum), row, col)
    val dirNum = Random.nextInt(4)
    bot.direction = dirNum match {
      case 0 => Direction.Up
      case 1 => Direction.Down
      case 2 => Direction.Left
      case 3 => Direction.Right
      case _ => throw new IllegalStateException("This code shouldn't be reachable")
    }

    println(programs(playerNum))

    programs(playerNum) match {
      case Left(_) => throw new IllegalStateException("This code shouldn't be reachable")
      case Right(program) => bot.program = program
    }
    board.addBot(bot)
  }

  // TODO: rm x
  var x = 1

  // TODO: cleanup
  def clickCompile(): Unit = {

    val file: String = cmEditor.getDoc().getValue()

    // Compile each file
    0 until 4 foreach { playerNum =>
      programs += playerNum ->Compiler.compile(files(playerNum))
    }

    // TODO: factor out common code?
    programs(currentFileNum) match {
      case Left(errors) => displayErrors(errors)
      case Right(_) => displaySuccess()
    }

    // TODO: move this up
    val newBoard = new Board()

    0 until 4 foreach { playerNum =>
      programs(playerNum) match {
        case Left(_) => println("foo")
        case Right(_) => addBot(newBoard, playerNum)
      }
    }

    // TODO: rm this; used just for testing
    /*val density = 0.5

    val rand = new Random(x)

    x += 1

    0 until config.sim.numRows foreach { row =>
      0 until config.sim.numCols foreach { col =>
        if (rand.nextDouble < density) {
            val bot = Bot(newBoard, row, col)
            bot.direction = Direction.Right
            if (rand.nextDouble < 0.25)
              bot.direction = Direction.Left
            if (rand.nextDouble < 0.25)
              bot.direction = Direction.Up
            if (rand.nextDouble < 0.25)
              bot.direction = Direction.Down

            val bank0 = new Bank()

            if (rand.nextDouble < 0.5)
              bank0.instructions :+= MoveInstruction()
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= MoveInstruction()
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= TurnInstruction(0)
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= MoveInstruction()
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= MoveInstruction()
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= MoveInstruction()
            if (rand.nextDouble < 0.5)
              bank0.instructions :+= TurnInstruction(1)
            bot.program.banks += (0 -> bank0)
            newBoard.addBot(bot)
        }
      }
    }*/

    viz.newBoard(newBoard)
    controller.board = newBoard

    createjs.Ticker.paused = true
  }

  def displaySuccess(): Unit = {
    val html = s"""<p class="display-success">Your program successfully compiled.</p>"""

    jQuery("#" + config.editor.compilerOutputId).html(html)
  }

  def displayErrors(errors: ArrayBuffer[ErrorMessage]): Unit = {

    val header = if (errors.length == 1) {
        s"""<p class="display-failure">There is 1 error in your program.</p>"""
      } else {
        s"""<p class="display-failure">There are ${errors.length} errors in your program.</p>"""
      }

    val html = header +
        errors.map { error: ErrorMessage =>
          s"""<p><span class="as">Line ${error.lineNumber + 1}</span>: ${error.message}</p>"""
        }
        .mkString("\n")

    jQuery("#" + config.editor.compilerOutputId)
      .html(html)

  }
}
