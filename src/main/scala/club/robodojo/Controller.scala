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

  def buttonHtml(
      buttonId: String,
      tooltip: String,
      functionName: String,
      glyph: String,
      spanId: String): String =

    s"""
      <button onmouseleave="$$('[data-toggle=\\'tooltip\\']').tooltip('hide')"
              id=${buttonId}
              data-toggle="tooltip"
              data-placement="bottom"
              title="${tooltip}"
              class="btn btn-default dark-border"
              onclick='club.robodojo.App().${functionName}("${config.id}")'>
        <span id=${spanId} class='glyphicon glyphicon-${glyph}'></span>
      </button>"""

  def addHtml(): Unit = {
    val html = s"""
      <div id='${config.viz.consoleDivId}'>
        <button type="button" class="btn btn-primary dark-border" onclick='window.open("doc/")'>Help</button>
        ${buttonHtml(config.viz.playPauseButtonId, "Run / pause game", "clickPlayPause", "play", config.viz.playPauseSpanId)}
        ${buttonHtml(config.viz.stepButtonId, "Step one cycle", "clickStep", "step-forward", config.viz.stepSpanId)}
        ${buttonHtml(config.viz.debugButtonId, "Open / close the debugger", "clickDebug", "eye-open", config.viz.debugSpanId)}
        ${buttonHtml(config.viz.editorButtonId, "Open / close the editor", "clickEditor", "pencil", config.viz.editorSpanId)}
        <span id="${config.viz.cycleCounterId}"></span>
        <span id="${config.viz.victorColorId}"></span>
      </div>

      <!-- activate tooltips -->
      <script>
      $$(function () {
        $$('[data-toggle="tooltip"]').tooltip()
      })
      </script>
      """

    jQuery("#" + config.viz.boardWrapperDivId).append(html)
  }

  def drawPause(): Unit = {
    jQuery("#" + config.viz.playPauseSpanId).attr("class", "glyphicon glyphicon-pause")
  }

  def drawPlay(): Unit = {
    jQuery("#" + config.viz.playPauseSpanId).attr("class", "glyphicon glyphicon-play")
  }

  def hideTooltip(): Unit = {
    js.Dynamic.global.`$`("[data-toggle='tooltip']").tooltip("hide")
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

    hideTooltip()
  }

  def clickStep(): Unit = {
    if (createjs.Ticker.paused) {
      viz.step = true
      createjs.Ticker.paused = false
    } else {
      clickPlayPause()
    }

    hideTooltip()
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

    hideTooltip()
  }

  def clickEditor(): Unit = {

    val handle = jQuery("#" + config.editor.divId)

    val style: String = handle.css("display")

    if (style == "block") {
      handle.css("display", "none")
    } else {
      handle.css("display", "block")
    }

    hideTooltip()
  }
}
