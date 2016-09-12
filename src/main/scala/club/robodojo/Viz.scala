package club.robodojo

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

class Viz(val preload: createjs.LoadQueue, var board: Board)(implicit val config: Config) {

  /** Begin constant definitions ******************************************************************/

  val cellSize = config.viz.cellSize
  val halfCell = cellSize / 2.0

  /** End constant definitions ********************************************************************/

  /** Begin initialization ************************************************************************/

  // Here's how the step functionality works. When the user clicks step, the controller sets step =
  // true, then unpauses the Ticker. Then Viz executes exactly one cycle, and pauses the Ticker.
  var step = false

  // Every tick (except when step == true), Viz calculates how many cycles to execute, as a Double.
  // Viz executes floor(numCycles + remainingCycles) cycles and sets remainingCycles =
  // numCycles - floor(numCycles). This way, the number of cycles executed in one second ~=
  // config.viz.cyclesPerSecond.
  var remainingCycles = 0.0

  updateMainDiv()

  val canvas = addCanvas()
  val stage = addStage()

  addBackground()
  addGrid()

  // botImages(botId) == the image on the stage for that bot
  val botImages = HashMap[Long, createjs.Container]()

  // See documentation for animateMove, section (3)
  val twinBotImages = HashMap[Long, createjs.Container]()

  // See documentation for animateBirthProgress
  val birthBotImages = HashMap[Long, createjs.Container]()

  // See documentation for BotVisualFeatures in Animation.scala
  // botVisualFeatures(botId) == the visual features for that bot
  var botVisualFeatures = HashMap[Long, BotVisualFeatures]()

  // Sometimes Viz draws a shape on the board that must be visible for at least one tick.
  // shapesToBeRemovedNextTick contains the shapes that should be removed after the next tick.
  // TODO: change to List[createjs.Container]?
  var shapesToBeRemovedNextTick = List[createjs.Shape]()

  // The set of all mandatory animations for a given tick
  // Set[(cycleNum, animation)]
  var mandatoryAnimations = Set[(Int, Animation)]()

  // All the animations from the last step in a tick
  var animationsLastStep = IndexedSeq[Animation]()

  var controller: Controller = null

  /** End initialization **************************************************************************/

  /** Begin initialization functions **************************************************************/

  def init(control: Controller): Unit = {

    controller = control

    addBotImages()

    stage.update()

    drawNumSteps()
  }

  def updateMainDiv(): Unit = {
    jQuery("#" + config.id).attr("class", "robo")
  }

  def addCanvas(): JQuery = {

    val canvasHtml = s"""
      <div id="${config.viz.boardWrapperDivId}" class="window">
        <canvas id="${config.viz.canvas.canvasId}"
                class="dark-border"
                width="${config.viz.canvas.width}"
                height="${config.viz.canvas.height}"
                style="clear: left">
      </div>"""

    jQuery("#" + config.id).html(canvasHtml)

    val canvas = jQuery("#" + config.viz.canvas.canvasId)

    // From http://www.unfocus.com/2014/03/03/hidpiretina-for-createjs-flash-pro-html5-canvas/
    val height = canvas.attr("height").toOption.map{ h => h.toInt}.getOrElse(0)
    val width = canvas.attr("width").toOption.map{ w => w.toInt}.getOrElse(0)

    // Reset the canvas width and height with window.devicePixelRatio applied
    canvas.attr("width", math.round(width * dom.window.devicePixelRatio))
    canvas.attr("height", math.round(height * dom.window.devicePixelRatio))

    // Force the canvas back to the original size using css
    canvas.css("width", width+"px")
    canvas.css("height", height+"px")

    return canvas
  }

