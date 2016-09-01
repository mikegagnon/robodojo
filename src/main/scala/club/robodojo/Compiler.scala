package club.robodojo

// TODO: rm
import scala.collection.mutable.ArrayBuffer

import scala.collection.mutable

// A TokenLine instance represents a line from the source code for a bot
// tokens is an array of "tokens", where each token is lower case, and trimmed of whitespace, etc.
//    (See tokenize(...)).
// originalLine is the original line from the source code
// lineIndex specifies the line number of the line, but starting at zero instead of one.
case class TokenLine(tokens: Array[String], originalLine: String, lineIndex: Int) {

  // TESTED
  override def equals(that: Any): Boolean =
    that match {
      case tl: TokenLine => tokens.sameElements(tl.tokens) &&
        lineIndex == tl.lineIndex &&
        originalLine == tl.originalLine
      case _ => false
    }
  
  // TESTED
  // TODO: Lookup hashCode best practice. I just hacked this together.
  override def hashCode: Int = {
    var x = 13
    tokens.foreach { str: String => x *= (str.hashCode + 13)}

    return x * (lineIndex.hashCode + 13) * (originalLine.hashCode + 11)
  }

  override def toString(): String = s"TokenLine([${tokens.mkString(",")}], '${originalLine}', " +
    s"${lineIndex})"
}

object ErrorCode {
  sealed trait EnumVal
  sealed trait CompileTimeError extends EnumVal
  sealed trait RunTimeError extends EnumVal

  case object UnrecognizedInstruction extends CompileTimeError
  case object TooManyParams extends CompileTimeError
  case object MissingParams extends CompileTimeError
  case object WrongParamType extends CompileTimeError
  case object UndeclaredBank extends CompileTimeError
  case object MaxBanksExceeded extends CompileTimeError
  case object EmptyBanks extends CompileTimeError
  case object MalformedInstruction extends CompileTimeError
  case object BadInstructionSetParam extends CompileTimeError
  case object BadNumBanksParam extends CompileTimeError
  case object BadMobileParam extends CompileTimeError
  case object DuplicateLabels extends CompileTimeError
  case object MissingLabel extends CompileTimeError

  case object InvalidParameter extends RunTimeError
  case object DataHunger extends RunTimeError
  case object InsufficientInstructionSet extends RunTimeError
  case object CannotMoveImmobile extends RunTimeError
  case object Tapout extends RunTimeError

}

// TODO: underline the offensive text in the program text?
// TODO: hyperlink error messages?
case class ErrorMessage(errorCode: ErrorCode.EnumVal, lineIndex: Int, message: String)

case class CompileLineResult(
  instruction: Option[Instruction],
  errorMessage: Option[ErrorMessage])

sealed trait ParamType

case object ReadableParamType extends ParamType {
  override val toString = "readable parameter"
}

case object WriteableParamType extends ParamType {
  override val toString = "writeable parameter"
}

case object LabelParamType extends ParamType {
  override val toString = "label parameter"
}

object Compiler {

  /* Begin: ErrorMessage production ***************************************************************/

  // converts 0 to "a", 1 to "b" and so on
  def getIndexToLetter(index: Int) = (index + 'a'.toInt).toChar.toString

  // Produces a string like: "a, b" or "a, b, c"
  def getInstructionForm(numParams: Int): String =
    (0 until numParams)
      .map { getIndexToLetter(_) }
      .mkString(", ")

  // Produces a string like: "a is a writeable parameter, and b is a readable parameter"
  def getParamTypes(types: Seq[ParamType]): String =
    types
      .zipWithIndex
      .map { case (t: ParamType, i: Int) =>
        s"<tt>${getIndexToLetter(i)}</tt> is a <i>${t}</i>"
      }
      .mkString(", and ")

  def getParamNumber(index: Int): String =
    if (index == 0) {
      "first"
    } else if (index == 1) {
      "second"
    } else if (index == 2) {
      "third"
    } else {
      throw new IllegalArgumentException("index is bad")
    }

  def getErrorIntro(
      begin: String,
      instructionName: String,
      types: Seq[ParamType]): String = {

    val instructionForm: String = getInstructionForm(types.length)

    val paramTypes: String = getParamTypes(types)

    return s"<b>${begin}</b>: the <tt>${instructionName}</tt> instruction " +
    s"must be of the form: <tt>${instructionName} ${instructionForm}</tt>, where ${paramTypes}. "
  }

