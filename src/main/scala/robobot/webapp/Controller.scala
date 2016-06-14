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
    println("clickPlay inside " + config.id)

    // HACK: clickStep
    if (initialized) {
      createjs.Ticker.paused = false
      viz.step = false
    } else {

      initialized = true

      createjs.Ticker.paused = false

      // TODO: put this code in RobobotApp, so that all Robobot instances share the same Ticker
      // HACK: The proper thing to do here is add an event listener to Ticker by passing in a listener
      // as a function. However, that API call in scalajs isn't available in the current version of
      // scalajs-createjs. See: https://github.com/scalawarrior/scalajs-createjs/blob/2aeec181b8307f2687aff83c2311ed8589f140e3/src/main/scala/com/scalawarrior/scalajs/createjs/EaselJS.scala#L701
      // So, to get around this limitation, we pass the viz.stage object, which results in the ticker
      // calling viz.stage.handleEvent. So, we override handleEvent with our own method, which should
      // work just fine.
      viz.stage.handleEvent = viz.tick _
      createjs.Ticker.addEventListener("tick", viz.stage)
      createjs.Ticker.setFPS(config.viz.framesPerSecond)
    }
  }

  def clickPause(): Unit = {
    println("clickPause inside " + config.id)
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