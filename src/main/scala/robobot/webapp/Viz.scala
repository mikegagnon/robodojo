package robobot.webapp

// TODO: cleanup imports
import scala.language.postfixOps

import scala.scalajs.js
import js.JSConverters._
import org.scalajs.jquery.{jQuery, JQuery}
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.Selection
import org.scalajs.dom

import scala.math

import com.scalawarrior.scalajs.createjs._

class Viz(val board: Board)(implicit val config: Config) {

  updateMainDiv()
  val canvas = addCanvas()
  val stage = addStage()

  addGrid()
  addBorder()
  addBot()

  stage.update()


  def updateMainDiv(): Unit = {
    jQuery("#" + config.id).attr("class", "robo")
  }

  // TODO: change all Unit methods to use this syntax
  // TODO: change all methods to have return type
  // TODO: return type
  def addCanvas(): JQuery = {

    val canvasHtml = s"""<canvas id="${config.viz.canvas.canvasId}"
          width="${config.viz.canvas.width}"
          height="${config.viz.canvas.height}">"""

    jQuery("#" + config.id).html(canvasHtml)

    val canvas = jQuery("#" + config.viz.canvas.canvasId)

    // From http://www.unfocus.com/2014/03/03/hidpiretina-for-createjs-flash-pro-html5-canvas/
    val height = canvas.attr("height").toOption.map{ h => h.toInt}.getOrElse(0)
    val width = canvas.attr("width").toOption.map{ w => w.toInt}.getOrElse(0)

    // Reset the canvas width and height with window.devicePixelRatio applied
    canvas.attr("width", math.round(width * dom.window.devicePixelRatio))
    canvas.attr("height", math.round(height * dom.window.devicePixelRatio))

    // force the canvas back to the original size using css
    canvas.css("width", width+"px")
    canvas.css("height", height+"px")

    return canvas
  }

  def addStage(): Stage = {
    val stage = new Stage(config.viz.canvas.canvasId)

    // To prevent fuzziness of lines
    // http://stackoverflow.com/questions/6672870/easeljs-line-fuzziness
    //stage.regX = -0.5
    //stage.regY = -0.5

    return stage
  }

  // TODO: better name for this function?
  def ratio(value: Int): Double = value * dom.window.devicePixelRatio

  def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: String): Unit = {
    val line = new Shape()

    line.graphics.setStrokeStyle(ratio(1))
    line.graphics.beginStroke(color)
    line.graphics.moveTo(x1, y1)
    line.graphics.lineTo(x2, y2)
    line.graphics.endStroke()

    stage.addChild(line)
  }

  def addGrid(): Unit = {

    // Draw the horizontal lines
    for (row <- 1 until config.sim.numRows) {
      drawLine(
        0,
        ratio(row * config.viz.cellSize).toInt,
        ratio(config.sim.numCols * config.viz.cellSize).toInt,
        ratio(row * config.viz.cellSize).toInt,
        config.viz.grid.stroke)
    }

    for (col <- 1 until config.sim.numCols) {
      drawLine(
        ratio(col * config.viz.cellSize).toInt,
        0,
        ratio(col * config.viz.cellSize).toInt,
        ratio(config.sim.numRows * config.viz.cellSize).toInt,
        config.viz.grid.stroke)
    }
  }

  def addBorder(): Unit = {

    for(i <- 1 to ratio(config.viz.border.thickness).toInt) {
      val rect = new Shape()

      rect.graphics.beginStroke(config.viz.border.stroke).drawRect(i, i,
        ratio(config.viz.canvas.width - i), ratio(config.viz.canvas.height - i))

      stage.addChild(rect)
    }

  }

  def addBot(): Unit = {
    val img =  dom.document.createElement("img").asInstanceOf[dom.raw.HTMLImageElement]

    // TODO: Conifg option
    img.src = "./img/bluebot.png";
    img.onload = { event: dom.raw.Event =>

      val bitmap = new Bitmap(img);
      val container = new Container()
      container.addChild(bitmap)

      container.x = ratio(32)
      container.y = ratio(32)

      container.regX = ratio(32 + 16)
      container.regX = ratio(32 + 16)

      stage.addChild(container);
      //bitmap.x = ratio(16)
      //bitmap.y = ratio(16)
      /*bitmap.regX = ratio(16)
      bitmap.regY = ratio(16)*/

      //bitmap.rotation = 0

      bitmap.addEventListener("click", (event: Object) => {
        container.rotation+=15
        container.regX = ratio(16)
        container.regY = ratio(16)
        container.x = ratio(16)
        container.y = ratio(16)
        stage.update()
        false
      })

      stage.update()
    }
  }

}