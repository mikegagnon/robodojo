package robobot.webapp

// TODO: cleanup imports
import scala.language.postfixOps

import scala.scalajs.js
import js.JSConverters._
import org.scalajs.jquery.jQuery
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.Selection
import org.scalajs.dom

class Viz(val board: Board)(implicit val config: Config) {

  val svg = addSvg()

  drawHorizGrid()
  drawVertGrid()
  drawBorder()
  drawBots()
  addConsole()

  def addSvg() : Selection[dom.EventTarget] = {

    jQuery("#" + config.id).append(s"<div id='${config.viz.mainDivId}'></div>")

    jQuery("#" + config.viz.mainDivId).append(s"<svg id='${config.viz.svgId}'></svg>")

    jQuery("#" + config.viz.svgId)
      .attr("xmlns", "'http://www.w3.lrg/2000/svg'")
      .attr("width", config.viz.svgWidth)
      .attr("height", config.viz.svgHeight)

    d3.select("#" + config.viz.svgId)
  }

  // Draw horizontal grid lines
  def drawHorizGrid() {
    val rowLines = 1 to config.sim.numRows - 1 toJSArray

    svg.selectAll(".row-grid-line")
      .data(rowLines)
      .enter()
      .append("line")
      .attr("class", "grid-line row-grid-line")
      .attr("x1", config.viz.border.strokeWidth)
      .attr("y1", (r:Int) => r * config.viz.cellSize + config.viz.border.strokeWidth)
      .attr("x2", config.sim.numCols * config.viz.cellSize +
        config.viz.border.strokeWidth)
      .attr("y2", (r:Int) => r * config.viz.cellSize + config.viz.border.strokeWidth)
      .style("stroke", config.viz.grid.stroke)
  }

  // Draw vertical grid lines
  def drawVertGrid() {
    val colLines = 1 to config.sim.numCols - 1 toJSArray

    svg.selectAll(".col-grid-line")
      .data(colLines)
      .enter()
      .append("line")
      .attr("class", "grid-line col-grid-line")
      .attr("x1", (c:Int) => c * config.viz.cellSize + config.viz.border.strokeWidth)
      .attr("y1", config.viz.border.strokeWidth)
      .attr("x2", (c:Int) => c * config.viz.cellSize + config.viz.border.strokeWidth)
      .attr("y2", config.sim.numRows * config.viz.cellSize +
        config.viz.border.strokeWidth)
      .style("stroke", config.viz.grid.stroke)
  }

  def drawBorder() {
    svg.append("rect")
      .attr("x", config.viz.border.strokeWidth)
      .attr("y", config.viz.border.strokeWidth)
      .attr("rx", config.viz.border.rxry)
      .attr("ry", config.viz.border.rxry)
      .attr("width", config.viz.border.width)
      .attr("height", config.viz.border.height)
      .style("stroke-width", config.viz.border.strokeWidth)
      .style("stroke", config.viz.border.stroke)
      .style("fill-opacity", 0.0)
  }

  def botTransform(bot: Bot) : String = {

    val x = bot.col * config.viz.cellSize + config.viz.border.strokeWidth
    val y = bot.row * config.viz.cellSize + config.viz.border.strokeWidth
    val translate = "translate(" + x + ", " + y + ") "

    val halfCell = config.viz.cellSize / 2
    val rotate = "rotate(" +
        Direction.toAngle(bot.direction) + " " +
        halfCell + " " + halfCell +")"

    List(translate, rotate).mkString(" ")
  }

  def drawBots() {

    svg.selectAll(".bot")
      .data(board.bots.toJSArray)
      .enter()
      .append("svg:image")
      .attr("class", "bot")
      .attr("xlink:href", "./img/bluebot.svg")
      .attr("height", config.viz.cellSize)
      .attr("width", config.viz.cellSize)
      .attr("transform", botTransform _)
  }

  def addConsole() {

  }

}