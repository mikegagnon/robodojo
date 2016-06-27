package club.robodojo

import org.scalajs.jquery.jQuery

class Debugger(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    val html = s"""
      <div class="debugger">
      </div>
      """

    jQuery("#" + config.id).append(html)
  }

}
