package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

// TODO: use the initEditor pattern elsewhere
class Editor(controller: Controller)(implicit val config: Config) {

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

  var currentFileNum = 0
  val file = files(currentFileNum)
  
  val cmEditor: org.denigma.codemirror.Editor = initEditor()
 
  /** End initialization **************************************************************************/

  // TODO: do we neally need all these ID's?
  def initEditor(): org.denigma.codemirror.Editor = {

    val html = s"""
      <div class = "editor" id = '${config.editor.divId}'>

          <div class = "editor-console" id = '${config.editor.consoleDivId}'>

            <div class="dropdown select-bot-dropdown">
              <button id="${config.editor.selectBotButtonId}"
                      class="btn btn-default dropdown-toggle dark-border"
                      type="button"
                      data-toggle="dropdown">
              Select bot to edit
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
            <div id='${config.editor.codemirrorDivId}' class="code-mirror-div">
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
      case _=> throw new IllegalStateException("TODO")
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
  }

  def clickCompile(): Unit = {

    val file: String = cmEditor.getDoc().getValue()

    Compiler.compile(file) match {
      case Left(errors) => displayErrors(errors)
      case Right(program) => displaySuccess()
    }
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
