package robobot.webapp

class Controller(val config: Config, val board: Board, val viz: Viz) {

  def clickPlay {
    println("clickPlay inside " + config.id)
  }

}