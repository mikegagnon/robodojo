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
import scala.collection.mutable.HashMap

import com.scalawarrior.scalajs.createjs

class Viz(val preload: createjs.LoadQueue, val board: Board)(implicit val config: Config) {

  updateMainDiv()
  val canvas = addCanvas()
  val stage = addStage()

  addGrid()
  addBorder()

  val bots = HashMap[Long, createjs.Container]()

  addBots()

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

  def addStage(): createjs.Stage = {
    val stage = new createjs.Stage(config.viz.canvas.canvasId)

    // To prevent fuzziness of lines
    // http://stackoverflow.com/questions/6672870/easeljs-line-fuzziness
    //stage.regX = -0.5
    //stage.regY = -0.5

    return stage
  }

  def retina(value: Double): Double = value * dom.window.devicePixelRatio

  def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: String): Unit = {
    val line = new createjs.Shape()

    line.graphics.setStrokeStyle(retina(1))
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
        retina(row * config.viz.cellSize).toInt,
        retina(config.sim.numCols * config.viz.cellSize).toInt,
        retina(row * config.viz.cellSize).toInt,
        config.viz.grid.stroke)
    }

    for (col <- 1 until config.sim.numCols) {
      drawLine(
        retina(col * config.viz.cellSize).toInt,
        0,
        retina(col * config.viz.cellSize).toInt,
        retina(config.sim.numRows * config.viz.cellSize).toInt,
        config.viz.grid.stroke)
    }
  }

  def addBorder(): Unit = {

    for(i <- 1 to retina(config.viz.border.thickness).toInt) {
      val rect = new createjs.Shape()

      rect.graphics.beginStroke(config.viz.border.stroke).drawRect(i, i,
        retina(config.viz.canvas.width - i), retina(config.viz.canvas.height - i))

      stage.addChild(rect)
    }

  }

  def addBots(): Unit = {

    board.bots.foreach { bot =>
      addBot(bot)
    }
  }

  // TODO: if rotation is a performance issue, then rotate using pre-rotated sprites
  // TODO: upscale the image of the bot, so it still looks good for cellSize > 32
  def addBot(bot: Bot): Unit = {

    val img = preload.getResult(config.viz.preload.blueBotId)
      .asInstanceOf[org.scalajs.dom.raw.HTMLImageElement]

    val bitmap = new createjs.Bitmap(img);

    // scale the bitmap
    val width = bitmap.image.width
    val height = bitmap.image.height

    if (width != height) {
      throw new IllegalArgumentException("Bot image.width != image.height")
    }

    val widthHeight = width
    val cellPhysicalSize = retina(config.viz.cellSize)
    bitmap.scaleX = cellPhysicalSize / widthHeight.toDouble
    bitmap.scaleY = cellPhysicalSize / widthHeight.toDouble

    val container = new createjs.Container()

    bots += bot.id -> container

    container.addChild(bitmap)

    val halfCell = config.viz.cellSize / 2

    container.regX = retina(halfCell)
    container.regY = retina(halfCell)
    container.x = retina(halfCell + config.viz.cellSize * bot.col)
    container.y = retina(halfCell + config.viz.cellSize * bot.row)

    stage.addChild(container);

    container.rotation = Direction.toAngle(bot.direction)

  }

  var remainingCycles = 0.0

  // Bummer: 20FPS burns between 30% and 40% CPU on my machine
  def tick(event: js.Dynamic): Unit = {

    // TODO: put cycle calculator in separate function?

    // Time elapsed sine list tick
    val delta = event.delta.asInstanceOf[Double]

    // The number of cycles to execute this tick
    // TODO: explain remainingCycles
    val cyclesDouble: Double = config.viz.cyclesPerSecond * delta / 1000.0 + remainingCycles

    // TODO: round?
    val cycles = Math.floor(cyclesDouble).toInt

    remainingCycles = cyclesDouble - cycles

    if (remainingCycles >= 1.0) {
      throw new IllegalStateException("remainingCycles >= 1.0")
    }

    var animations: List[Animation] = Nil

    // TODO: do something fancier to aggregate all the animations, rather than just taking the last
    // one. Perhaps monoids?
    1 to cycles foreach { _ =>
      animations = board.cycle()
    }

    animations.foreach { animation =>
      animation match {
        case moveAnimation: MoveAnimation => animateMove(moveAnimation)
        case turnAnimation: TurnAnimation => animateTurn(turnAnimation)
      }
    }

    stage.update()

    println(cycles)

  }

  // todo explain
  def animateMove(animation: MoveAnimation): Unit = {

    val bot = animation.bot

    val delta: Double = animation.cycleNum.toDouble / config.sim.moveCycles.toDouble

    val row = if (animation.row < bot.row) {
        bot.row - delta
      } else if (animation.row > bot.row) {
        bot.row + delta
      } else {
        bot.row
      }

    val col = if (animation.col < bot.col) {
        bot.col - delta
      } else if (animation.col > bot.col) {
        bot.col + delta
      } else {
        bot.col
      }

    bots(bot.id).x = retina((config.viz.cellSize / 2 + config.viz.cellSize * col))
    bots(bot.id).y = retina((config.viz.cellSize / 2 + config.viz.cellSize * row))
    bots(bot.id).rotation = Direction.toAngle(bot.direction)
  }

  // TODO: explain
  def animateTurn(animation: TurnAnimation): Unit = {

    val bot = animation.bot
    val oldDirection = animation.oldDirection
    val percentComplete = animation.cycleNum.toDouble / config.sim.turnCycles.toDouble

    val angle: Double = animation.leftOrRight match {
      case Direction.Left => Direction.toAngle(oldDirection) - 90.0 * percentComplete
      case Direction.Right => Direction.toAngle(oldDirection) + 90.0 * percentComplete
      case _ => throw new IllegalStateException("Bots can only turn Left or Right")
    }

    bots(bot.id).rotation = angle

  }

}







      