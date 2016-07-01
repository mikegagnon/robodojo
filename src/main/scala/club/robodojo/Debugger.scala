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

  // TODO: only update the debugger window if the debugger is open
  def tick(): Unit = {
    botIdDebugged.map { botId =>
      setupDebugger(botId)
    }
  }

  // What line does instruction X appear in the debugger window? Where X is
  // the instruction from instructionIndex, within bank bankIndex
  // TODO: Deal with empty banks
  def getLineIndex(bot: Bot): Int = {

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

    previousBanksLength + lineIndex + 1
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
            val body = sourceMap
              .text
              .zipWithIndex
              .map { case (line: String, index: Int) =>

                val requiredCycles =
                  if (bot.requiredCycles == 0) {
                    bot.program.banks(0).instructions(0).getRequiredCycles(bot)
                  } else {
                    bot.requiredCycles
                  }

                if (index + 1 == lineIndex) {
                  line + s" ; cycle ${bot.cycleNum} / ${requiredCycles}"
                } else {
                  line
                }
              }
              .mkString("\n")

            header + body
          }
        }
      }
      .mkString("\n")
  }

  // TODO: better styling?
  def setupMicroscope(bot: Bot): Unit = {

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

    val html = registersHtml + specialParamsHtml

    jQuery("#" + config.debugger.outputId).html(html)
  }

  def setupDebugger(botId: Long): Unit = {

    botIdDebugged = Some(botId)

    val bot = getBot(botId)

    val lineIndex = getLineIndex(bot)

    val programText = getProgramText(bot, lineIndex)
    cmEditor.getDoc().setValue(programText)

    val handle = cmEditor.getDoc().getLineHandle(lineIndex)
    cmEditor.addLineClass(handle, "background", "line-highlight")

    // TODO: this is just a demo
    // TODO: instead of 5, do half of num-lines-in-cmeditor-window
    // Scroll down to the highlighted line
    val pos = js.Dynamic.literal(line = lineIndex + 5, ch = 0).asInstanceOf[Position]

    cmEditor.getDoc().setCursor(pos)

    setupMicroscope(bot)
  }
}
