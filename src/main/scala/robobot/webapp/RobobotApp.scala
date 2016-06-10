package robobot.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js

object RobobotApp extends JSApp {

  var instances = Map[String, Robobot]()

  @JSExport
  def newRobobot(configJS: js.Dictionary[Any]) = {

    implicit val config = new Config(configJS.toMap)

    instances += (config.id -> new Robobot())
  }

  @JSExport
  def clickPlay(id: String) {
    val robobot = instances(id)

    robobot.controller.clickPlay
  }

  def main(): Unit = {}

}