  def addStage(): createjs.Stage = {
    val stage = new createjs.Stage(config.viz.canvas.canvasId)
    stage.regX = -0.5
    stage.regY = -0.5
    stage.enableMouseOver()
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

  def addBackground(): Unit = {
    val rect = new createjs.Shape()

    rect.graphics.beginFill(config.viz.backgroundColor)
      .drawRect(0, 0, retina(config.viz.canvas.width), retina(config.viz.canvas.height))

    stage.addChild(rect)
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

  def addBotImages(): Unit =
    board.bots.foreach { bot =>
      addBot(bot.id, bot.playerColor, bot.row, bot.col, bot.direction, bot.active)
    }

  // Either highlight or de-highlight the specified bot
  def updateBotHighlight(botId: Long, highlight: Boolean): Unit = {
    val botContainer = botImages(botId)
    botContainer.getChildByName("highlighter").visible = highlight
  }

  def newBotContainer(
      botId: Long,
      playerColor: PlayerColor.EnumVal,
      row: Int,
      col: Int,
      direction: Direction.EnumVal): createjs.Container = {

    // The bot is drawn as multiple layers
    // container holds all those layers
    val container = new createjs.Container()

    // Whenever a bot is being debugged, the debugged bot is highlighted by drawing
    // a colorful square underneath the bot. This is that colorful square.
    val highlightCell = new createjs.Shape()
    highlightCell.name = "highlighter"

    val x = retina(0)
    val y = retina(0)
    val w = retina(config.viz.cellSize)
    val h = retina(config.viz.cellSize)

    val borderColor = config.debugger.highlightBorderColor
    val fillColor = config.debugger.highlightColor
    highlightCell.graphics.beginStroke(borderColor).beginFill(fillColor).drawRect(x, y, w, h)
    highlightCell.visible = false
    highlightCell.alpha = 0.5

    container.addChild(highlightCell)

    // Draw the bot
    val botColor = playerColor match {
      case PlayerColor.Blue => config.viz.preload.blueBotId
      case PlayerColor.Red => config.viz.preload.redBotId
      case PlayerColor.Green => config.viz.preload.greenBotId
      case PlayerColor.Yellow => config.viz.preload.yellowBotId
    }

    val img = preload.getResult(botColor)
      .asInstanceOf[org.scalajs.dom.raw.HTMLImageElement]

    val bitmap = new createjs.Bitmap(img);

    val width = bitmap.image.width
    val height = bitmap.image.height

    if (width != height) {
      throw new IllegalArgumentException("Bot image.width != image.height")
    }

    // scale the bitmap
    val widthHeight = width
    val cellPhysicalSize = retina(config.viz.cellSize)
    bitmap.scaleX = cellPhysicalSize / widthHeight.toDouble
    bitmap.scaleY = cellPhysicalSize / widthHeight.toDouble

    container.addChild(bitmap)

    // Set the "registration point" for the image to the center of image. This way, we can rotate
    // around the center. As a consequnce of having a centered registration point, when we move
    // bot images, we must move the (x,y) to the center of the cell, instead of the (0,0) of the
    // cell
    container.regX = retina(halfCell)
    container.regY = retina(halfCell)

    container.x = retina(halfCell + config.viz.cellSize * col)
    container.y = retina(halfCell + config.viz.cellSize * row)

    container.rotation = Direction.toAngle(direction)

    return container
  }

  def addBot(botId: Long,
      playerColor: PlayerColor.EnumVal,
      row: Int,
      col: Int,
      direction: Direction.EnumVal,
      active: Short): Unit = {

    val twinContainer = newBotContainer(botId, playerColor, -1, -1, direction)
    val birthContainer = newBotContainer(botId, playerColor, -1, -1, direction)
    val container = newBotContainer(botId, playerColor, row, col, direction)

    def onBotClick(event: Object): Boolean = {
      controller.debugger.onBotClick(botId)
      return false
    }

    container.cursor = "pointer"
    container.on("click", onBotClick _)

    if (createjs.Ticker.paused) {
      container.mouseEnabled = true
    } else {
      container.mouseEnabled = false
    }

    twinBotImages += (botId -> twinContainer)
    birthBotImages += (botId -> birthContainer)
    botImages += (botId -> container)

    stage.addChild(twinContainer)
    stage.addChild(birthContainer)
    stage.addChild(container)

    botVisualFeatures(botId) = BotVisualFeatures(None)

    if (active < 1) {
      drawInactive(botId)
    }
  }

  /** End initialization functions ****************************************************************/

  /** Begin misc functions  ***********************************************************************/

  def newBoard(newBoard: Board): Unit = {
    board = newBoard
    step = false
    remainingCycles = 0.0

    botImages.keys.foreach { id =>
      botImages -= id
    }

    twinBotImages.keys.foreach { id =>
      twinBotImages -= id
    }

    birthBotImages.keys.foreach { id =>
      birthBotImages -= id
    }

    stage.removeAllChildren()

    botVisualFeatures = HashMap[Long, BotVisualFeatures]()

    addBackground()
    addGrid()
    addBotImages()

    stage.update()

  }

  // TODO: do something fancier to aggregate all the animations, rather than just taking the last
  // one. Perhaps monoids?
  // Returns true if we have hit a breakpoint; false otherwise.
  // Setting disableBreakpoint to true disables stopping at breakpoints
  def cycle(disableBreakpoint: Boolean): Boolean = {

    var breakpointHit =
      controller
        .debugger
        .botIdDebugged
        .flatMap { botId: Long =>
          board.getBot(botId)
        }
        .map { bot: Bot =>

          val bankIndex = bot.bankIndex
          val instructionIndex = bot.instructionIndex
          val breakpoint = Breakpoint(instructionIndex, bankIndex)

          // TODO: add different data structure to Debugger for more efficient
          // querying for breakpoint?
          bot.cycleNum == 1 && controller.debugger.breakpoints.values.toList.contains(breakpoint)
        }
        .getOrElse(false)

    //
    if (!breakpointHit) controller.debugger.cycleToNum match {
      case Some(cycleToNum) => {
        if (board.cycleNum == cycleToNum) {
          breakpointHit = true
        }
      }
      case None => ()
    }

    if (breakpointHit && !disableBreakpoint) {
      return true
    }

    animationsLastStep = board.cycle()

    // Update mandatoryAnimations
    animationsLastStep.filter { animation: Animation =>
        animation.mandatory
      }
      .foreach { animation: Animation =>
        mandatoryAnimations += ((board.cycleNum, animation))
      }

    return false
  }

  def tickStep(): Int = {
    
    remainingCycles = 0.0

    val cycles = 1
    
    createjs.Ticker.paused = true
    
    return cycles
  }

  // See documentation for remainingCycles
  def tickMultiStep(event: js.Dynamic): Int = {

    // Time elapsed since list tick
    val delta = event.delta.asInstanceOf[Double]

    // The number of cycles to execute this tick
    val cyclesDouble: Double = config.viz.cyclesPerSecond * delta / 1000.0 + remainingCycles

    val cycles: Int = Math.floor(cyclesDouble).toInt

    remainingCycles = cyclesDouble - cycles

    if (remainingCycles >= 1.0) {
      throw new IllegalStateException("remainingCycles >= 1.0")
    }

    return cycles
  }

  // Bummer: 20FPS burns between 30% and 40% CPU on my machine
  def tick(event: js.Dynamic): Unit = {
    val calculatedCycles =
      if (step) {
        tickStep()
      } else {
        tickMultiStep(event)
      }

    mandatoryAnimations = Set[(Int, Animation)]()

    val numCyclesThisTick = Math.min(config.viz.maxCyclesPerTick, calculatedCycles)

    // TODO: do takeWhile instead of break boolean
    var break = false

    // TODO: refactor
    var actualNumCyclesThisTick = 0

    0 until numCyclesThisTick foreach { cycleNum =>

      if (!break) {
        val breakpointHit = cycle(cycleNum == 0)
        if (breakpointHit) {
          break = true
          createjs.Ticker.paused = false
          controller.clickPlayPause()
        } else {
          actualNumCyclesThisTick += 1
        }
      }

    }

    shapesToBeRemovedNextTick.foreach { shape: createjs.Shape =>
      stage.removeChild(shape)
    }

    shapesToBeRemovedNextTick = List()

    animate(numCyclesThisTick)

    stage.update()

    controller.debugger.tick()
  }

  /** End misc functions  *************************************************************************/

  /** Begin animation functions  ******************************************************************/


  // Returns a collection containing every animation that should be drawn for this step
  def getAnimationsForThisTick(numCyclesThisTick: Int): Iterable[Animation] = {

    if (numCyclesThisTick < 1) {
      throw new IllegalStateException("numCyclesThisTick < 1")
    }

    val mandatory = mandatoryAnimations
      .filter { case (cycleNum, _) =>

        // Drop animations for board.cylceNum because animationsLastStep convers those, and we
        // don't want them done twice.
        cycleNum != board.cycleNum
      }
      .toList
      // sort by cycleNum
      .sortBy { _._1 }
      .map{ _._2}

    return mandatory ++ animationsLastStep
  }

  def animate(numCyclesThisTick: Int): Unit = {

    if (numCyclesThisTick == 0) {
      return
    }

    getAnimationsForThisTick(numCyclesThisTick).foreach { animation =>
      animation match {
        case moveAnimation: MoveAnimationProgress => ()
        case moveAnimation: MoveAnimationSucceed => animateMoveSucceed(moveAnimation)
        case moveAnimation: MoveAnimationFail => ()
        case birthAnimation: BirthAnimationProgress => ()
        case birthAnimation: BirthAnimationSucceed => animateBirthSucceed(birthAnimation)
        case birthAnimation: BirthAnimationFail => ()
        case turnAnimation: TurnAnimationProgress => ()
        case turnAnimation: TurnAnimationFinish => animateTurnFinish(turnAnimation)
        case deactivateAnimation: DeactivateAnimation => animateDeactivate(deactivateAnimation)
        case activateAnimation: ActivateAnimation => animateActivate(activateAnimation)
        case fatalError: FatalErrorAnimation => animateFatalError(fatalError)
        case bankColorAnimation: BankColorAnimation => animateBankColor(bankColorAnimation)
      }
    }

    drawNumSteps()
  }

  def drawNumSteps(): Unit =
    jQuery("#" + config.viz.cycleCounterId).text("Step " + board.cycleNum.toString)

  def animateMoveSucceed(animation: MoveAnimationSucceed): Unit = {

    val botImage = botImages(animation.botId)
    botImage.x = retina(halfCell + cellSize * animation.newCol)
    botImage.y = retina(halfCell + cellSize * animation.newRow)
    botImage.rotation = Direction.toAngle(animation.direction)

    val twinImage = twinBotImages(animation.botId)
    twinImage.x = retina(halfCell - cellSize)
    twinImage.y = retina(halfCell - cellSize)
  }

  def animateBirthSucceed(animation: BirthAnimationSucceed): Unit = {

    val active: Short = 0
    addBot(animation.newBotId,
      animation.playerColor,
      animation.row,
      animation.col,
      animation.direction,
      active)

    val birthImage = birthBotImages(animation.botId)
    birthImage.x = retina(halfCell - cellSize)
    birthImage.y = retina(halfCell - cellSize)

    val twinImage = twinBotImages(animation.botId)
    twinImage.x = retina(halfCell - cellSize)
    twinImage.y = retina(halfCell - cellSize)
  }

  def animateTurnProgress(animation: TurnAnimationProgress): Unit = {

    val oldDirection = animation.oldDirection

    // See the documentation for animateMove, section (1).
    val proportionCompleted = animation.cycleNum.toDouble / config.sim.cycleCount.durTurn.toDouble

    val angle: Double = animation.leftOrRight match {
      case Direction.Left => Direction.toAngle(oldDirection) - 90.0 * proportionCompleted
      case Direction.Right => Direction.toAngle(oldDirection) + 90.0 * proportionCompleted
      case _ => throw new IllegalStateException("Bots can only turn Left or Right")
    }

    val botImage = botImages(animation.botId)
    botImage.rotation = angle
  }

  def animateTurnFinish(animation: TurnAnimationFinish): Unit = {

    val angle = Direction.toAngle(animation.direction)

    val botImage = botImages(animation.botId)
    botImage.rotation = angle
  }

  def drawInactive(botId: Long): Unit = {

    val line = new createjs.Shape()

    line.graphics.setStrokeStyle(retina(cellSize * 0.15))
    line.graphics.beginStroke(config.viz.backgroundColor)
    line.graphics.moveTo(retina(1), retina(cellSize / 2.0))
    line.graphics.lineTo(retina(cellSize - 1), retina(cellSize / 2.0))
    line.graphics.endStroke()

    botImages(botId).addChild(line)

    botVisualFeatures(botId).inactiveShape = Some(line)

  }

  def drawActive(botId: Long): Unit = {

    val line = botVisualFeatures(botId).inactiveShape.get

    botImages(botId).removeChild(line)
    botVisualFeatures(botId).inactiveShape = None

  }

  def animateDeactivate(deactivateAnimation: DeactivateAnimation): Unit = {

    val recipientBotId = deactivateAnimation.recipientBotId

    botVisualFeatures
      .get(recipientBotId)
      .map { features: BotVisualFeatures =>
        // If the deactivate visualization hasn't alrady been drawn
        if (features.inactiveShape.isEmpty) {
          drawInactive(recipientBotId)
        }
      }
  }

  def animateActivate(activateAnimation: ActivateAnimation): Unit = {

    val recipientBotId = activateAnimation.recipientBotId

    botVisualFeatures
      .get(recipientBotId)
      .map { features: BotVisualFeatures =>
        // If the deactivate visualization hasn't alrady been drawn
        if (features.inactiveShape.nonEmpty) {
      drawActive(recipientBotId)
        }
      }
  }

  def animateFatalError(fatalError: FatalErrorAnimation): Unit = {

    // First remove the bot images
    val botImage = botImages(fatalError.botId)
    val twinBotImage = twinBotImages(fatalError.botId)
    val birthBotImage = birthBotImages(fatalError.botId)

    stage.removeChild(botImage)
    stage.removeChild(twinBotImage)
    stage.removeChild(birthBotImage)

    // Second, draw a flash
    val rect = new createjs.Shape()

    val x = retina(fatalError.col * config.viz.cellSize)
    val y = retina(fatalError.row * config.viz.cellSize)
    val w = retina(config.viz.cellSize)
    val h = retina(config.viz.cellSize)
    val htmlColor = PlayerColor.toHtmlColor(fatalError.playerColor)
    rect.graphics.beginFill(htmlColor).drawRect(x, y, w, h)

    stage.addChild(rect)

    // Schedule the flash to be removed next tick
    shapesToBeRemovedNextTick +:= rect

    // Third, display the error message
    jQuery("#" + config.editor.outputId).html(fatalError.errorMessage.message)
  }

  def removeBankColor(botId: Long): Unit = {
    val circle = botVisualFeatures(botId).bankCircle.get
    botImages(botId).removeChild(circle)
    botVisualFeatures(botId).bankCircle = None
  }

  def addBankColor(botId: Long, playerColor: PlayerColor.EnumVal): Unit = {
    val circle = new createjs.Shape()

    val color = PlayerColor.toHtmlColor(playerColor)

    circle.graphics.setStrokeStyle(1).beginStroke("white").beginFill(color).drawCircle(0, 0, retina(cellSize / 6.0))
    circle.x = retina(cellSize / 2.0)
    circle.y = retina(cellSize / 2.0) + cellSize * 0.125
    botImages(botId).addChild(circle)

    botVisualFeatures(botId).bankCircle = Some(circle)
  }

  def animateBankColor(bankColorAnimation: BankColorAnimation): Unit = {
    
    val recipientBotId = bankColorAnimation.recipientBotId

    botVisualFeatures
      .get(recipientBotId)
      .foreach { features: BotVisualFeatures =>

        // First, remove the bankColor if there is one
        if (features.bankCircle.nonEmpty) {
          removeBankColor(recipientBotId)
        } 

        board
          .getBot(recipientBotId)
          .foreach { recipient : Bot =>
            // Second, maybe draw the bankColor
            if (recipient.playerColor != bankColorAnimation.playerColor) {
              addBankColor(recipientBotId, bankColorAnimation.playerColor)
            }
          }
      }
  }

  /** End animation functions  ********************************************************************/

}
