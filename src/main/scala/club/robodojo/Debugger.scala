package club.robodojo

import org.denigma.codemirror.Position
import org.scalajs.jquery.jQuery
import scala.scalajs.js

class Debugger(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val cmEditor = CodeMirrorDojo.getCmEditor(true, config.debugger.textAreaId)

  jQuery(s"#${config.debugger.divId} .CodeMirror").css("font-size", config.debugger.fontSize)

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    val html = s"""
      <div class="window" id="${config.debugger.divId}">
        <div class="dark-border light-background">
          <div class="code-mirror-div">
            <textarea id='${config.debugger.textAreaId}'></textarea>
          </div>
          <div id='${config.debugger.outputId}' class="compiler-output">
          </div>
          <div style='clear: both;'></div>
        </div> <!-- end codemirror -->
      </div>
      """

    jQuery("#" + config.id).append(html)
  }

  def onBotClick(botId: Long): Unit = {
    setupDebugger(botId: Long)
  }

  // What line does instruction X appear in the debugger window? Where X is
  // the instruction from instructionIndex, within bank bankIndex
  def getLineIndex(bot: Bot, bankIndex: Int, instructionIndex: Int): Int = {
    val banks = bot.program.banks

    val previousBanksLength =
      (0 until bankIndex)
        .map { i =>
          val bank = banks(i)
          val sourceMap = bank.sourceMap.get
          sourceMap.text.length + 1
        }
        .sum

    val instruction = banks(bankIndex).instructions(instructionIndex)
    val lineIndex = instruction.sourceMapInstruction.lineIndex

    previousBanksLength + lineIndex + 1
  }

  def getProgramText(botId: Long): String = {

    val bot: Bot = viz.board.getBot(botId).get

    val banks = bot.program.banks

    (0 until banks.size)
      .map { bankIndex =>
        val bank = banks(bankIndex)
        val sourceMap = bank.sourceMap.get
        val playerColor = sourceMap.playerColor
        val origBankIndex = sourceMap.bankIndex
        s"; Bank #${bankIndex + 1} = ${playerColor} Bank #${origBankIndex + 1}\n" +
        sourceMap.text.mkString("\n")
      }
      .mkString("\n")
  }

  def setupDebugger(botId: Long): Unit = {
    val programText = getProgramText(botId)
    cmEditor.getDoc().setValue(programText)

    val bot: Bot = viz.board.getBot(botId).get

    // TODO: deal with this temporary hack
    val lineIndex = getLineIndex(bot, 0, 3)

    val handle = cmEditor.getDoc().getLineHandle(lineIndex)
    cmEditor.addLineClass(handle, "background", "line-highlight")

    // TODO: this is just a demo
    // TODO: instead of 5, do half of num-lines-in-cmeditor-window
    val pos = js.Dynamic.literal(line = lineIndex + 5, ch = 0).asInstanceOf[Position]

    cmEditor.getDoc().setCursor(pos)
  }
}
