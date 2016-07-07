package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HTMLTextAreaElement
import scala.scalajs.js

object CodeMirrorDojo {

  // Generates an instance of a CodeMirror Editor
  def getCmEditor(
      readOnly: Boolean,
      breakpoints: Boolean,
      textAreaId: String): codemirror.Editor = {

    val mode = "clike"

    // gutters is an array of CSS class names that are applied to the gutter of this editor
    val gutters = if (breakpoints) {
        js.Array("CodeMirror-linenumbers", "breakpoints")
      } else {
        js.Array("CodeMirror-linenumbers")
      }

    val params: EditorConfiguration = EditorConfig
      .mode(mode)
      .lineNumbers(true)
      .readOnly(readOnly)
      .gutters(gutters)

    val editor = dom.document.getElementById(textAreaId) match {
      case el:HTMLTextAreaElement => CodeMirror.fromTextArea(el,params)
      case _=> throw new IllegalStateException("Could not find textarea for " + textAreaId)
    }

    if (breakpoints) {
      // https://codemirror.net/demo/marker.html
      editor.on("gutterClick", (cm: codemirror.Editor, lineIndex: Int) => {
        val info = cm.lineInfo(lineIndex)
        val gutterMarkers: js.UndefOr[js.Array[String]] = info.gutterMarkers
        cm.setGutterMarker(lineIndex, "breakpoints",
          if (gutterMarkers.isEmpty || gutterMarkers == null) makeMarker() else null)
        ()
      })
    }

    editor
  }

  def makeMarker(): HTMLElement = {
    var marker = dom.document.createElement("div").asInstanceOf[HTMLElement]
    marker.style.color = "#00F"
    marker.innerHTML = "●"
    return marker
  }
}