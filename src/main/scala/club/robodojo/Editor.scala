package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

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

  var currentFile = 0
  val file = files(currentFile)

  addPrimaryDiv()

  addConsole()
  
  val cmEditor = addCodeMirrorEditor()
 
  /** End initialization **************************************************************************/

  def addPrimaryDiv(): Unit = {
    val html = s"""<div
                    class = "editor"
                    id = '${config.editor.divId}'></div>"""
    jQuery("#" + config.id)
      .append(html)
  }

  def addConsole(): Unit = {
    val html = s"""<div
                    class = "editor-console"
                    id = '${config.editor.consoleDivId}'></div>"""
    jQuery("#" + config.editor.divId).append(html)

    addSelectBotDropdown()
    addCompileButton()
  }

  def addSelectBotDropdown(): Unit = {

    val html = s"""
      <div class="dropdown" style="float: left; margin-right: 5px">
        <button style="border-color: #444" id="${config.editor.selectBotButtonId}" class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
        Select bot to edit
        <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(0, '${config.id}')">Blue bot</a></li>
          <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(1, '${config.id}')">Red bot</a></li>
          <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(2, '${config.id}')">Green bot</a></li>
          <li><a href="javascript:club.robodojo.App().clickSelectBotDropdown(3, '${config.id}')">Yellow bot</a></li>
        </ul>
      </div>"""

      jQuery("#" + config.editor.consoleDivId)
        .append(html)
  }

  def addCompileButton(): Unit = {
    val html = s"""
      <button type="button"
              class="btn btn-default"
              style="border-color: #444"
              onclick='club.robodojo.App().clickCompile("${config.id}")'>
        Compile
      </button>"""
    jQuery("#" + config.editor.consoleDivId)
      .append(html)
  }

  def addCodeMirrorEditor(): org.denigma.codemirror.Editor = {

    val html = s"""
      <div style="border: 1px solid #444;  background: #fff;">
        <div id='${config.editor.codemirrorDivId}'
             style="border-right: 1px solid #ddd; width: 50%; float: left">
          <textarea id='${config.editor.textAreaId}'></textarea>
        </div>
        <div id='${config.editor.compilerOutputId}'
             style="padding: 5px; width: 50%; float: left;">
        </div>
        <div style='clear: both;'></div>
      </div>
      """

    jQuery("#" + config.editor.divId)
      .append(html)

    val mode = "clike"
    val params: EditorConfiguration = EditorConfig.mode(mode)
      .lineNumbers(true)

    val cmEditor =dom.document.getElementById(config.editor.textAreaId) match {
      case el:HTMLTextAreaElement =>
        val m = CodeMirror.fromTextArea(el,params)
        m.getDoc().setValue(file)
        m
      case _=> throw new IllegalStateException("TODO")
    }

    return cmEditor
  }

  def clickSelectBotDropdown(playerNum: Int): Unit = {
    if (playerNum < 0 || playerNum >= config.sim.maxPlayers) {
      throw new IllegalArgumentException("playerNum is invalid")
    }

    val newText = playerToColor(playerNum) + " bot <span class='caret'></span>"

    jQuery("#" + config.editor.selectBotButtonId)
      .html(newText)

    // Save the file
    files += currentFile -> cmEditor.getDoc().getValue()

    // Open the new file
    currentFile = playerNum
    cmEditor.getDoc().setValue(files(currentFile))
  }

  def clickCompile(): Unit = {

    val file: String = cmEditor.getDoc().getValue()

    Compiler.compile(file) match {
      case Left(errors) => displayErrors(errors)
      case Right(program) => displaySuccess()
    }
  }

  def displaySuccess(): Unit = {
    val html = s"<p><b style='color: green'>Your program successfully compiled.</b></p>"

    jQuery("#" + config.editor.compilerOutputId)
      .html(html)
  }

  def displayErrors(errors: ArrayBuffer[ErrorMessage]): Unit = {

    val header = if (errors.length == 1) {
        s"<p><b style='color: red'>There is 1 error in your program.</b></p>"
      } else {
        s"<p><b style='color: red'>There are ${errors.length} errors in your program.</b></p>"
      }

    val html = header +
        errors.map { error: ErrorMessage =>
          s"<p><b>Line ${error.lineNumber + 1}</b>: ${error.message}</p>"
        }
        .mkString("\n")

    jQuery("#" + config.editor.compilerOutputId)
      .html(html)

  }
}
