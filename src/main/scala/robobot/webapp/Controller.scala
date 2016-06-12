package robobot.webapp

import org.scalajs.jquery.jQuery


class Controller(val config: Config, val board: Board, val viz: Viz) {

  addConsole()
  addPlayButton()

  def addConsole() {
    val consoleDiv = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")

    jQuery("#" + config.id).append(consoleDiv)
  }

  def addPlayButton() {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickPlay("${config.id}")'>
        <span class='glyphicon glyphicon-play'></span>
      </button>""")
  }

  def clickPlay {
    println("clickPlay inside " + config.id)
  }

}