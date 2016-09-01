package club.robodojo

// TODO: only execute instructions if the bot has the proper instruction set
//
// There are two instruction sets: Basic and extended. Each instruction is associated with one
// of those instruction sets. Each bot is also associated with an instruction set.
// A basic-bot may only execute basic instructions, whereas an extended-bot may execute any
// instruction.
object InstructionSet {

  sealed trait EnumVal {
    val value: Short
    val str: String
  }

  case object Basic extends EnumVal {
    val value: Short = 0
    val str: String = "Basic"
  }

  case object Advanced extends EnumVal {
    val value: Short = 1
    val str: String = "Advanced"
  }

  case object Super extends EnumVal {
    val value: Short = 2
    val str: String = "Super"
  }

  def fromInt(value: Int): InstructionSet.EnumVal =
    if (value == 0) {
      InstructionSet.Basic
    } else if (value == 1) {
      InstructionSet.Advanced
    } else if (value == 2) {
      InstructionSet.Super
    } else {
      throw new IllegalArgumentException("Bad value: " + value)
    }
}

// Instruction instances must be stateless, so multiple bots can execute the same instruction
// at the same time.
sealed abstract class Instruction {

  val sourceMapInstruction: SourceMapInstruction

  val instructionSet: InstructionSet.EnumVal

  // TODO: foreach concrete subclass, factor into account remote penalty
  def getRequiredCycles(bot: Bot): Int

  // TODO: document FAT hack and this work around
  def cycle(bot: Bot, cycleNum: Int): Option[Animation] = {

    if (cycleNum >= bot.requiredCycles) {
      bot.requiredCycles = getRequiredCycles(bot)
    }

    if (cycleNum >= bot.requiredCycles) {
      return execute(bot)
    } else {
      return progress(bot, cycleNum)
    }

  }

  def checkInstructionSet(bot: Bot, instructionName: String, lineIndex: Int, playerColor: PlayerColor.EnumVal): Option[Animation] = {

    if (bot.instructionSet.value < instructionSet.value) {

      val message = s"<p><span class='display-failure'>Error at line ${lineIndex + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it only has the " +
        s"<tt>${bot.instructionSet.str}</tt> instruction set, but it needs the " +
        s"<tt>${instructionSet.str}</tt> instruction set to execute the " +
        s"<tt>${instructionName}</tt> instruction."

      val errorCode = ErrorCode.InsufficientInstructionSet

      val errorMessage = ErrorMessage(errorCode, lineIndex, message)
      Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
    } else {
      None
    }

  }

  // Execute the instruction
  def execute(bot: Bot): Option[Animation]

  // Make progress toward reaching requiredCycles
  def progress(bot: Bot, cycleNum: Int): Option[Animation]
}

/* Begin param values *****************************************************************************/

sealed trait Param

sealed trait ReadableParam extends Param {
  // local == true iff the read is local
  def local: Boolean
  def read(bot: Bot): Short
}

sealed trait WriteableParam extends ReadableParam {
  def local: Boolean
  def write(bot: Bot, value: Short): Option[Animation]
}

// includes @ prefix
final case class LabelParam(label: String) extends Param

/* Begin KeywordParam values **********************************************************************/

// Encompasses #Active, %Active, $Banks, ... Anything with a keyword parameter name.
sealed trait KeywordParam extends Param 

sealed trait ReadableKeyword extends KeywordParam with ReadableParam

sealed trait WriteableKeyword extends KeywordParam with WriteableParam

sealed trait ReadableFromBot extends ReadableKeyword {

  def read(bot: Bot): Short =
    if (local) {
      readFromBot(bot)
    } else {
      bot.getRemote
          .map{ remoteBot => readFromBot(remoteBot) }
          .getOrElse(0)
    }

  def readFromBot(bot: Bot): Short
}

object ActiveKeyword {

