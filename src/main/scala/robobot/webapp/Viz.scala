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

  val stage = addStage()

  addBorder()
  addCircle()

  def updateMainDiv(): Unit = {
    jQuery("#" + config.id).attr("class", "robo")
  }

  // TODO: change all Unit methods to use this syntax
  // TODO: change all methods to have return type
  def addCanvas(): Unit = {

    val canvasHtml = s"""<canvas id="${config.viz.canvas.canvasId}"
          width="${config.viz.canvas.width}"
          height="${config.viz.canvas.height}">"""

    val canvas = jQuery("#" + config.id).html(canvasHtml)

  }

  def addStage(): Stage = {
    val stage = new Stage(config.viz.canvas.canvasId)

    // To prevent fuzziness of lines
    // http://stackoverflow.com/questions/6672870/easeljs-line-fuzziness
    stage.regX = -0.5
    stage.regY = -0.5

    stage
  }

  def addBorder(): Unit = {
    val rect = new Shape()

    rect.graphics.beginStroke(config.viz.border.stroke).drawRect(1, 1, config.viz.canvas.width-2,
      config.viz.canvas.height-2)

    stage.addChild(rect)
  }

  def addCircle(): Unit = {
    val circle = new Shape()

    circle.graphics.beginFill("slateblue").drawCircle(25.5, 25.5, 20.5)

    stage.addChild(circle)
    stage.update()
  }

}