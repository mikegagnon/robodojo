package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

// TODO: does Editor really need viz?
class Editor(viz: Viz) {
  val id = "editor1"
  val code = "hello Scala!" //code to add
  val mode = "clike" //language mode, some modes have weird names in org.denigma.codemirror
  val params: EditorConfiguration = EditorConfig.mode(mode).lineNumbers(true) //config
  val editor = dom.document.getElementById(id) match {
    case el:HTMLTextAreaElement =>
      val m = CodeMirror.fromTextArea(el,params)
      m.getDoc().setValue("foo") //add the code
    case _=> dom.console.error("cannot find text area for the code!")
  }

  println("Foo")
}