  def writeTo(bot: Bot, value: Short, writerBotId: Long, recipientBotId: Long): Option[Animation]= {
    val oldActive = bot.active
    bot.active = value
    if (bot.active < 1 && oldActive >= 1) {
      Some(DeactivateAnimation(writerBotId, recipientBotId))
    } else if (bot.active >= 1 && oldActive < 1) {
      Some(ActivateAnimation(writerBotId, recipientBotId))
    } else {
      None
    }
  }
}

case class ActiveKeyword(local: Boolean)(implicit config: Config) extends WriteableKeyword
    with ReadableFromBot {

  def readFromBot(bot: Bot): Short = bot.active

  override def write(bot: Bot, value: Short): Option[Animation] =
    if (local) {
      ActiveKeyword.writeTo(bot, value, bot.id, bot.id)
    } else {
      bot
        .getRemote
        .flatMap { remoteBot =>
          ActiveKeyword.writeTo(remoteBot, value, bot.id, remoteBot.id)
        }
    }
}

case class BanksKeyword(local: Boolean)(implicit config: Config) extends ReadableFromBot {
  def readFromBot(bot: Bot): Short = bot.program.banks.size.toShort
}

case class InstrSetKeyword(local: Boolean) extends ReadableFromBot {
  def readFromBot(bot: Bot): Short = bot.instructionSet.value
}

// TODO: is there a better place to put this?
object Mobile {
  def fromInt(value: Int): Boolean =
    if (value == 0) {
      false
    } else if (value == 1) {
      true
    } else {
      throw new IllegalArgumentException("Bad value")
    }
}

case class MobileKeyword(local: Boolean) extends ReadableFromBot {
  def readFromBot(bot: Bot): Short = if (bot.mobile) 1 else 0
}

case class FieldsKeyword()(implicit config: Config) extends ReadableKeyword {
  val local = true
  def read(bot: Bot): Short = config.sim.numRows.toShort
}

/* End KeywordParam values **********************************************************************/

final case class IntegerParam(value: Short) extends ReadableParam {
  val local = true
  def read(bot: Bot): Short = value
}

final case class RegisterParam(registerIndex: Int)(implicit config: Config)
    extends ReadableParam with WriteableParam {

  val local = true

  if (registerIndex < 0 || registerIndex >= config.sim.maxNumVariables) {
    throw new IllegalArgumentException("Register num out of range: " + registerIndex)
  }

  def read(bot: Bot) = bot.registers(registerIndex)

  def write(bot: Bot, value: Short): Option[Animation] = {
    bot.registers(registerIndex) = value
    return None
  }

}

/* End param values *******************************************************************************/

/* Begin instructions *****************************************************************************/


case class MoveInstruction(
    sourceMapInstruction: SourceMapInstruction,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durMove

  // TESTED
  def execute(bot: Bot): Option[Animation] = {

    if (!bot.mobile) {
        val message = s"<p><span class='display-failure'>Error at line ${lineIndex + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it tried to move, but it is an " +
        s"immobile bot."

      val errorCode = ErrorCode.CannotMoveImmobile
      val errorMessage = ErrorMessage(errorCode, lineIndex, message)
      
      bot.board.removeBot(bot)

      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
    }


    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        bot.board.moveBot(bot, row, col)
        Some(MoveAnimationSucceed(bot.id, row, col, bot.direction))
      }
      case Some(_) => Some(MoveAnimationFail(bot.id))
    }
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = {
    val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    return Some(MoveAnimationProgress(
      bot.id,
      cycleNum,
      bot.requiredCycles,
      bot.row,
      bot.col,
      destRow,
      destCol,
      bot.direction))
  }
}