  def getErrorWrongParamType(
      instructionName: String,
      token: String,
      badParameterIndex: Int,
      lineIndex: Int,
      types: Seq[ParamType]): ErrorMessage = {

    val paramNumber: String = getParamNumber(badParameterIndex)

    val intro: String = getErrorIntro("Wrong parameter type", instructionName, types)

    val message = intro + s"Your ${paramNumber} parameter, <tt>${token}</tt>, is not a " +
      s"${types(badParameterIndex)}."

    val errorCode = ErrorCode.WrongParamType
    
    return ErrorMessage(errorCode, lineIndex, message)
  }

  def getErrorMalformedInstruction(
      instructionName: String,
      lineIndex: Int,
      types: Seq[ParamType]): ErrorMessage = {

      val form: String = getInstructionForm(types.length)

      val instrTypes: String = getParamTypes(types)

      val message = s"<b>Malformed <tt>${instructionName}</tt> instruction</b>: the " +
        s"<tt>${instructionName}</tt> must be of the form <tt>${instructionName} " +
        s"${form}</tt>, where ${instrTypes}."

      val errorCode = ErrorCode.MalformedInstruction

      return ErrorMessage(errorCode, lineIndex, message)

  }

  /* End: ErrorMessage production *****************************************************************/

  /* Begin: reading parameters from a TokenLine ***************************************************/

  def isShort(value: String): Boolean =
    try {
      value.toShort
      true
    } catch {
      case _ : NumberFormatException => false
    }

  def isRegister(token: String)(implicit config: Config): Boolean = {
    if (token(0) != '#') {
      return false
    }

    val num = token.substring(1)

    if (!isShort(num)) {
      return false
    }

    val registerNum = num.toInt

    if (registerNum < 1 || registerNum > config.sim.maxNumVariables) {
      return false
    }

    return true
  }

  def getWriteable(token: String)(implicit config: Config): WriteableParam =
    if (token == "#active") {
      ActiveKeyword(true)
    } else if (token == "%active") {
      ActiveKeyword(false)
    } else if (isRegister(token)) {
      val registerNum = token.substring(1).toShort
      RegisterParam(registerNum - 1)
    } else {
     throw new IllegalArgumentException("Bad token: " + token)
    }

  def getReadable(token: String)(implicit config: Config): ReadableParam =
    if (isRegister(token)) {
      val registerNum = token.substring(1).toShort
      RegisterParam(registerNum - 1)
    } else if (isShort(token)) {
      IntegerParam(token.toShort)
    } else if (token == "#active") {
      ActiveKeyword(true)
    } else if (token == "%active") {
      ActiveKeyword(false)
    } else if (token == "$banks") {
      BanksKeyword(true)
    } else if (token == "%banks") {
      BanksKeyword(false)
    } else if (token == "$instrset") {
      InstrSetKeyword(true)
    } else if (token == "%instrset") {
      InstrSetKeyword(false)
    } else if (token == "$mobile") {
      MobileKeyword(true)
    } else if (token == "%mobile") {
      MobileKeyword(false)
    } else if (token == "$fields") {
      FieldsKeyword()
    } else {
      throw new IllegalArgumentException("Bad token: " + token)
    }

  def getLabel(token: String)(implicit config: Config): LabelParam =
    if (token(0) == '@') {
      LabelParam(token)
    } else {
      throw new IllegalArgumentException("Not a label: " + token)
    }

  // TESTED
  def getParam(
      instructionName: String,
      parameterIndex: Int,
      lineIndex: Int,
      types: Seq[ParamType],
      token: String)(implicit config: Config): Either[ErrorMessage, Param] = {

    val paramType = types(parameterIndex)

    paramType match {
      case ReadableParamType => try {
          Right(getReadable(token))
        } catch {
          case _: IllegalArgumentException =>
            Left(getErrorWrongParamType(instructionName, token, parameterIndex, lineIndex, types))
        }
      case WriteableParamType => try {
          Right(getWriteable(token))
        } catch {
          case _: IllegalArgumentException =>
            Left(getErrorWrongParamType(instructionName, token, parameterIndex, lineIndex, types))
        }
      case LabelParamType => try {
          Right(getLabel(token))
        } catch {
          case _: IllegalArgumentException =>
          Left(getErrorWrongParamType(instructionName, token, parameterIndex, lineIndex, types))
        }
    }
  }

