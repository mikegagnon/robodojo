package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror
import org.denigma.codemirror.{CodeMirror, EditorConfiguration, LineInfo}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HTMLTextAreaElement
import scala.scalajs.js

object CodeMirrorDojo {

  // Generates an instance of a CodeMirror Editor
  def getCmEditor(
      textAreaId: String,
      debugger: Option[Debugger]): codemirror.Editor = {

    val mode = "clike"

    // gutters is an array of CSS class names that are applied to the gutter of this editor
    val gutters = if (debugger.nonEmpty) {
        js.Array("CodeMirror-linenumbers", "breakpoints")
      } else {
        js.Array("CodeMirror-linenumbers")
      }

    val params: EditorConfiguration = EditorConfig
      .mode(mode)
      .lineNumbers(true)
      .readOnly(debugger.nonEmpty)
      .gutters(gutters)

    val editor = dom.document.getElementById(textAreaId) match {
      case el:HTMLTextAreaElement => CodeMirror.fromTextArea(el,params)
      case _=> throw new IllegalStateException("Could not find textarea for " + textAreaId)
    }

    if (debugger.nonEmpty) {
      // https://codemirror.net/demo/marker.html
      editor.on("gutterClick", (cm: codemirror.Editor, lineIndex: Int) => {
        val info: LineInfo = cm.lineInfo(lineIndex)
        val gutterMarkers: js.UndefOr[js.Array[String]] = info.gutterMarkers

        if (gutterMarkers.isEmpty || gutterMarkers == null) {
          debugger.get.addBreakpoint(lineIndex, cm)
        } else {
          debugger.get.removeBreakpoint(lineIndex, cm)
        }
        ()
      })
    }

    editor
  }

}