case class TurnInstruction(sourceMapInstruction: SourceMapInstruction, leftOrRight: ReadableParam)(implicit val config: Config) extends Instruction {

    val instructionSet = InstructionSet.Basic

    def getRequiredCycles(bot: Bot): Int = {

      val remoteReadCost = if (leftOrRight.local) 0 else config.sim.cycleCount.durRemoteAccessCost

      return config.sim.cycleCount.durTurn + remoteReadCost
    }

    def getNewDirection(currentDir: Direction.EnumVal, bot: Bot): Direction.EnumVal =
      if (leftOrRight.read(bot) == 0) {
        Direction.rotateLeft(currentDir)
      } else {
        Direction.rotateRight(currentDir)
      }

    def toDirection(bot: Bot): Direction.EnumVal =
      if (leftOrRight.read(bot) == 0) {
        Direction.Left
      } else {
        Direction.Right
      }

    def execute(bot: Bot): Option[Animation] = {
      val oldDirection = bot.direction
      bot.direction = getNewDirection(bot.direction, bot)
      Some(TurnAnimationFinish(bot.id, bot.direction))
    }

    def progress(bot: Bot, cycleNum: Int): Option[Animation] =
      Some(TurnAnimationProgress(bot.id, cycleNum, bot.direction, toDirection(bot)))
}

// TODO: Prevent FAT hack
// TODO: make compliant with Robocom standard: generation, maxGeneration, and maxNumBots?
case class CreateInstruction(
    sourceMapInstruction: SourceMapInstruction,
    childInstructionSet: ReadableParam,
    numBanks: ReadableParam,
    mobile: ReadableParam,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    // Note this is different than bot.playerColor, which is the color of the bot.
    // For example, if a blue bot infects a red bot with a bank that has as bad create instruction,
    // then bot.playerColor == red and playerColor, here, == blue.
    playerColor: PlayerColor.EnumVal)(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Super

  def getRequiredCycles(bot: Bot): Int = {

    val childInstructionSetValue = childInstructionSet.read(bot)
    val numBanksValue = numBanks.read(bot)
    val mobileValue = mobile.read(bot)

    val remoteCost1 = if (childInstructionSet.local) {
        0
      } else {
        config.sim.cycleCount.durRemoteAccessCost
      }
    val remoteCost2 = if (numBanks.local) 0 else config.sim.cycleCount.durRemoteAccessCost
    val remoteCost3 = if (mobile.local) 0 else config.sim.cycleCount.durRemoteAccessCost

    val durCreate1 = config.sim.cycleCount.durCreate1
    val durCreate2 = config.sim.cycleCount.durCreate2
    val durCreate3 = config.sim.cycleCount.durCreate3
    val durCreate3a = config.sim.cycleCount.durCreate3a
    val durCreate4 = config.sim.cycleCount.durCreate4
    val durCreate5 = config.sim.cycleCount.durCreate5
    val maxCreateDur = config.sim.cycleCount.maxCreateDur

    val primary = durCreate1 + durCreate2 * numBanksValue
    val mobilityCost = if (mobileValue > 0) durCreate3 else 1
    val secondaryMobilityCost = if (mobileValue > 0) durCreate3a else 0

    val instructionSetCostAdvanced = childInstructionSetValue match {
      case InstructionSet.Advanced.value => durCreate4
      case _ => 0
    }

    val instructionSetCostSuper = childInstructionSetValue match {
      case InstructionSet.Super.value => durCreate5
      case _ => 0
    }

    val calculatedCost =
      primary * mobilityCost +
      secondaryMobilityCost +
      instructionSetCostAdvanced +
      instructionSetCostSuper +
      remoteCost1 +
      remoteCost2 +
      remoteCost3

    // We use max and min here because numBanks, childInstructionSet, and mobile might have wonky
    // values
    return Math.max(Math.min(calculatedCost, maxCreateDur), 1)
  }

  // TODO: cleanup
  def errorCheck(
      bot: Bot,
      childInstructionSetValue: Short,
      numBanksValue: Short,
      mobileValue: Short): Option[Animation] = {

    val insufficientInstructionSet = checkInstructionSet(bot, "create", lineIndex, playerColor)

    val errorCode = ErrorCode.InvalidParameter

    // TODO: factor out common code
    val messageHeader = s"<p><span class='display-failure'>Error at line ${lineIndex + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it attempted to " +
        s"execute a <tt>create</tt> instruction with "

    if (childInstructionSetValue < 0 || childInstructionSetValue > 2) {

      val message = messageHeader + s"<tt>childInstructionSet</tt> equal to " +
        s"${childInstructionSetValue}. <tt>childInstructionSet</tt> must be either 0 (signifying " +
        s"the Basic instruction set) or 1 (signifying the Advanced instruction set) or 2 " +
        s"(signifying the Super instruction set).</p>"
      val errorMessage = ErrorMessage(errorCode, lineIndex, message)
      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))

    } else if (numBanksValue <= 0 || numBanksValue > config.sim.maxBanks) {

      val message = messageHeader + s"<tt>numBanks</tt> equal to ${numBanksValue}. "+
        s"<tt>numBanks</tt> must be greater than 0 and less than (or equal to) " +
        s"${config.sim.maxBanks}.</p>"
      val errorMessage = ErrorMessage(errorCode, lineIndex, message)
      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))

    } else if (mobileValue < 0 || mobileValue > 1) {

      val message = messageHeader + s"<tt>mobile</tt> equal to " +
        s"${mobileValue}. <tt>mobile</tt> must be either 0 (signifying " +
        s"immobility) or 1 (signifying mobility).</p>"
      val errorMessage = ErrorMessage(errorCode, lineIndex, message)
      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))

    } else if (insufficientInstructionSet.nonEmpty) {
      insufficientInstructionSet
    } else {
      None
    }
  }

  def execute(bot: Bot): Option[Animation] = {

    val childInstructionSetValue: Short = childInstructionSet.read(bot)
    val numBanksValue: Short = numBanks.read(bot)
    val mobileValue: Short = mobile.read(bot)

    val error: Option[Animation] = errorCheck(
      bot,
      childInstructionSetValue,
      numBanksValue,
      mobileValue)

    if (error.nonEmpty) {
      bot.board.removeBot(bot)
      return error
    }

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        val emptyProgram = Program.emptyProgram(numBanksValue)

        val active: Short = 0

        val newBot = Bot(
          bot.board,
          bot.playerColor,
          row,
          col,
          bot.direction,
          emptyProgram,
          InstructionSet.fromInt(childInstructionSetValue),
          Mobile.fromInt(mobileValue),
          active)

        bot.board.addBot(newBot)

        Some(BirthAnimationSucceed(
          bot.id,
          newBot.id,
          newBot.playerColor,
          newBot.row,
          newBot.col,
          newBot.direction))
      }
      case Some(_) => Some(BirthAnimationFail(bot.id))
    }
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = {
     val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)

    return Some(BirthAnimationProgress(
      bot.id,
      cycleNum,
      bot.requiredCycles,
      bot.row,
      bot.col,
      destRow,
      destCol,
      bot.direction))
  }
}

