package robobot.webapp

import org.scalajs.jquery.jQuery

class Viz(val config: Config) {

  jQuery("body").append(s"<div id='${config.mainDivId}'></div>")

  jQuery("#" + config.mainDivId).append(s"<svg id='${config.svgId}'></svg>")

  val svg = jQuery("#" + config.svgId)

  svg.attr("xmlns", "'http://www.w3.lrg/2000/svg'")
  svg.attr("width", config.svgWidth)
  svg.attr("height", config.svgHeight)

}