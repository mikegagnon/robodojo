package robobot.webapp

import org.scalajs.jquery.jQuery

class Viz(val config: Config) {

  def init {
    jQuery("body").append(s"<div id='${config.mainDivId}'></div>")
  }


}