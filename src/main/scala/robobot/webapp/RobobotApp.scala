package robobot.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js

object RobobotApp extends JSApp {

  // TODO: is this really needed?
  var instances = Map[String, Robobot]()

  @JSExport
  def newRobobot(configJS: js.Dictionary[Any]) = {

    val config = new Config(configJS.toMap)

    instances += (config.id -> new Robobot(config))
  }

  def main(): Unit = {}

}