case class SetInstruction(
  sourceMapInstruction: SourceMapInstruction,
  destination: WriteableParam,
  source: ReadableParam)(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  def getRequiredCycles(bot: Bot): Int = {

    val remoteWriteCost = if (destination.local) 0 else config.sim.cycleCount.durRemoteAccessCost
    val remoteReadCost = if (source.local) 0 else config.sim.cycleCount.durRemoteAccessCost

    return config.sim.cycleCount.durSet + remoteWriteCost + remoteReadCost
  }

  def execute(bot: Bot): Option[Animation] = {
    val sourceValue = source.read(bot)
    return destination.write(bot, sourceValue)
  }

  def progress(bot: Bot, cycleNum: Int) : Option[Animation] = None
}

case class TransInstruction(
  sourceMapInstruction: SourceMapInstruction,
  sourceBank: ReadableParam,
  destBank: ReadableParam,
  lineIndex: Int,
  // Whose program did this instruction come from originally?
  playerColor: PlayerColor.EnumVal)(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Advanced

  def getRequiredCycles(bot: Bot): Int = {

    val remoteReadCostSrc = if (sourceBank.local) 0 else config.sim.cycleCount.durRemoteAccessCost
    val remoteReadCostDest = if (destBank.local) 0 else config.sim.cycleCount.durRemoteAccessCost

    val sourceBankIndex = sourceBank.read(bot) - 1

    val numInstructions =
      if (sourceBankIndex < 0 || sourceBankIndex >= bot.program.banks.size) {
        1
      } else {
        bot
          .program
          .banks(sourceBankIndex)
          .instructions
          .size
      }

    return config.sim.cycleCount.durTrans1 + config.sim.cycleCount.durTrans2 * numInstructions +
           remoteReadCostSrc + remoteReadCostDest

  }

  def errorCheck(
      bot: Bot,
      sourceBankIndex: Int): Option[Animation] = {

    val insufficientInstructionSet = checkInstructionSet(bot, "trans", lineIndex, playerColor)

    if (sourceBankIndex < 0 || sourceBankIndex >= bot.program.banks.size) {

      val errorCode = ErrorCode.InvalidParameter

      val message = s"<p><span class='display-failure'>Error at line ${lineIndex + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it attempted to " +
        s"execute a <tt>trans</tt> instruction with sourceBank == ${sourceBankIndex + 1}, but " +
        s"sourceBank must be >= 1 and <= ${bot.program.banks.size}"

      val errorMessage = ErrorMessage(errorCode, lineIndex, message)

      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
    } else if (insufficientInstructionSet.nonEmpty) {
      insufficientInstructionSet
    } else {
      return None
    }
  }

  def execute(bot: Bot): Option[Animation] = {

    val sourceBankIndex = sourceBank.read(bot) - 1

    val error: Option[Animation] = errorCheck(
      bot,
      sourceBankIndex)

    if (error.nonEmpty) {
      bot.board.removeBot(bot)
      return error
    }

    val bank = bot.program.banks(sourceBankIndex)

    bot
      .getRemote
      .map { remoteBot: Bot =>
        val destBankIndex = destBank.read(bot).toInt - 1
        if (remoteBot.program.banks.contains(destBankIndex)) {
          remoteBot.program.banks += destBankIndex -> bank
          if (remoteBot.bankIndex == destBankIndex) {
            remoteBot.instructionIndex = 0
            remoteBot.cycleNum = 1
          }
        }
      }

    None

  }

  def progress(bot: Bot, cycleNum: Int) : Option[Animation] = None
}

