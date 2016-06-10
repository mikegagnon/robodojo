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

import com.scalawarrior.scalajs.createjs._

class Viz(val board: Board)(implicit val config: Config) {

  updateMainDiv()
  addCanvas()

  def updateMainDiv(): Unit = {
    jQuery("#" + config.id).attr("class", "robo")
  }

  // TODO: change all Unit methods to use this syntax
  // TODO: change all methods to have return type
  def addCanvas(): Unit = {

    val canvasHtml = s"""<canvas id="testCanvas"
          width="${config.viz.canvas.width}"
          height="${config.viz.canvas.height}"></canvas>"""

    jQuery("#" + config.id).append(canvasHtml)

  }

}