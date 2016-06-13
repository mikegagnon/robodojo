package robobot.webapp

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs


class Controller(val config: Config, val board: Board, val viz: Viz) {

  addConsole()
  addPlayButton()

  def addConsole(): Unit = {
    val consoleDiv = jQuery(s"<div id='${config.viz.consoleDivId}'></div>")

    jQuery("#" + config.id).append(consoleDiv)
  }

  def addPlayButton(): Unit = {
    jQuery("#" + config.viz.consoleDivId).append(s"""
      <button onclick='robobot.webapp.RobobotApp().clickPlay("${config.id}")'>
        <span class='glyphicon glyphicon-play'></span>
      </button>""")
  }

  def clickPlay(): Unit = {
    println("clickPlay inside " + config.id)

    // HACK: The proper thing to do here is add an event listener to Ticker by passing in a listener
    // as a function. However, that API call in scalajs isn't available in the current version of
    // scalajs-createjs. See: https://github.com/scalawarrior/scalajs-createjs/blob/2aeec181b8307f2687aff83c2311ed8589f140e3/src/main/scala/com/scalawarrior/scalajs/createjs/EaselJS.scala#L701
    // So, to get around this limitation, we pass the viz.stage object, which results in the ticker
    // calling viz.stage.handleEvent. So, we override handleEvent with our own method, which should
    // work just fine.
    viz.stage.handleEvent = tick _
    createjs.Ticker.addEventListener("tick", viz.stage)
    createjs.Ticker.setFPS(config.viz.framesPerSecond)
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

    // TODO: tersify?
    animations.foreach { animation =>
      animation match {
        case MoveAnimation(bot, row, col) => {
          viz.bots(bot.id).x = viz.retina((config.viz.cellSize / 2 + config.viz.cellSize * col))
          viz.bots(bot.id).y = viz.retina((config.viz.cellSize / 2 + config.viz.cellSize * row))
          viz.bots(bot.id).rotation = Direction.toAngle(bot.direction)
        }
        case TurnAnimation(bot, angle) => {
          viz.bots(bot.id).rotation = angle
        }
      }
    }

    viz.stage.update()

    println(cycles)

  }

}