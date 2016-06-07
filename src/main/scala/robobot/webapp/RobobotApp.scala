package robobot.webapp

import scala.scalajs.js.JSApp

object RobobotApp extends JSApp {
  def main(): Unit = {
    
    val robobot = new Robobot(ConfigPrimary)
    robobot.viz.init

  }
}