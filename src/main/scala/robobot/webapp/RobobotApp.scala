package robobot.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs
import scala.collection.mutable.ArrayBuffer

object RobobotApp extends JSApp {

  var configs = new ArrayBuffer[Config]()
  var instances = Map[String, Robobot]()

  @JSExport
  def newRobobot(configJS: js.Dictionary[Any]) = {
    configs += new Config(configJS.toMap)
  }

  @JSExport
  def clickPlay(id: String) {
    val robobot = instances(id)

    robobot.controller.clickPlay
  }

  @JSExport
  def launch() {

    val preload = new createjs.LoadQueue()

    //http://stackoverflow.com/questions/24827965/preloadjs-isnt-loading-images-bitmaps-correctly
    preload.setUseXHR(false)

    preload.on("complete", handleComplete _ , this)

    // TODO: add error checking, etc.
    def handleComplete(obj: Object): Boolean = {
      configs.foreach { config =>
        instances += (config.id -> new Robobot(preload)(config))
      }
      return true
    }

    val manifest = js.Array(
      js.Dynamic.literal(
        id = "blueBotImage",
        src = "./img/bluebot.png"
      )
    )

    preload.loadManifest(manifest)

  }

  def main(): Unit = {}

}