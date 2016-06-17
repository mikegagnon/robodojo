package club.robodojo

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs
import scala.collection.mutable.ArrayBuffer

// TODO: update this
// App deals with all Robodojo instances for a given html page. Here is a demo of how you
// can instantiate multiple Robodojo instances:
//
//    <div id="robo1"></div>
//
//    <div id="robo2"></div>
//
//    <script type="text/javascript">
//      var app = club.robodojo.App()
//
//     app.newRobobot({
//        "id": "robo1",
//        "sim.numRows": 10,
//        "sim.numCols": 10,
//        "viz.cellSize": 32
//      })
//
//      app.newRobobot({
//        "id": "robo2",
//        "viz.cellSize": 16
//      })
//
//      app.launch()
//
//    </script>
//
object App extends JSApp {

  var configs = new ArrayBuffer[Config]()
  
  var activeInstanceId: Option[String] = None

  // instances(instanceId) == instance of Robodojo
  var instances = Map[String, Robodojo]()

  @JSExport
  def newRobodojo(configJS: js.Dictionary[Any]): Unit = {

    val config = new Config(configJS.toMap)
    configs += config

    // The id of the first Robodojo instantiation goes to activeInstanceId
    activeInstanceId = Some(activeInstanceId.getOrElse(config.id))
  }

  @JSExport
  def clickPlay(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickPlay()
  }

  @JSExport
  def clickPause(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickPause()
  }

  @JSExport
  def clickStep(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickStep()
  }

  def initializeTicker(): Unit = {
    val config = instances(activeInstanceId.get).config
    createjs.Ticker.addEventListener("tick", tick _)
    createjs.Ticker.setFPS(config.viz.framesPerSecond)
    createjs.Ticker.paused = true
  }

  // TODO: check for paused
  def tick(event: js.Dynamic): Boolean = {
    val rd = instances(activeInstanceId.get)
    rd.viz.tick(event)
    return true
  }

  // launch() uses createjs's preloading system to load all our images, then block once the loading
  // is complete.
  @JSExport
  def launch(): Unit = {

    val preload = new createjs.LoadQueue()

    //http://stackoverflow.com/questions/24827965/preloadjs-isnt-loading-images-bitmaps-correctly
    preload.setUseXHR(false)

    preload.on("complete", handleComplete _ , this)

    // TODO: add error checking, etc.
    def handleComplete(obj: Object): Boolean = {
      configs.foreach { config =>
        instances += (config.id -> new Robodojo(preload)(config))
      }

      initializeTicker()

      return true
    }

    val manifest = js.Array(      
      js.Dynamic.literal(
        id = configs(0).viz.preload.blueBotId,
        src = configs(0).viz.preload.blueBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.redBotId,
        src = configs(0).viz.preload.redBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.greenBotId,
        src = configs(0).viz.preload.greenBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.yellowBotId,
        src = configs(0).viz.preload.yellowBotPath
      )
    )

    preload.loadManifest(manifest)

  }

  def main(): Unit = {}

}