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
import scala.collection.mutable.{ArrayBuffer, HashMap}

import com.scalawarrior.scalajs.createjs

class Viz(val preload: createjs.LoadQueue, val board: Board)(implicit val config: Config) {

  // HACK: clickStep
  var step = false

  // TODO: document
  var remainingCycles = 0.0

  updateMainDiv()
  val canvas = addCanvas()
  val stage = addStage()

  addGrid()
  addBorder()

  val bots = HashMap[Long, createjs.Container]()
  val twinBots = HashMap[Long, createjs.Container]()

  addBots()


  // Bots maintain their own cycle counter, which counts the number of cycles relative to a single
  // instruction. The bot cycle counter resets to zero after an instruction is executed.
  // The board has another cycle counter, boardCycleNum, which increments after every call to
  // cycle(). Yet another cycle counter is animationCycleNum, which is cycle counter at which we
  // currently animating
  //
  // Notice, just below we execute config.sim.moveCycles cycles, yet animationCycleNum
  // == 0. This is because the animation lags behind the board. We do this so that (when animating)
  // we can look ahead to see if a move will succeed or fail. If the move is destined to fail, we
  // do not animate a successful move operation.
  //
  // animations(boarCycleNum) == A list of all animations at cycleNum point in time
  // TODO: change ArrayBuffer into HashMap?
  val animations = HashMap[Int, ArrayBuffer[Animation]]()
  var boardCycleNum = 0
  var animationCycleNum = 0

  1 to (config.sim.moveCycles + 1) foreach { _ => cycle() }

  animationCycleNum = 0

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

    val halfCell = config.viz.cellSize / 2

    def newBotContainer(bot: Bot): createjs.Container = {
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

      container.addChild(bitmap)

      container.regX = retina(halfCell)
      container.regY = retina(halfCell)
      container.x = retina(halfCell + config.viz.cellSize * bot.col)
      container.y = retina(halfCell + config.viz.cellSize * bot.row)

      container.rotation = Direction.toAngle(bot.direction)

      return container
    }

    val container = newBotContainer(bot)
    bots += bot.id -> container
    stage.addChild(container)

