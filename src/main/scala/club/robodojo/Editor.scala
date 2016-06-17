package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

// TODO: does Editor really need viz?
class Editor(controller: Controller) {

  val config = controller.config

  // TODO: configify div.id
  jQuery("#" + config.id)
    .append(s"<div id='${config.editor.editorId}-div'></div>")

  jQuery("#" + config.editor.editorId + "-div")
    .append(s"<textarea id='${config.editor.editorId}'></textarea>")

  val code = "hello Scala!"
  val mode = "clike"
  val params: EditorConfiguration = EditorConfig.mode(mode)
    .lineNumbers(true)

  val editor = dom.document.getElementById(config.editor.editorId) match {
    case el:HTMLTextAreaElement =>
      val m = CodeMirror.fromTextArea(el,params)
      m.getDoc().setValue(code)
      m
    case _=> throw new IllegalStateException("TODO")
  }

  // TODO: configify
  jQuery("#" + config.editor.editorId + "-div")
    .css("border-top", "1px solid #444")
    .css("border-bottom", "1px solid #444")
    // TODO: Same as game margin
    .css("margin-top", "5px")
}
