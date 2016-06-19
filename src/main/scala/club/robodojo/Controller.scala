package club.robodojo

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs

// TODO: is board really needed?
class Controller(var board: Board, val viz: Viz)(implicit val config: Config) {

  addConsole()
  addPlayButton()
  addPauseButton()
  addStepButton()

  val editor = new Editor(this, viz)


  def addConsole(): Unit = {
    val html = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")
    jQuery("#" + config.viz.boardWrapperDivId).append(html)
  }

  def buttonHtml(functionName: String, glyph: String): String = 
    s"""
      <button class="btn btn-default dark-border" onclick='club.robodojo.App().${functionName}("${config.id}")'>
        <span class='glyphicon glyphicon-${glyph}'></span>
      </button>"""

  def addPlayButton(): Unit = {
    val html = buttonHtml("clickPlay", "play")
    jQuery("#" + config.viz.consoleDivId).append(html)
  }

  def addPauseButton(): Unit = {
    val html = buttonHtml("clickPause", "pause")
    jQuery("#" + config.viz.consoleDivId).append(html)
  }

  def addStepButton(): Unit = {
    val html = buttonHtml("clickStep", "step-forward")
    jQuery("#" + config.viz.consoleDivId).append(html)
  }

  def clickPlay(): Unit = {
    viz.step = false
    createjs.Ticker.paused = false
  }

  def clickPause(): Unit = {
    createjs.Ticker.paused = true
  }

  def clickStep(): Unit =
    if (createjs.Ticker.paused) {
      viz.step = true
      createjs.Ticker.paused = false
    } else {
      clickPause()
    }

}