    val twinContainer = newBotContainer(bot)
    twinContainer.x = retina(halfCell + config.viz.cellSize * -1)
    twinContainer.y = retina(halfCell + config.viz.cellSize * -1)
    twinBots += bot.id -> twinContainer
    stage.addChild(twinContainer)


  }

  // TODO: do something fancier to aggregate all the animations, rather than just taking the last
  // one. Perhaps monoids?
  def cycle(): Unit = {
    val animationList = board.cycle()

    animations(boardCycleNum) = ArrayBuffer[Animation]()

    // TODO: remove old animations on a rolling basis
    animationList.foreach { animation: Animation =>

      animations(boardCycleNum) += animation
    }

    boardCycleNum += 1
    animationCycleNum += 1
  }

  def tickStep(): Int = {
    
    remainingCycles = 0.0

    val cycles = 1
    
    createjs.Ticker.paused = true
    
    return cycles
  }

  def tickMultiStep(event: js.Dynamic): Int = {

    // Time elapsed sine list tick
    val delta = event.delta.asInstanceOf[Double]

    // The number of cycles to execute this tick
    // TODO: explain remainingCycles
    val cyclesDouble: Double = config.viz.cyclesPerSecond * delta / 1000.0 + remainingCycles

    val cycles = Math.floor(cyclesDouble).toInt

    remainingCycles = cyclesDouble - cycles

    if (remainingCycles >= 1.0) {
      throw new IllegalStateException("remainingCycles >= 1.0")
    }

    return cycles
  }

  // Bummer: 20FPS burns between 30% and 40% CPU on my machine
  def tick(event: js.Dynamic): Unit = {
    
    if (createjs.Ticker.paused) {
      return
    }

    val cycles = 
      if (step) {
        tickStep()
      } else {
        tickMultiStep(event)
      }

    var animations: List[Animation] = Nil

    1 to cycles foreach { _ => cycle() }

    animate()

    stage.update()
  }

  def animate(): Unit = {
    val currentAnimations: ArrayBuffer[Animation] = animations(animationCycleNum)

    currentAnimations.foreach { animation =>

      animation match {
        case moveAnimation: MoveAnimation => animateMove(moveAnimation)
        case turnAnimation: TurnAnimation => animateTurn(turnAnimation)
      }
    }

  }

  // todo explain
  def animateMove(animation: MoveAnimation): Unit = {

    // This is where we look into the future to see if the move is successful or not
    val endOfMoveCycleNum = animationCycleNum + config.sim.moveCycles - animation.cycleNum

    // TODO: clean this up
    val futureAnimation = animations(endOfMoveCycleNum)
      .flatMap { a =>
        if (a.botId == animation.botId) {
          Some(a)
        } else {
          None
        }
      }
      .headOption


    // TODO: change to futureAnimation.map. Improve error messages
    val success = if (futureAnimation.isEmpty) {
      throw new IllegalStateException("Bad")
    } else {
      futureAnimation.get match {
        case m: MoveAnimation => m.oldRow != m.newRow || m.oldCol != m.newCol
        case _ => throw new IllegalStateException("Bad")
      }

    }

    if (!success) {
      return
    }

    val delta: Double = animation.cycleNum.toDouble / config.sim.moveCycles.toDouble

    // TODO: change to val Option
    var twinRow = -1.0
    var twinCol = -1.0

    // TODO: reorder so it's UDLR
    val row: Double = // if the bot is moving down, to off the screen
      if (animation.oldRow - animation.newRow > 1) {
        println("Row torus")
        if (animation.cycleNum == config.sim.moveCycles) {
          twinRow = -1.0
          twinCol = -1.0
          animation.newRow
        } else {
          twinRow = animation.newRow - 1.0 + delta
          twinCol = animation.newCol
          animation.oldRow + delta
        }
      }
      // if the bot is moving up, to off the screen
      else if (animation.newRow - animation.oldRow > 1) {
        if (animation.cycleNum == config.sim.moveCycles) {
          twinRow = -1.0
          twinCol = -1.0
          animation.newRow
        } else {
          twinRow = animation.newRow + 1.0 - delta
          twinCol = animation.newCol
          animation.oldRow - delta
        }
      } else if (animation.newRow < animation.oldRow) {
        animation.oldRow - delta
      } else if (animation.newRow > animation.oldRow) {
        animation.oldRow + delta
      } else {
        animation.oldRow
      }

    val col: Double =

      // if the bot is moving right, to off the screen
      if (animation.oldCol - animation.newCol > 1) {

        if (animation.cycleNum == config.sim.moveCycles) {
          twinRow = -1.0
          twinCol = -1.0
          animation.newCol
        } else {
          twinRow = animation.newRow
          twinCol = animation.newCol - 1.0 + delta
          animation.oldCol + delta
        }
      }
      // if the bot is moving left, to off the screen
      else if (animation.newCol - animation.oldCol > 1) {
        if (animation.cycleNum == config.sim.moveCycles) {
          twinRow = -1.0
          twinCol = -1.0
          animation.newCol
        } else {
          twinRow = animation.newRow
          twinCol = animation.newCol + 1.0 - delta
          animation.oldCol - delta
        }
      }
      else if (animation.newCol < animation.oldCol) {
        animation.oldCol - delta
      } else if (animation.newCol > animation.oldCol) {
        animation.oldCol + delta
      } else {
        animation.oldCol
      }

    // TODO: use half val
    bots(animation.botId).x = retina(config.viz.cellSize / 2 + config.viz.cellSize * col)
    bots(animation.botId).y = retina(config.viz.cellSize / 2 + config.viz.cellSize * row)
    bots(animation.botId).rotation = Direction.toAngle(animation.direction)


    //if (twinRow != -1.0 || twinCol != -1.0) {
      println(twinRow, twinCol)
      twinBots(animation.botId).x = retina(config.viz.cellSize / 2 + config.viz.cellSize * twinCol)
      twinBots(animation.botId).y = retina(config.viz.cellSize / 2 + config.viz.cellSize * twinRow)

      //println(twinBots(animation.botId).x, twinBots(animation.botId).y)
      twinBots(animation.botId).rotation = Direction.toAngle(animation.direction)

      //println(twinBots(animation.botId).x, twinBots(animation.botId).y)
    //}



  }

  // TODO: explain
  def animateTurn(animation: TurnAnimation): Unit = {

    //val bot = animation.bot
    val oldDirection = animation.oldDirection
    val percentComplete = animation.cycleNum.toDouble / config.sim.turnCycles.toDouble

    val angle: Double = animation.leftOrRight match {
      case Direction.Left => Direction.toAngle(oldDirection) - 90.0 * percentComplete
      case Direction.Right => Direction.toAngle(oldDirection) + 90.0 * percentComplete
      case _ => throw new IllegalStateException("Bots can only turn Left or Right")
    }

    bots(animation.botId).rotation = angle

  }

}







      