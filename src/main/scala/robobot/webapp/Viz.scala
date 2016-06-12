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

  addBorder()
  addBot()

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
    stage.regX = -0.5
    stage.regY = -0.5

    return stage
  }

  def addBorder(): Unit = {
    val rect = new Shape()

    rect.graphics.beginStroke(config.viz.border.stroke).drawRect(1, 1,
      (config.viz.canvas.width -1) * dom.window.devicePixelRatio,
      (config.viz.canvas.height -1) * dom.window.devicePixelRatio)

    stage.addChild(rect)

    val rect2 = new Shape()

    rect2.graphics.beginStroke(config.viz.border.stroke).drawRect(2, 2,
      (config.viz.canvas.width -2) * dom.window.devicePixelRatio,
      (config.viz.canvas.height -2) * dom.window.devicePixelRatio)

    stage.addChild(rect2)
  }

  def addBot(): Unit = {
    val img =  dom.document.createElement("img").asInstanceOf[dom.raw.HTMLImageElement]
    img.src = "./img/bluebot.png";
    img.onload = { event: dom.raw.Event =>
      println("loaded")
      val tempBitMap = new Bitmap(img);
      stage.addChild(tempBitMap);
      tempBitMap.x = 0
      tempBitMap.y = 0
      stage.update()
    }
  }

}