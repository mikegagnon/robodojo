package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

object CodeMirrorDojo {

  def getCmEditor(readOnly: Boolean, textAreaId: String): org.denigma.codemirror.Editor = {
    val mode = "clike"
    val params: EditorConfiguration = EditorConfig.mode(mode).lineNumbers(true).readOnly(readOnly)

    dom.document.getElementById(textAreaId) match {
      case el:HTMLTextAreaElement =>
        val cmEditor = CodeMirror.fromTextArea(el,params)
        cmEditor
      case _=> throw new IllegalStateException("Could not find textarea for " + textAreaId)
    }
  }
}