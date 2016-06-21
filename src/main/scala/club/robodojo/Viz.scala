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

  addBotImages()
  
  // animations(board cycleNum)(botId) == the animation for bot (with id == botId) at board cycleNum
  // point in time.
  val animations = HashMap[Int, HashMap[Long, Animation]]()

  // Fast forward the board, so we can begin animating. See the documentation for animateMove,
  // section (2) for an explanation.
  1 to config.viz.lookAheadCycles foreach { _ => cycle() }

  // There are three cycle counters:
  //   (1) Bots maintain their own cycle counter, which counts the number of cycles relative to a
  //       single instruction. The bot cycle counter resets to zero after an instruction is
  //       executed.
  //   (2) The board has another cycle counter, board.cycleNum, which increments after every call to
  //       cycle().
  //   (3) Yet another cycle counter is animationCycleNum, which is the cycle counter equal to the
  //       board.cycleNum we are currently animating. animationCycleNum != board.cycleNum
  //       because the animation lags behind the board simulation. See the documentation for
  //       animateMove, section (2) for an explanation.
  var animationCycleNum = 0

  stage.update()

  /** End initialization **************************************************************************/

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

    animations.keys.foreach { cycleNum =>
      animations -= cycleNum
    }

    stage.removeAllChildren()

    addBackground()
    addGrid()
    addBotImages()

    stage.update()

    1 to config.viz.lookAheadCycles foreach { _ => cycle() }

    animationCycleNum = 0
  }

  def updateMainDiv(): Unit = {
    jQuery("#" + config.id).attr("class", "robo")
  }

  def addCanvas(): JQuery = {

    val canvasHtml = s"""
      <div id="${config.viz.boardWrapperDivId}" style="margin-bottom: 10px; background-color: #ccc; border-radius: 8px; padding: 10px;">
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

    rect.graphics.beginFill("#fff").drawRect(0, 0, retina(config.viz.canvas.width),
      retina(config.viz.canvas.height))

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

  def addBotImages(): Unit = {
    board.bots.foreach { bot =>
      addBot(bot.id, bot.playerColor, bot.row, bot.col, bot.direction)
    }
  }

  def newBotContainer(
      botId: Long,
      playerColor: PlayerColor.EnumVal,
      row: Int,
      col: Int,
      direction: Direction.EnumVal): createjs.Container = {

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

    val container = new createjs.Container()

    container.addChild(bitmap)

    val halfCell = config.viz.cellSize / 2.0

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
      direction: Direction.EnumVal): Unit = {

    val twinContainer = newBotContainer(botId, playerColor, row, col, direction)
    val birthContainer = newBotContainer(botId, playerColor, row, col, direction)
    val container = newBotContainer(botId, playerColor, row, col, direction)

    twinBotImages += (botId -> twinContainer)
    birthBotImages += (botId -> birthContainer)
    botImages += (botId -> container)

    stage.addChild(twinContainer)
    stage.addChild(birthContainer)
    stage.addChild(container)

    val halfCell = config.viz.cellSize / 2.0

    // TODO: maybe this is unnecessary if we call newBotContainer with row==col==-1
    // Move the twin image off screen to (row = -1, col = -1)
    twinContainer.x = retina(halfCell - config.viz.cellSize)
    twinContainer.y = retina(halfCell - config.viz.cellSize)

    // Move birth image off screen
    birthContainer.x = retina(halfCell - config.viz.cellSize)
    birthContainer.y = retina(halfCell - config.viz.cellSize)
  }

  // TODO: do something fancier to aggregate all the animations, rather than just taking the last
  // one. Perhaps monoids?
  def cycle(): Unit = {
    val animationList = board.cycle()

    animations(board.cycleNum) = HashMap[Long, Animation]()

    // Remove obsolete animations to avoid memory leak
    // TODO: test
    animations -= board.cycleNum - config.viz.lookAheadCycles - 1

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
    
    if (createjs.Ticker.paused) {
      return
    } else {
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
  }

  def animate(): Unit = {
    // TODO: a bug reveals itself here. Sometimes, in the beginning of simualtion run,
    // animations will not have an entry for animationCycleNum, which causes an exception.
    val currentAnimations: HashMap[Long, Animation] = animations(animationCycleNum)

    currentAnimations.values.foreach { animation =>
      animation match {
        case moveAnimation: MoveAnimationProgress => animateMoveProgress(moveAnimation)
        case moveAnimation: MoveAnimationSucceed => animateMoveSucceed(moveAnimation)
        case moveAnimation: MoveAnimationFail => animateMoveFail(moveAnimation)
        case turnAnimation: TurnAnimation => animateTurn(turnAnimation)
        case birthAnimation: BirthAnimationProgress => animateBirthProgress(birthAnimation)
        case birthAnimation: BirthAnimationSucceed => animateBirthSucceed(birthAnimation)
        case birthAnimation: BirthAnimationFail => ()
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
  //     time when the move instruction executes its last cycle. If the move fails, then the bot
  //     is drawn at its current location. If the move succeeds, then we calculate
  //     proportionCompleted, which measures how far along the move instruction has progressed. Then
  //     we calculate (row, col) as a double based on proportionCompleted. For example if the bot is
  //     moving from (0, 0) to (0, 1) and the move instruction is half-way done executing, then
  //     (row, col) == (0, 0.5). Then we draw the bot at (row, col). A similar approach is used in
  //     animateTurn.
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
  def animateMoveProgress(animation: MoveAnimationProgress): Unit = 
    animateBotImageProgress(animation, botImages)


  def animateMoveSucceed(animation: MoveAnimationSucceed): Unit = ()

  def animateMoveFail(animation: MoveAnimationFail): Unit = ()

  def animateTurn(animation: TurnAnimation): Unit = {

    val oldDirection = animation.oldDirection

    // See the documentation for animateMove, section (1).
    val proportionCompleted = animation.cycleNum.toDouble / config.sim.turnCycles.toDouble

    val angle: Double = animation.leftOrRight match {
      case Direction.Left => Direction.toAngle(oldDirection) - 90.0 * proportionCompleted
      case Direction.Right => Direction.toAngle(oldDirection) + 90.0 * proportionCompleted
      case _ => throw new IllegalStateException("Bots can only turn Left or Right")
    }

    val botImage = botImages(animation.botId)
    botImage.rotation = angle
  }

  def animateBotImageProgress(
      animation: AnimationProgress,
      primaryImages: HashMap[Long, createjs.Container]): Unit = {

    // This is where we look into the future to see if the move is successful or not
    val endCycleNum = animationCycleNum + animation.requiredCycles - animation.cycleNum

    // futureAnimation == the animation for when this bot finishes executing its create instruction
    val futureAnimation = animations(endCycleNum)(animation.botId)

    // success == true iff the new bot successfully moves into its birth cell
    // TODO: check for failure, then throw execpetion if neither success nor failure
    val success = futureAnimation match {
      case m: AnimationProgressSucceed => true
      case _: AnimationProgressFail => false
      case _ => throw new IllegalStateException("This code shouldn't be reachable")
    }

    val cellSize = config.viz.cellSize
    val halfCell = cellSize / 2.0

    val twinImage = twinBotImages(animation.botId)
    val primaryImage = primaryImages(animation.botId)

    // TODO: maybe animate the bot moving forward a half cell, then moving backward a half cell?
    if (!success) {
      twinImage.x = retina(halfCell - cellSize)
      twinImage.y = retina(halfCell - cellSize)
      return
    }

    // The amount the bot has moved towards its new cell (as a proportion)
    val proportionCompleted: Double =
      animation.cycleNum.toDouble / animation.requiredCycles.toDouble

    val oldRow = animation.oldRow
    val oldCol = animation.oldCol
    val newRow = animation.newRow
    val newCol = animation.newCol

    val (twinRow: Double, twinCol: Double) =
      // if the bot has finished its movement, then move the twin off screen
      if (animation.cycleNum == animation.requiredCycles) {
        // TODO: this shouldn't happen, right?
        (-1.0, -1.0)
      }
      // if the bot is moving up, towards off the screen
      else if (newRow - oldRow > 1) {
        (newRow + 1.0 - proportionCompleted, newCol)
      }
      // if the bot is moving down, towards off the screen
      else if (oldRow - newRow > 1) {
        (newRow - 1.0 + proportionCompleted, newCol)
      }
      // if the bot is moving left, towards off the screen
      else if (newCol - oldCol > 1) {
        (newRow, newCol + 1.0 - proportionCompleted)
      }
      // if the bot is moving right, towards off the screen
      else if (oldCol - newCol > 1) {
        (newRow, newCol - 1.0 + proportionCompleted)
      }
      // if the bot isn't wrapping around the screen
      else  {
        (-1.0, -1.0)
      }

    val (primaryRow: Double, primaryCol: Double) =
      // if the bot has finished its movement, then move the bot to its new home
      if (animation.cycleNum == animation.requiredCycles) {
        (newRow, newCol)
      }
      // if the bot is moving up, towards off the screen
      else if (newRow - oldRow > 1) {
        (oldRow - proportionCompleted, oldCol)
      }
      // if the bot is moving down, towards off the screen
      else if (oldRow - newRow > 1) {
        (oldRow + proportionCompleted, oldCol)
      }
      // if the bot is moving left, towards off the screen
      else if (newCol - oldCol > 1) {
        (oldRow, oldCol - proportionCompleted)
      }
      // if the bot is moving right, towards off the screen
      else if (oldCol - newCol > 1) {
        (oldRow, oldCol + proportionCompleted)
      }
      // the bot is moving up
      else if (newRow < oldRow) {
        (oldRow - proportionCompleted, oldCol)
      }
      // the bot is moving down
      else if (newRow > oldRow) {
        (oldRow + proportionCompleted, oldCol)
      }
      // the bot is moving left
      else if (newCol < oldCol) {
        (oldRow, oldCol - proportionCompleted)
      }
      // the bot is moving right
      else if (newCol > oldCol) {
        (oldRow, oldCol + proportionCompleted)
      } else {
        throw new IllegalStateException("This code shouldn't be reachable")
      }


    twinImage.x = retina(halfCell + cellSize * twinCol)
    twinImage.y = retina(halfCell + cellSize * twinRow)
    twinImage.rotation = Direction.toAngle(animation.direction)

    primaryImage.x = retina(halfCell + cellSize * primaryCol)
    primaryImage.y = retina(halfCell + cellSize * primaryRow)
    primaryImage.rotation = Direction.toAngle(animation.direction)

  }

  // TODO: factor out code common to animateMove
  // TODO: document
  def animateBirthProgress(animation: BirthAnimationProgress): Unit =
    animateBotImageProgress(animation, birthBotImages)

  // TODO: cleanup
  def animateBirthSucceed(animation: BirthAnimationSucceed): Unit = {
    addBot(animation.newBotId,
      animation.playerColor,
      animation.row,
      animation.col,
      animation.direction)

    val birthImage = birthBotImages(animation.botId)
    val cellSize = config.viz.cellSize
    val halfCell = cellSize / 2.0
    birthImage.x = retina(halfCell - cellSize)
    birthImage.y = retina(halfCell - cellSize)

    val twinImage = twinBotImages(animation.botId)
    twinImage.x = retina(halfCell - cellSize)
    twinImage.y = retina(halfCell - cellSize)
  }

}
