package robobot.webapp

class Bot(val board: Board) {

  var row = -1

  var col = -1

  var direction: Direction.EnumVal = Direction.NoDir

  var banks = Map[Int, Bank]()

  var bankNum = 0

  var instructionNum = 0

  var cycleNum = 0

  def cycle() = {

    var bank = banks(bankNum)

    val instruction = bank.instructions(instructionNum)

    if (cycleNum == instruction.cycles) {

      cycleNum = 0
      instructionNum += 1

      if (instructionNum == bank.instructions.length) {
        instructionNum = 0
      }

      instruction.execute(this, board)
    } else {
      cycleNum += 1
    }

  }

}