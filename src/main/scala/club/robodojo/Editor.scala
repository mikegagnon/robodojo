package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

import scala.collection.immutable.HashMap

// TODO: does Editor really need viz?
class Editor(controller: Controller) {

  /** Begin initialization ************************************************************************/

  val config = controller.config

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
    jQuery("#" + config.id)
      .append(s"<div id='${config.editor.divId}'></div>")    
  }

  def addConsole(): Unit = {
    val html = s"<div id='${config.editor.consoleDivId}'></div>"
    jQuery("#" + config.editor.divId).append(html)

    // TODO: add class instead of manual cssing
    jQuery("#" + config.editor.consoleDivId)
      .css("border-top", "1px solid #444")
      // TODO: Same as game margin
      .css("margin-top", "5px")
      .css("padding-top", "5px")
      .css("padding-bottom", "5px")

    addSelectBotDropdown()

  }

  def addSelectBotDropdown(): Unit = {

    val html = s"""
      <div class="dropdown">
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
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

  def addCodeMirrorEditor(): org.denigma.codemirror.Editor = {

    val html = s"""
      <div>
        <div id='${config.editor.codemirrorDivId}'
             style="border: 1px solid #444; width: 50%; float: left">
          <textarea id='${config.editor.textAreaId}'></textarea>
        </div>
        <div id='${config.editor.compilerOutputId}'
             style="border: 1px solid #444; width: 50%; float: left">
            foo
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

    // Save the file
    files += currentFile -> cmEditor.getDoc().getValue()

    // Open the new file
    currentFile = playerNum
    cmEditor.getDoc().setValue(files(currentFile))
  }
}
