package club.robodojo

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs

class Controller(val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val debugger = new Debugger(this, viz)

  val editor = new Editor(this, viz)

  /** End initialization **************************************************************************/

  def buttonHtml(functionName: String, glyph: String, spanId: String): String =
    s"""
      <button class="btn btn-default dark-border" onclick='club.robodojo.App().${functionName}("${config.id}")'>
        <span id=${spanId} class='glyphicon glyphicon-${glyph}'></span>
      </button>"""

  def addHtml(): Unit = {
    val html = s"""
      <div id='${config.viz.consoleDivId}'>
        ${buttonHtml("clickPlayPause", "play", config.viz.playPauseSpanId)}
        ${buttonHtml("clickStep", "step-forward", config.viz.stepSpanId)}
        ${buttonHtml("clickDebug", "eye-open", config.viz.debugSpanId)}
        ${buttonHtml("clickEditor", "pencil", config.viz.editorSpanId)}
      </div>
      """
    jQuery("#" + config.viz.boardWrapperDivId).append(html)
  }

  def drawPause(): Unit = {
    jQuery("#" + config.viz.playPauseSpanId).attr("class", "glyphicon glyphicon-pause")
  }

  def drawPlay(): Unit = {
    jQuery("#" + config.viz.playPauseSpanId).attr("class", "glyphicon glyphicon-play")
  }

  def clickPlayPause(): Unit = {
    viz.step = false
    if (createjs.Ticker.paused) {
      createjs.Ticker.paused = false
      viz.botImages.foreach { case (_, botImage) =>
        botImage.mouseEnabled = false;
      }
      drawPause()
    } else {
      createjs.Ticker.paused = true
      viz.botImages.foreach { case (i, botImage) =>
        botImage.mouseEnabled = true;
      }
      drawPlay()
    }
  }

  def clickStep(): Unit =
    if (createjs.Ticker.paused) {
      viz.step = true
      createjs.Ticker.paused = false
    } else {
      clickPlayPause()
    }

  def clickDebug(): Unit = {

    val handle = jQuery("#" + config.debugger.divId)

    val style: String = handle.css("display")

    if (style == "block") {
      handle.css("display", "none")
    } else {
      handle.css("display", "block")

      debugger.botIdDebugged.foreach { botId: Long =>
        debugger.setupDebugger(botId)
      }
    }
  }

  def clickEditor(): Unit = {

    val handle = jQuery("#" + config.editor.divId)

    val style: String = handle.css("display")

    if (style == "block") {
      handle.css("display", "none")
    } else {
      handle.css("display", "block")
    }
  }
}
