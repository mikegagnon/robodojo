package club.robodojo

import org.denigma.codemirror.Position
import org.scalajs.jquery.jQuery
import scala.scalajs.js

class Debugger(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val cmEditor = CodeMirrorDojo.getCmEditor(true, config.debugger.textAreaId)

  jQuery(s"#${config.debugger.divId} .CodeMirror").css("font-size", config.debugger.fontSize)
  jQuery(s"#${config.debugger.outputId}").css("font-size", config.debugger.fontSize)

  // If we set display:none in addHtml, then the line gutter doesn't appear.
  // Perhaps, it inherits the display:none from the debugger.divId-window.
  // Either way, the problem goes away by configuring the debugger window, and then
  // setting display:none
  jQuery("#" + config.debugger.divId).css("display", "none")

  // the bot that is being scrutinized
  var botIdDebugged: Option[Long]= None

  /** End initialization **************************************************************************/

  def reset(): Unit = {
    botIdDebugged = None
    cmEditor.getDoc().setValue("")
    jQuery("#" + config.debugger.outputId).html("")
  }

  def addHtml(): Unit = {

    val html = s"""
      <div class="window" id="${config.debugger.divId}">
        <span class="window-name">Debugger</span>
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

    // Show the debugger
    jQuery("#" + config.debugger.divId).css("display", "block")

    setupDebugger(botId: Long)
  }

  // TODO: only update the debugger window if the debugger is open
  def tick(): Unit = {
    botIdDebugged.map { botId =>
      setupDebugger(botId)
    }
  }

  // What line does instruction X appear in the debugger window? Where X is
  // the instruction from instructionIndex, within bank bankIndex
  // TODO: Deal with empty banks
  def getLineIndex(bot: Bot): Option[Int] = {

    if (bot.program.banks(bot.bankIndex).instructions.length == 0) {
      return None
    }

    val bankIndex = bot.bankIndex
    val instructionIndex = bot.instructionIndex
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

    Some(previousBanksLength + lineIndex + 1)
  }

  // get the bot at animationCycleNum point in time
  def getBot(botId: Long) = viz.boards(viz.animationCycleNum).getBot(botId).get

  def getProgramText(bot: Bot, lineIndex: Int): String = {

    val banks = bot.program.banks

    (0 until banks.size)
      .map { bankIndex =>
        val bank = banks(bankIndex)
        bank.sourceMap match {
          case None => {
            if (bank.instructions.length != 0) {
              throw new IllegalStateException("If sourceMap is none, then instructions should be " +
                "empty")
            }
            s"; Bank #${bankIndex + 1} = empty bank"
          }
          case Some(sourceMap) => {
            val playerColor = sourceMap.playerColor
            val origBankIndex = sourceMap.bankIndex
            val header = s"; Bank #${bankIndex + 1} = ${playerColor} Bank #${origBankIndex + 1}\n"
            val body = sourceMap.text.mkString("\n")

            header + body
          }
        }
      }
      .mkString("\n")
  }

  // TODO: better styling?
  def setupMicroscope(bot: Bot): Unit = {

    val requiredCycles =
      // bot.requiredCycles == 0 indicates the simulation hasn't begun
      if (bot.program.banks(0).instructions.length == 0) {
        0
      } else if (bot.requiredCycles == 0) {
        bot.program.banks(0).instructions(0).getRequiredCycles(bot)
      } else {
        bot.requiredCycles
      }

    val cycleCountHtml = s"""
      <div class='microscope-header'>Cycle counter</div>
      <span class='microscope-element'><span class='microscope-name'>Cycle</span> ${bot.cycleNum} / ${requiredCycles}</span>
      """

    val registersHtml = s"""
      <div class='microscope-header'>Registers</div>
      """ +
      bot
        .registers
        .zipWithIndex
        .map { case (regiserValue, registerIndex) =>
          s"<span class='microscope-element'><span class='microscope-name'>#${registerIndex +1}</span> = ${regiserValue}</span>"
        }
        .mkString("\n")

    val remoteBot = bot.getRemote

    val specialParams = List(
        ("#active", bot.active),
        ("%active", remoteBot.map{ _.active }.getOrElse(0)),
        ("$banks", bot.program.banks.size),
        ("%banks", remoteBot.map{ _.program.banks.size }.getOrElse(0)),
        ("$instrset", bot.instructionSet.value),
        ("%instrset", remoteBot.map{ _.instructionSet.value }.getOrElse(0)),
        ("$mobile", if (bot.mobile) 1 else 0 ),
        ("%mobile", if (remoteBot.map{ _.mobile }.getOrElse(false)) 1 else 0 ),
        ("$fields", config.sim.numRows.toShort)
      )

    val specialParamsHtml =

      "<div class='microscope-header'>Special values</div>" +
      specialParams.map{ case (name: String, value: Short) =>
        s"""<span class='microscope-element'><span class='microscope-name'>${name}</span> = ${value}</span>"""
      }
      .mkString("\n")

    val html = cycleCountHtml + registersHtml + specialParamsHtml

    jQuery("#" + config.debugger.outputId).html(html)
  }

  def setupDebugger(botId: Long): Unit = {

    botIdDebugged = Some(botId)

    val bot = getBot(botId)
    val lineIndex: Option[Int] = getLineIndex(bot)
    val programText = getProgramText(bot, lineIndex.getOrElse(0))
    cmEditor.getDoc().setValue(programText)
    println(lineIndex)

    lineIndex.foreach { l: Int =>
      val handle = cmEditor.getDoc().getLineHandle(l)
      cmEditor.addLineClass(handle, "background", "line-highlight")

      val pos = js.Dynamic.literal(line = l + 5, ch = 0).asInstanceOf[Position]
      cmEditor.getDoc().setCursor(pos)
    }

    setupMicroscope(bot)
  }
}