  // TESTED
  // The indices for where we should find commas
  def getCommaIndices(numParams: Int): Seq[Int] =
    (0 until numParams - 1)
      .map { paramIndex: Int =>
        paramIndex * 2 + 2
      }

  // TESTED
  // Returns true iff there are commas in all the right places
  def foundCommas(numParams: Int, tl: TokenLine): Boolean =
    getCommaIndices(numParams)
      .forall { index =>
        tl.tokens(index) == ","
      }

  // TESTED
  def parseParams(
      instructionName: String,
      tl: TokenLine,
      paramTypes: ParamType*)(implicit config: Config):
        Either[ErrorMessage, Seq[Param]] =

    if (tl.tokens.length != paramTypes.length * 2 ||
        !foundCommas(paramTypes.length, tl)) {
      return Left(getErrorMalformedInstruction(instructionName, tl.lineIndex, paramTypes))
    } else {

      val paramsAndErrors: Seq[Either[ErrorMessage, Param]] =
        paramTypes
          .zipWithIndex
          .map { case (paramType: ParamType, index: Int) =>
            val token = tl.tokens(index * 2 + 1)
            getParam(instructionName, index, tl.lineIndex, paramTypes, token)
          }

      val errorMessage: Option[ErrorMessage] =
        paramsAndErrors
          .flatMap { element: Either[ErrorMessage, Param] =>
            element match {
              case Left(error) => Some(error)
              case Right(_) => None
            }
          }
          .headOption

      val params: Seq[Param] =
        paramsAndErrors
          .flatMap { element: Either[ErrorMessage, Param] =>
            element match {
              case Left(_) => None
              case Right(param) => Some(param)
            }
          }

      errorMessage match {
        case Some(error) => Left(error)
        case None => {
          if (params.length != paramTypes.length) {
            throw new IllegalStateException("params.length != paramTypes.length")
          }
          Right(params)
        }
      }
    }

  /* End: reading parameters from a TokenLine *****************************************************/

  // TESTED
  // TODO: deal with name, author country
  def tokenize(text: String)(implicit config: Config): Array[TokenLine] =
    text
      .split("\n")
      // Slice the string so only the first config.compiler.maxLineLength characters are kept
      .map { line: String =>
        val slice = line.slice(0, config.compiler.maxLineLength)
        (slice, slice)
      }
      // Remove comments
      .map { case (line: String, originalLine: String) =>
        (line.replaceAll(";.*", ""), originalLine)
      }
      .map { case (line: String, originalLine: String) =>
        (line.replaceAll(",", " , "), originalLine)
      }
      .map { case (line: String, originalLine: String) =>
        (line.replaceAll("""^\s*@""", "@ "), originalLine)
      }
      .map { case (line: String, originalLine: String) =>
        (line.toLowerCase, originalLine)
      }
      // Separate into tokens
      .map { case (line: String, originalLine: String) =>
        (line.split("""\s+"""), originalLine)
      }
      // Drop empty tokens
      .map { case (tokens: Array[String], originalLine: String) =>
        (tokens.filter { _ != "" }, originalLine)
      }
      // Pair lines with line numbers
      .zipWithIndex
      .map { case ((tokens: Array[String], originalLine: String), lineIndex: Int) =>
        TokenLine(tokens, originalLine, lineIndex)
      }

  val readableKeywords = Set("#active", "%active", "$banks", "%banks", "$instrset", "%instrset",
    "$mobile", "%mobile", "$fields")

  def isReadableKeyword(token: String): Boolean = readableKeywords.contains(token)

  val writeableKeywords = Set("#active", "%active")

  def isWriteableKeyword(token: String): Boolean = writeableKeywords.contains(token)

  // TODO: rm?
  def isReadable(token: String)(implicit config: Config): Boolean =
    isRegister(token) ||
    isReadableKeyword(token) ||
    isShort(token)

  // TODO: rm?
  def isWriteable(token: String)(implicit config: Config): Boolean =
    isRegister(token) ||
    isWriteableKeyword(token)

  def unrecognizedInstruction(tl: TokenLine) = {
    val message = s"Unrecognized instruction: <tt>${tl.tokens(0)}</tt>."
    val errorMessage = ErrorMessage(ErrorCode.UnrecognizedInstruction, tl.lineIndex, message)
    CompileLineResult(None, Some(errorMessage))
  }

