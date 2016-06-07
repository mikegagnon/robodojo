package robobot.webapp

import org.scalajs.jquery.jQuery
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.Ops._

class Viz(val config: Config) {

  jQuery("body").append(s"<div id='${config.mainDivId}'></div>")

  jQuery("#" + config.mainDivId).append(s"<svg id='${config.svgId}'></svg>")

  val svg = jQuery("#" + config.svgId)

  svg.attr("xmlns", "'http://www.w3.lrg/2000/svg'")
  svg.attr("width", config.svgWidth)
  svg.attr("height", config.svgHeight)

  d3.select("#" + config.svgId)
    .append("rect")
    .attr("x", config.svgBorderStrokeWidth)
    .attr("y", config.svgBorderStrokeWidth)
    .attr("rx", config.svgBorderRxRy)
    .attr("ry", config.svgBorderRxRy)
    .attr("width", config.svgBorderWidth)
    .attr("height", config.svgBorderHeight)
    .style("stroke-width", config.svgBorderStrokeWidth)
    .style("stroke", config.svgBorderStroke)
    .style("fill", "#fff")



}