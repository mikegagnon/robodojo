package robobot.webapp

import org.scalajs.jquery.jQuery

import com.scalawarrior.scalajs.createjs


class Controller(val config: Config, val board: Board, val viz: Viz) {

  addConsole()
  addPlayButton()

  def addConsole(): Unit = {
    val consoleDiv = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")

    jQuery("#" + config.id).append(consoleDiv)
  }

  def addPlayButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickPlay("${config.id}")'>
        <span class='glyphicon glyphicon-play'></span>
      </button>""")
  }

  def clickPlay(): Unit = {
    println("clickPlay inside " + config.id)

    // HACK: The proper thing to do here is add an event listener to Ticker by passing in a listener
    // as a function. However, that API call in scalajs isn't available in the current version of
    // scalajs-createjs. See: https://github.com/scalawarrior/scalajs-createjs/blob/2aeec181b8307f2687aff83c2311ed8589f140e3/src/main/scala/com/scalawarrior/scalajs/createjs/EaselJS.scala#L701
    // So, to get around this limitation, we pass the viz.stage object, which results in the ticker
    // calling viz.stage.handleEvent. So, we override handleEvent with our own method, which should
    // work just fine.
    viz.stage.handleEvent = tick _
    createjs.Ticker.addEventListener("tick", viz.stage);
  }

  // Bummer: 20FPS burns between 30% and 40% CPU on my machine
  def tick(): Unit = {
    println("Foo")

  }

}