  // TESTED
  def compileBank(tl: TokenLine): CompileLineResult =
    if (tl.tokens.length > 2) {
      val message = "Too many parameters: the <tt>bank</tt> directive requires exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineIndex, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length < 2) {
      val message = "Missing parameter: the <tt>bank</tt> directive requires exactly one parameter."
      val errorMessage = ErrorMessage(ErrorCode.MissingParams, tl.lineIndex, message)
      CompileLineResult(None, Some(errorMessage))
    } else {
      CompileLineResult(None, None)
    }

  // TESTED
  def compileMove(
      sourceMapInstruction: SourceMapInstruction,
      tl: TokenLine,
      playerColor: PlayerColor.EnumVal)
      (implicit config: Config): CompileLineResult =

    if (tl.tokens.length > 1) {
      val message = "Too many parameters: the <tt>move</tt> instruction does not take any " +
        "parameters."
      val errorMessage = ErrorMessage(ErrorCode.TooManyParams, tl.lineIndex, message)
      CompileLineResult(None, Some(errorMessage))
    } else if (tl.tokens.length == 1) {
      val instruction = MoveInstruction(sourceMapInstruction, tl.lineIndex, playerColor)
      CompileLineResult(Some(instruction), None)
    } else {
      throw new IllegalStateException("This code shouldn't be reachable")
    }

  // TESTED
  def compileTurn(sourceMapInstruction: SourceMapInstruction, tl: TokenLine)(implicit config: Config): CompileLineResult = {

    val parsed: Either[ErrorMessage, Seq[Param]] = parseParams("turn", tl, ReadableParamType)

    val params: Seq[Param] = parsed match {
      case Left(errorMessage) => return CompileLineResult(None, Some(errorMessage))
      case Right(params) => params
    }

    val direction = params(0).asInstanceOf[ReadableParam]

    val instruction = TurnInstruction(sourceMapInstruction, direction)

    CompileLineResult(Some(instruction), None)
  }

  // TESTED
  def compileCreate(
      sourceMapInstruction: SourceMapInstruction, 
      tl: TokenLine,
      playerColor: PlayerColor.EnumVal)(implicit config: Config): CompileLineResult = {

    val parsed: Either[ErrorMessage, Seq[Param]] =
      parseParams(
        "create",
        tl,
        ReadableParamType,
        ReadableParamType,
        ReadableParamType)

    val params: Seq[Param] = parsed match {
      case Left(errorMessage) => return CompileLineResult(None, Some(errorMessage))
      case Right(params) => params
    }

    // These casts are safe because parseParams ensures type safety
    val instructionSet = params(0).asInstanceOf[ReadableParam]
    val numBanks = params(1).asInstanceOf[ReadableParam]
    val mobile = params(2).asInstanceOf[ReadableParam]

    // Check for errors
    if (config.compiler.safetyChecks) {

      val instructionSetInt: Int = instructionSet match {
        case IntegerParam(i) => i
        case _ => 0
      }

      val numBanksInt: Int = numBanks match {
        case IntegerParam(i) => i
        case _ => 1
      }

      val mobileInt: Int = mobile match {
        case IntegerParam(i) => i
        case _ => 0
      }

      if (instructionSetInt < 0 || instructionSetInt > 2) {
        val message = "Bad instruction-set parameter: the first parameter to the <tt>create</tt> " +
          "instruction, the instruction-set parameter, can only be 0 (signifying the " +
          "<i>Basic</i> instruction set) or 1 (signifying the <i>Extended</i> instruction set)."
        val errorCode = ErrorCode.BadInstructionSetParam
        val errorMessage = ErrorMessage(errorCode, tl.lineIndex, message)
        return CompileLineResult(None, Some(errorMessage))
      } else if (numBanksInt < 1 || numBanksInt > config.sim.maxBanks) {
        val message = "Bad numBanks parameter: the second parameter to the <tt>create</tt> " +
          "instruction, the numBanks parameter, must be greater (or equal to) 1 and less than " +
          s"(or equal to) ${config.sim.maxBanks}"
        val errorCode = ErrorCode.BadNumBanksParam
        val errorMessage = ErrorMessage(errorCode, tl.lineIndex, message)
        return CompileLineResult(None, Some(errorMessage))
      } else if (mobileInt < 0 || mobileInt > 1) {
        val message = "Bad mobile parameter: the third parameter to the <tt>create</tt> " +
          "instruction, the mobile parameter, can only be 0 (signifying immobility) or 1 " +
          "(signifying mobility)."
        val errorCode = ErrorCode.BadMobileParam
        val errorMessage = ErrorMessage(errorCode, tl.lineIndex, message)
        return CompileLineResult(None, Some(errorMessage))
      }
    }

    val instruction = CreateInstruction(
      sourceMapInstruction,
      instructionSet,
      numBanks,
      mobile,
      tl.lineIndex,
      playerColor)

    CompileLineResult(Some(instruction), None)
  }

