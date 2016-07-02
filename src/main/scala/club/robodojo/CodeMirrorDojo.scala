package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HTMLTextAreaElement
import scala.scalajs.js

object CodeMirrorDojo {

  def getCmEditor(readOnly: Boolean, textAreaId: String): codemirror.Editor = {
    val mode = "clike"
    val params: EditorConfiguration = EditorConfig
      .mode(mode)
      .lineNumbers(true)
      .readOnly(readOnly)
      .gutters(js.Array("CodeMirror-linenumbers", "breakpoints"))

    val editor = dom.document.getElementById(textAreaId) match {
      case el:HTMLTextAreaElement => CodeMirror.fromTextArea(el,params)
      case _=> throw new IllegalStateException("Could not find textarea for " + textAreaId)
    }

    // https://codemirror.net/demo/marker.html
    editor.on("gutterClick", (cm: codemirror.Editor, n: Int) => {
      val info = cm.lineInfo(n)
      val gutterMarkers: js.UndefOr[js.Array[String]] = info.gutterMarkers
      cm.setGutterMarker(n, "breakpoints", if (gutterMarkers.nonEmpty) null else makeMarker())
      ()
    })

    editor
  }

  def makeMarker(): HTMLElement = {
    var marker = dom.document.createElement("div").asInstanceOf[HTMLElement] 
    marker.style.color = "#822"
    marker.innerHTML = "●"
    return marker
  }
}