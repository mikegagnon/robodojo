package club.robodojo

import org.scalajs.jquery.jQuery

class Debugger(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val cmEditor = CodeMirrorDojo.getCmEditor(true, config.debugger.textAreaId)

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    val html = s"""
      <div class="window">
        <div class="dark-border light-background">
          <div class="code-mirror-div">
            <textarea id='${config.debugger.textAreaId}'></textarea>
          </div>
          <div id='${config.debugger.outputId}' class="compiler-output">
          </div>
          <div style='clear: both;'></div>
        </div> <!-- end codemirror -->
      </div>
      """

    jQuery("#" + config.id).append(html)
  }

  def onBotClick(botId: Long): Unit = {
    setupDebugger(botId: Long)
  }

  def getProgramText(botId: Long): String = {
    val bot: Bot = viz.board.getBot(botId).get
    return bot.toString
  }

  // TODO: pause
  def setupDebugger(botId: Long): Unit = {
    val programText = getProgramText(botId)
    cmEditor.getDoc().setValue(programText)
  }
}