  // TESTED
  def compileSet(sourceMapInstruction: SourceMapInstruction, tl: TokenLine)
      (implicit config: Config): CompileLineResult = {

    val parsed: Either[ErrorMessage, Seq[Param]] =
      parseParams("set", tl, WriteableParamType, ReadableParamType)

    val params: Seq[Param] = parsed match {
      case Left(errorMessage) => return CompileLineResult(None, Some(errorMessage))
      case Right(params) => params
    }

    val destination = params(0).asInstanceOf[WriteableParam]
    val source = params(1).asInstanceOf[ReadableParam]
    val instruction = SetInstruction(sourceMapInstruction, destination, source)

    CompileLineResult(Some(instruction), None)
  }

  def compileTrans(
      sourceMapInstruction: SourceMapInstruction,
      tl: TokenLine,
      playerColor: PlayerColor.EnumVal)
      (implicit config: Config): CompileLineResult = {

    val parsed: Either[ErrorMessage, Seq[Param]] =
      parseParams("trans", tl, ReadableParamType, ReadableParamType)

    val params: Seq[Param] = parsed match {
      case Left(errorMessage) => return CompileLineResult(None, Some(errorMessage))
      case Right(params) => params
    }

    val sourceBank = params(0).asInstanceOf[ReadableParam]
    val destBank = params(1).asInstanceOf[ReadableParam]

    val instruction = TransInstruction(
      sourceMapInstruction,
      sourceBank,
      destBank,
      tl.lineIndex,
      playerColor)

    CompileLineResult(Some(instruction), None)
  }

  // TODO: better error messages. Right now error message make no mention of labels
  def compileJump(
      sourceMapInstruction: SourceMapInstruction,
      tl: TokenLine,
      playerColor: PlayerColor.EnumVal)
      (implicit config: Config): CompileLineResult = {

    val parsed: Either[ErrorMessage, Seq[Param]] =
      parseParams("jump", tl, ReadableParamType)

    val params: Seq[Param] = parsed match {
      case Left(errorMessage) => {

        if (tl.tokens.length == 2 && tl.tokens(1)(0) == '@'
          && tl.tokens(1).length > 1) {
          val labelId = tl.tokens(1)
          val instruction = LabeledJumpInstruction(
            sourceMapInstruction,
            labelId,
            tl.lineIndex,
            playerColor)
          return CompileLineResult(Some(instruction), None)
        }

        return CompileLineResult(None, Some(errorMessage))
      }
      case Right(params) => params
    }

    val jump = params(0).asInstanceOf[ReadableParam]

    val instruction = JumpInstruction(
      sourceMapInstruction,
      jump,
      tl.lineIndex,
      playerColor)

    CompileLineResult(Some(instruction), None)
  }

    // TODO: better error messages. Right now error message make no mention of labels
  def compileBjump(
      sourceMapInstruction: SourceMapInstruction,
      tl: TokenLine,
      playerColor: PlayerColor.EnumVal)
      (implicit config: Config): CompileLineResult = {

    val parsed1: Either[ErrorMessage, Seq[Param]] =
      parseParams("bjump", tl, ReadableParamType, ReadableParamType)

    val parsed2: Either[ErrorMessage, Seq[Param]] =
      parseParams("bjump", tl, ReadableParamType, LabelParamType)

    val (params: Seq[Param], label: Boolean) = parsed1 match {
      case Left(errorMessage) => {
        parsed2 match {
          case Left(errorMessage) => return CompileLineResult(None, Some(errorMessage))
          case Right(params) => (params, true)
        }
      }
      case Right(params) => (params, false)
    }

    val bankNumber = params(0).asInstanceOf[ReadableParam]

    val instruction: Instruction =
      if (label) {

        val labelId: String = params(1).asInstanceOf[LabelParam].label

        LabeledBjumpInstruction(
            sourceMapInstruction,
            bankNumber,
            labelId,
            tl.lineIndex,
            playerColor)

      } else {

        val instructionNumber = params(1).asInstanceOf[ReadableParam]

        BjumpInstruction(
          sourceMapInstruction,
          bankNumber,
          instructionNumber,
          tl.lineIndex,
          playerColor)
      }

    CompileLineResult(Some(instruction), None)
  }

