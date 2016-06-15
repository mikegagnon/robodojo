package robobot.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs
import scala.collection.mutable.ArrayBuffer

// Deals with all robobot instances from a given html page
object RobobotApp extends JSApp {

  var configs = new ArrayBuffer[Config]()
  
  var activeInstanceId: Option[String] = None
  var instances = Map[String, Robobot]()

  // Set to true once the Ticker has been initialized
  var initTicker = false

  @JSExport
  def newRobobot(configJS: js.Dictionary[Any]): Unit = {

    val config = new Config(configJS.toMap)
    configs += config

    // The id of the first robobot instantiation goes to activeInstanceId
    activeInstanceId = Some(activeInstanceId.getOrElse(config.id))

  }

  // TODO
  @JSExport
  def clickPlay(id: String) {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickPlay()
  }

  @JSExport
  def clickPause(id: String) {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickPause()
  }

  @JSExport
  def clickStep(id: String) {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickStep()
  }

  def initializeTicker(): Unit =
    if (!initTicker) {
      val config = instances(activeInstanceId.get).config
      createjs.Ticker.addEventListener("tick", tick _)
      createjs.Ticker.setFPS(config.viz.framesPerSecond)
      createjs.Ticker.paused = true
    }

  def tick(event: js.Dynamic): Boolean = {
    val robobot = instances(activeInstanceId.get)
    robobot.viz.tick(event)
    return true
  }

  @JSExport
  def launch() {

    // TODO: factor our preload code intp separate function
    val preload = new createjs.LoadQueue()

    //http://stackoverflow.com/questions/24827965/preloadjs-isnt-loading-images-bitmaps-correctly
    preload.setUseXHR(false)

    preload.on("complete", handleComplete _ , this)

    // TODO: add error checking, etc.
    def handleComplete(obj: Object): Boolean = {
      configs.foreach { config =>
        instances += (config.id -> new Robobot(preload)(config))
      }

      initializeTicker()

      return true
    }

    val manifest = js.Array(
      js.Dynamic.literal(
        id = configs(0).viz.preload.blueBotId,
        src = configs(0).viz.preload.blueBotPath
      )
    )

    preload.loadManifest(manifest)

  }

  def main(): Unit = {}

}