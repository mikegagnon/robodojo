package robobot.webapp

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs


class Controller(val config: Config, val board: Board, val viz: Viz) {

  addConsole()
  addPlayButton()
  addPauseButton()

  // HACK: clickStep
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

// HACK: clickStep
def addStepButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickStep("${config.id}")'>
        <span class='glyphicon glyphicon-step-forward'></span>
      </button>""")
  }

  // TODO: get rid of this hack
  var initialized = false

  // TODO: handle multiple clicks
  // TODO: handle resume from pause
  // TODO: pause all other instances of robobot when click play is clicked here
  def clickPlay(): Unit = {
    viz.step = false
    createjs.Ticker.paused = false
  }

  def clickPause(): Unit = {
    createjs.Ticker.paused = true
  }

  // HACK: clickStep
  def clickStep(): Unit = {

    if (!initialized) {
      clickPlay()
      createjs.Ticker.paused = true
    }

    if (createjs.Ticker.paused) {
      viz.step = true
      createjs.Ticker.paused = false
    }


  }

}