  // To facilitate the debugger, instructions and banks keep track of source code information.
  // Regarding banks, each bank has a sourceMap field that stores the original source code for the
  // bank. This function generates those sourceMap fields. This function assumes the compilation was
  // successful; this is a second pass.
  def getSourceMaps(
      // All lines from the original source code program
      lines: Array[TokenLine],
      playerColor: PlayerColor.EnumVal,
      bankBuilders: mutable.Map[Int, BankBuilder]): Map[Int, Bank] = {

    var bankLines = ArrayBuffer[String]()
    var bankIndex = -1
    var newBanks = Map[Int, Bank]()

    lines.foreach { case TokenLine(tokens, originalLine, lineIndex) =>

      if (tokens.length > 0 && tokens(0) == "bank") {
        if (bankIndex >= 0) {
          val sourceMap = Some(SourceMap(playerColor, bankIndex, bankLines.toIndexedSeq))
          val oldBank = bankBuilders(bankIndex)
          newBanks += bankIndex -> Bank(oldBank.instructions.toIndexedSeq, sourceMap)
        }

        bankIndex += 1
        bankLines = ArrayBuffer[String]()
      }
      bankLines :+= originalLine
    }

    // The last bank
    if (bankIndex >= 0) {
      val sourceMap = Some(SourceMap(playerColor, bankIndex, bankLines.toIndexedSeq))
      val oldBank = bankBuilders(bankIndex)
      newBanks += bankIndex -> Bank(oldBank.instructions.toIndexedSeq, sourceMap)
    }

    newBanks
  }