case class LabeledJumpInstruction(
    sourceMapInstruction: SourceMapInstruction,
    labelId: String,
    lineIndex: Int,
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  def getRequiredCycles(bot: Bot): Int = 0

  def execute(bot: Bot): Option[Animation] = None

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

// TODO: refactor Bjump and Jump together
case class JumpInstruction(
    sourceMapInstruction: SourceMapInstruction,
    jump: ReadableParam,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durJump

  def execute(bot: Bot): Option[Animation] = {

    // NOTE: The -1 here is due to the fact that bot.cycle executes the instruction
    // first, then increments the instructionIndex. So, we need to subtract by 1 to account for
    // that.
    val newInstructionIndex = bot.instructionIndex + jump.read(bot) - 1

    val oobIndex = bot.program.banks(bot.bankIndex).instructions.length

    if ((newInstructionIndex + 1) < 0 || (newInstructionIndex + 1) >= oobIndex) {
      // TODO: What if bankIndex-0, instructionIndex-0 is empty?
      // AUTOREBOOT
      bot.bankIndex = 0
      bot.instructionIndex = -1
    } else {
      bot.instructionIndex = newInstructionIndex
    }

    return None
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class LabeledBjumpInstruction(
    sourceMapInstruction: SourceMapInstruction,
    bankNumber: ReadableParam,
    labelId: String,
    lineIndex: Int,
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  def getRequiredCycles(bot: Bot): Int = 0

  def execute(bot: Bot): Option[Animation] = None

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class BjumpInstruction(
    sourceMapInstruction: SourceMapInstruction,
    bankNumber: ReadableParam,
    instructionNumber: ReadableParam,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durBJump

  def execute(bot: Bot): Option[Animation] = {

    val newBankIndex = bankNumber.read(bot) - 1

    if (newBankIndex < 0 || newBankIndex >= bot.program.banks.size) {
      // TODO: What if bankIndex-0, instructionIndex-0 is empty?
      // AUTOREBOOT
      bot.bankIndex = 0
      bot.instructionIndex = -1
      return None
    }

    // NOTE: The -2 here is due to the fact that bot.cycle executes the instruction
    // first, then increments the instructionIndex. So, we need to subtract by 1 to account for
    // that, then subtract by 1 again since instructionNumber starts at 1 and instructionIndex
    // starts at 0.
    val newInstructionIndex = instructionNumber.read(bot) - 1

    val oobIndex = bot.program.banks(newBankIndex).instructions.length

    if (newInstructionIndex < 0 || newInstructionIndex >= oobIndex) {
      // TODO: What if bankIndex-0, instructionIndex-0 is empty?
      // AUTOREBOOT
      bot.bankIndex = 0
      bot.instructionIndex = -1
      return None
    } else {
      bot.bankIndex = newBankIndex
      bot.instructionIndex = newInstructionIndex - 1
      return None
    }
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class TapoutInstruction(
    sourceMapInstruction: SourceMapInstruction,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durTapout

  def execute(bot: Bot): Option[Animation] = {
      val message = s"<p><span class='display-failure'>Tap out at line ${lineIndex + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it executed the <tt>tapout</tt> " +
        s"instruction.</p>"

      val errorCode = ErrorCode.Tapout
      val errorMessage = ErrorMessage(errorCode, lineIndex, message)

      bot.board.removeBot(bot)

      Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

// TODO: check for insufficient instruction set
case class ScanInstruction(
    sourceMapInstruction: SourceMapInstruction,
    dest: WriteableParam,
    lineIndex: Int,
    // Whose program did this instruction come from originally?
    playerColor: PlayerColor.EnumVal)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Advanced

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durScan

  def execute(bot: Bot): Option[Animation] = {
    
    checkInstructionSet(bot, "scan", lineIndex, playerColor) match {
      case Some(error) => return Some(error)
      case None => ()
    }

    bot.getRemote() match {
      case None => dest.write(bot, 0)
      case Some(remote) => {
        if (remote.playerColor == bot.playerColor) {
          dest.write(bot, 2)
        } else {
          dest.write(bot, 1)
        }
      }
    }
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class CompInstruction(
    sourceMapInstruction: SourceMapInstruction,
    first: ReadableParam,
    second: ReadableParam)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durComp

  def execute(bot: Bot): Option[Animation] = {
    val firstValue = first.read(bot)
    val secondValue = second.read(bot)

    if (firstValue == secondValue) {
      bot.instructionIndex += 1
    }

    return None
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class AddInstruction(
    sourceMapInstruction: SourceMapInstruction,
    first: WriteableParam,
    second: ReadableParam)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durAdd

  def execute(bot: Bot): Option[Animation] = {
    val firstValue: Short = first.read(bot)
    val secondValue: Short = second.read(bot)

    first.write(bot, (firstValue + secondValue).toShort)

    return None
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

case class SubInstruction(
    sourceMapInstruction: SourceMapInstruction,
    first: WriteableParam,
    second: ReadableParam)
    (implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  // TODO: take into account remote access
  def getRequiredCycles(bot: Bot): Int = config.sim.cycleCount.durSub

  def execute(bot: Bot): Option[Animation] = {
    val firstValue: Short = first.read(bot)
    val secondValue: Short = second.read(bot)

    first.write(bot, (firstValue - secondValue).toShort)

    return None
  }

  def progress(bot: Bot, cycleNum: Int): Option[Animation] = None
}

/* End instructions *******************************************************************************/
