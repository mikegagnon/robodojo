package robobot.webapp

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs


class Controller(val config: Config, val board: Board, val viz: Viz) {

  addConsole()
  addPlayButton()
  addPauseButton()
  addStepButton()

  def addConsole(): Unit = {
    val html = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")
    jQuery("#" + config.id).append(html)
  }

  def buttonHtml(functionName: String, glyph: String): String = 
    s"""
      <button onclick='robobot.webapp.RobobotApp().${functionName}("${config.id}")'>
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