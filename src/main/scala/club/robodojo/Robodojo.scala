package club.robodojo

import com.scalawarrior.scalajs.createjs

import scala.util.Random

class Robodojo(preload: createjs.LoadQueue)(implicit val config: Config) {
  val board = new Board()
  
  val viz = new Viz(preload, board)

  val controller = new Controller(viz)

  viz.init(controller)

}