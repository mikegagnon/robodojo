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
    val consoleDiv = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")

    jQuery("#" + config.id).append(consoleDiv)
  }

  // TODO: factor out common html
  def addPlayButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickPlay("${config.id}")'>
        <span class='glyphicon glyphicon-play'></span>
      </button>""")
  }

  def addPauseButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickPause("${config.id}")'>
        <span class='glyphicon glyphicon-pause'></span>
      </button>""")
  }

  def addStepButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickStep("${config.id}")'>
        <span class='glyphicon glyphicon-step-forward'></span>
      </button>""")
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
    }

}