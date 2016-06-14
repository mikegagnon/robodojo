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

  // TODO: document
  var step = false

  // TODO: document
  var remainingCycles = 0.0

  updateMainDiv()
  val canvas = addCanvas()
  val stage = addStage()

  addGrid()
  addBorder()

  val bots = HashMap[Long, createjs.Container]()

  // See documentation for animateMove, section (3)
  val twinBots = HashMap[Long, createjs.Container]()

  addBots()


  // Bots maintain their own cycle counter, which counts the number of cycles relative to a single
  // instruction. The bot cycle counter resets to zero after an instruction is executed.
  // The board has another cycle counter, board.cycleNum, which increments after every call to
  // cycle(). Yet another cycle counter is animationCycleNum, which is the cycle counter at which we
  // currently animating.
  //
  // Notice, just below we execute config.sim.moveCycles cycles, yet animationCycleNum
  // == 0. This is because the animation lags behind the board. We do this so that (when animating)
  // we can look ahead to see if a move will succeed or fail. If the move is destined to fail, we
  // do not animate a successful move operation. See documentation for animateMove, section (2).
  //
  // animations(board.cycleNum)(botId) == the animation for bot (with id == botId) at board.cycleNum
  // point in time.
  val animations = HashMap[Int, HashMap[Long, Animation]]()

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

    animations(board.cycleNum) = HashMap[Long, Animation]()

    // TODO: remove old animations on a rolling basis
    animationList.foreach { animation: Animation =>

      animations(board.cycleNum)(animation.botId) = animation
    }

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

    1 to cycles foreach { _ => cycle() }

    animate()

    stage.update()
  }

  def animate(): Unit = {
    val currentAnimations: HashMap[Long, Animation] = animations(animationCycleNum)

    currentAnimations.values.foreach { animation =>

      animation match {
        case moveAnimation: MoveAnimation => animateMove(moveAnimation)
        case turnAnimation: TurnAnimation => animateTurn(turnAnimation)
      }
    }

  }

  // animateMove is a bit complex. There are three aspects that are worth documenting:
  //    (1) Animating the typical case
  //    (2) Peeking into the future (or the past, depending on your perspective)
  //    (3) Drawing the movement when the bot goes off screen, and wraps around torus style
  //
  // (1) Animating the typical case. First, we peek into the future (see (2)), to determine whether
  //     or not the move will succed (i.e. the bot will move from one cell to another cell). Recall
  //     from MoveInstruction, bots can only move into another cell if the new cell is empty at the
  //     time when the move instruction exectures its last cycle. If the move fails, then the bot
  //     is drawn at its current location. If the move succeeds, then we calculate delta, which
  //     measures how far along the move instruction has progressed. Then we calculate (row, col)
  //     as a double based on the delta. For example if the bot is moving from (0, 0) to (0, 1)
  //     and the move instruction is half-way done executing, then (row, col) == (0, 0.5). Then we
  //     draw the bot at (row, col). A similar approach is used in animateTurn.
  // (2) Peeking into the future. We do not want to animate a bot move if the move fails.
  //     Unfortunately, we cannot know whether or not the move will succeed or fail until
  //     config.sim.moveCycles cycles have been executed. So, the way we get around is is by
  //     running the animation several cycles behind the board simulator. This way, the animation
  //     can peek into the future, to see if the move will fail or succeed.
  // (3) Drawing the movement when the bot goes off screen, and wraps around torus style. How do
  //     we do it? We use "twin images." A twin image is a duplicate image of a bot. The twin image
  //     is normally kept off screen, at (-1, -1). When a bot wraps around the board, we have the
  //     primary image of the bot move off screen. Then, we have the twin image move on screen.
  //     Once the movement is complete, we move the image off screen again.
  def animateMove(animation: MoveAnimation): Unit = {

    // This is where we look into the future to see if the move is successful or not
    val endOfMoveCycleNum = animationCycleNum + config.sim.moveCycles - animation.cycleNum

    // futureAnimation == the animation for when this bot finishes executing its move instruction
    val futureAnimation = animations(endOfMoveCycleNum)(animation.botId)

    // success == true iff the bot successfully moves into a new cell
    val success = futureAnimation match {
      case m: MoveAnimation => m.oldRow != m.newRow || m.oldCol != m.newCol
      case _ => throw new IllegalStateException("Bad")
    }

    // TODO: maybe animate the bot moving forward a half cell, then moving backward a half cell?
    if (!success) {
      return
    }

    // The amount the bot has moved towards its new cell (as a proportion)
    // TODO: change to percentComplete (also in documentation)
    val delta: Double = animation.cycleNum.toDouble / config.sim.moveCycles.toDouble

    val oldRow = animation.oldRow
    val oldCol = animation.oldCol
    val newRow = animation.newRow
    val newCol = animation.newCol

    val (twinRow: Double, twinCol: Double) =
      // if the bot has finished its movement, then move the twin off screen
      if (animation.cycleNum == config.sim.moveCycles) {
        (-1.0, -1.0)
      }
      // if the bot is moving up, towards off the screen
      else if (newRow - oldRow > 1) {
        (newRow + 1.0 - delta, newCol)
      }
      // if the bot is moving down, towards off the screen
      else if (oldRow - newRow > 1) {
        (newRow - 1.0 + delta, newCol)
      }
      // if the bot is moving left, towards off the screen
      else if (newCol - oldCol > 1) {
        (newRow, newCol + 1.0 - delta)
      }
      // if the bot is moving right, towards off the screen
      else if (oldCol - newCol > 1) {
        (newRow, newCol - 1.0 + delta)
      }
      // if the bot isn't wrapping around the screen
      else  {
        (-1.0, -1.0)
      }

    val (row: Double, col: Double) =
      // if the bot has finished its movement, then move the bot to its new home
      if (animation.cycleNum == config.sim.moveCycles) {
        (newRow, newCol)
      }
      // if the bot is moving up, towards off the screen
      else if (newRow - oldRow > 1) {
        (oldRow - delta, oldCol)
      }
      // if the bot is moving down, towards off the screen
      else if (oldRow - newRow > 1) {
        (oldRow + delta, oldCol)
      }
      // if the bot is moving left, towards off the screen
      else if (newCol - oldCol > 1) {
        (oldRow, oldCol - delta)
      }
      // if the bot is moving right, towards off the screen
      else if (oldCol - newCol > 1) {
        (oldRow, oldCol + delta)
      }
      // the bot is moving up
      else if (newRow < oldRow) {
        (oldRow - delta, oldCol)
      }
      // the bot is moving down
      else if (newRow > oldRow) {
        (oldRow + delta, oldCol)
      }
      // the bot is moving left
      else if (newCol < oldCol) {
        (oldRow, oldCol - delta)
      }
      // the bot is moving right
      else if (newCol > oldCol) {
        (oldRow, oldCol + delta)
      } else {
        throw new IllegalStateException("This code shouldn't be reachable")
      }

    val cellSize = config.viz.cellSize

    val botImage = bots(animation.botId)

    botImage.x = retina(cellSize / 2 + cellSize * col)
    botImage.y = retina(cellSize / 2 + cellSize * row)
    botImage.rotation = Direction.toAngle(animation.direction)

    val twinImage = twinBots(animation.botId)

    twinImage.x = retina(cellSize / 2 + cellSize * twinCol)
    twinImage.y = retina(cellSize / 2 + cellSize * twinRow)
    twinImage.rotation = Direction.toAngle(animation.direction)
  }

  // See the documentation for animateMove, section (1).
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







      