  // TESTED
  // We need playerColor because of run-time errors. Specifically, some instructions (such as the
  // CreateInstruction) need to report the color of the bot that that the instruction came from.
  // See for example CreateInstruction documentation and CreateInstruction.errorCheck(...).
  def compile(
      text: String,
      playerColor: PlayerColor.EnumVal)(implicit config: Config):
      Either[ArrayBuffer[ErrorMessage], Program] = {

    val lines: Array[TokenLine] = tokenize(text)
    var bankBuilders = mutable.Map[Int, BankBuilder]()

    // TODO: change to bankIndex?
    var bankNumber = -1
    var errors = ArrayBuffer[ErrorMessage]()

    val labelStrings = mutable.Set[String]()

    // labels(bankIndex)(labelString) == instruction Index for that label
    var labels = mutable.Map[Int, mutable.Map[String, Int]]()

    // labelsByLabel(labelString) = instruction Index for that label (relative to its bank)
    var labelsByLabel = mutable.Map[String, Int]()

    var currentLabelId: Option[String] = None

    // Like lineIndex, but relative to the current bank. For example, if we are at the 5th line
    // of bank N, then bankLineIndex == 4
    var bankLineIndex = 0

    lines.foreach { case (tl: TokenLine) =>

      if (tl.tokens.length > 0) {

        val sourceMapInstruction = SourceMapInstruction(bankLineIndex, bankNumber)

        val result: CompileLineResult = tl.tokens(0) match {
          case "bank" => {
            if (bankNumber == config.sim.maxBanks - 1) {
                val errorMessage = ErrorMessage(ErrorCode.MaxBanksExceeded, tl.lineIndex,
                  s"Too many banks: programs may only have ${config.sim.maxBanks} banks.")
                CompileLineResult(None, Some(errorMessage))
            } else {
              bankNumber += 1
              bankLineIndex = 0
              bankBuilders += (bankNumber -> new BankBuilder)
              compileBank(tl)
            }
          }

          case "@" => {
            // TOOD: check label tokens for correctness

            val labelId = "@" + tl.tokens(1)

            if (labelStrings.contains(labelId)) {
              val errorMessage = ErrorMessage(ErrorCode.DuplicateLabels, tl.lineIndex,
                s"""Duplicate labels: The label <tt>${labelId}</tt> appears multiple
                times in your program.""")
              CompileLineResult(None, Some(errorMessage))
            } else {
              labelStrings.add(labelId)
              currentLabelId = Some(labelId)
              CompileLineResult(None, None)
            }
          }
          case "move" => compileMove(sourceMapInstruction, tl, playerColor)
          case "turn" => compileTurn(sourceMapInstruction, tl)
          case "create" => compileCreate(sourceMapInstruction, tl, playerColor)
          case "set" => compileSet(sourceMapInstruction, tl)
          case "trans" => compileTrans(sourceMapInstruction, tl, playerColor)
          case "jump" => compileJump(sourceMapInstruction, tl, playerColor)
          case "bjump" => compileBjump(sourceMapInstruction, tl, playerColor)
          case _ => unrecognizedInstruction(tl)
        }

        result.errorMessage match {
          case Some(errorMessage) => {
            bankBuilders = mutable.Map[Int, BankBuilder]()
            errors += errorMessage
          }
          case None => ()
        }

        if (errors.isEmpty) {
          result.instruction.foreach { instruction: Instruction =>
            if (bankNumber >= 0) {
              bankBuilders(bankNumber).instructions += instruction

              currentLabelId.foreach { id: String =>
                val instructionIndex = bankBuilders(bankNumber).instructions.length - 1

                if (!labels.contains(bankNumber)) {
                  labels(bankNumber) = mutable.Map[String, Int]()
                }

                if (labels(bankNumber).contains(id)) {
                  throw new IllegalStateException("duplicate labels not caught by labelStrings")
                } else {
                  labels(bankNumber)(id) = instructionIndex
                  labelsByLabel(id) = instructionIndex
                }

                currentLabelId = None
              }

            } else {
              errors += ErrorMessage(ErrorCode.UndeclaredBank, tl.lineIndex, "Undeclared " +
                "bank: you must place a <tt>bank</tt> directive before you place any instructions.")
            }
          }
        }
      }

      bankLineIndex += 1

    }

    // Convert jump @label instructions into executable jump instructions
    bankBuilders.foreach { case (bankIndex: Int, builder: BankBuilder) =>
      builder
        .instructions
        .zipWithIndex
        .foreach { case (instruction: Instruction, instructionIndex: Int) =>
          instruction match {
            case LabeledJumpInstruction(
                sourceMapInstruction,
                labelId,
                lineIndex,
                playerColor) => {

              if (labels.contains(bankIndex) && labels(bankIndex).contains(labelId)) {

                // The destination instructionIndex for the jump
                val jumpTo: Int = labels(bankIndex)(labelId)

                val jump = (jumpTo - instructionIndex).toShort

                val jumpInstruction = JumpInstruction(
                  sourceMapInstruction,
                  IntegerParam(jump),
                  lineIndex,
                  playerColor)

                bankBuilders(bankIndex).instructions(instructionIndex) = jumpInstruction
              } else {
                errors += ErrorMessage(ErrorCode.MissingLabel, lineIndex, s"""
                  You cannot jump to ${labelId} because the label ${labelId} does not appear in
                  your bank (or there are no instructions after label ${labelId}).
                  """)
              }

            }
            case LabeledBjumpInstruction(
                sourceMapInstruction,
                bankNumber,
                labelId,
                lineIndex,
                playerColor) => {

              if (labelsByLabel.contains(labelId)) {

                // The destination instructionIndex for the jump
                val jumpToNumber = labelsByLabel(labelId)  + 1

                val bjumpInstruction = BjumpInstruction(
                  sourceMapInstruction,
                  bankNumber,
                  IntegerParam(jumpToNumber.toShort),
                  lineIndex,
                  playerColor)

                bankBuilders(bankIndex).instructions(instructionIndex) = bjumpInstruction
              } else {
                errors += ErrorMessage(ErrorCode.MissingLabel, lineIndex, s"""
                  You cannot bjump to ${labelId} because the label ${labelId} does not appear in
                  the destination bank (or there are no instructions after label ${labelId}).
                  """)
              }
            }
            case _ =>
          }
        }
    }

    if (errors.nonEmpty) {
      Left(errors)
    } else if (bankBuilders.isEmpty) {
      Left(ArrayBuffer(ErrorMessage(ErrorCode.EmptyBanks, 0, "Your program is empty.")))
    } else {
      val banks = getSourceMaps(lines, playerColor, bankBuilders)
      Right(Program(banks))
    }
  }

}