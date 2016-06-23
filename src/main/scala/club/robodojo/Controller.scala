package club.robodojo

import org.scalajs.jquery.jQuery
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs

class Controller(val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val editor = new Editor(this, viz)

  /** End initialization **************************************************************************/

  def buttonHtml(functionName: String, glyph: String): String = 
    s"""
      <button class="btn btn-default dark-border" onclick='club.robodojo.App().${functionName}("${config.id}")'>
        <span class='glyphicon glyphicon-${glyph}'></span>
      </button>"""

  def addHtml(): Unit = {
    val html = s"""
      <div id='${config.viz.consoleDivId}'>
        ${buttonHtml("clickPlay", "play")}
        ${buttonHtml("clickPause", "pause")}
        ${buttonHtml("clickStep", "step-forward")}
      </div>
      """
    jQuery("#" + config.viz.boardWrapperDivId).append(html)
  }

  def clickPlay(): Unit = {
    viz.step = false
    createjs.Ticker.paused = false
  }

  def clickPause(): Unit = {
    createjs.Ticker.paused = true
  }

  def clickStep(): Unit =
    if (createjs.Ticker.paused) {
      viz.step = true
      createjs.Ticker.paused = false
    } else {
      clickPause()
    }

}