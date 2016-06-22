package club.robodojo

import com.scalawarrior.scalajs.createjs

import scala.util.Random

class Robodojo(preload: createjs.LoadQueue)(implicit val config: Config) {
  val board = new Board()

  /*
  val density = 0.5

  val rand = new Random(0)

  0 until config.sim.numRows foreach { row =>
    0 until config.sim.numCols foreach { col =>
      if (rand.nextDouble < density) {
          val bot = Bot(board, PlayerColor.Blue, row, col)
          bot.direction = Direction.Right
          if (rand.nextDouble < 0.25)
            bot.direction = Direction.Left
          if (rand.nextDouble < 0.25)
            bot.direction = Direction.Up
          if (rand.nextDouble < 0.25)
            bot.direction = Direction.Down

          val bank0 = new Bank()

          if (rand.nextDouble < 0.5)
            bank0.instructions :+= MoveInstruction()
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= MoveInstruction()
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= TurnInstruction(0)
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= MoveInstruction()
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= MoveInstruction()
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= MoveInstruction()
          if (rand.nextDouble < 0.5)
            bank0.instructions :+= TurnInstruction(1)
          bot.program.banks += (0 -> bank0)
          board.addBot(bot)
      }
    }
  }*/

  
  /*
  bank Main
    bank Main
    turn 0
    create 1, 1, 1
    turn 1
    move
  */

  val instructionSet = InstructionSet.Extended
  val mobile = true
  val active = true

  val bot1Program: Program =
    Compiler.compile("bank foo\ncreate 1,1,1\nturn 0", PlayerColor.Blue).right.get

  //val bot1Program: Program = Compiler.compile("bank foo\nmove").right.get
  val bot1 = Bot(
    board,
    PlayerColor.Blue,
    1, 0,
    Direction.Right,
    bot1Program,
    instructionSet,
    mobile,
    active)

  board.addBot(bot1)

  val bot2Program: Program = Compiler.compile("bank foo\nturn 1", PlayerColor.Red).right.get
  val bot2 = Bot(
    board,
    PlayerColor.Red,
    1, 1,
    Direction.Right,
    bot2Program,
    instructionSet,
    mobile,
    active)
  
  board.addBot(bot2)
  
  val viz = new Viz(preload, board)

  val controller = new Controller(viz)
}