package club.robodojo

import com.scalawarrior.scalajs.createjs

import scala.util.Random

class Robodojo(preload: createjs.LoadQueue)(implicit val config: Config) {
  val board = new Board()

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
  }

  /*
  val bot1 = Bot(board, 1, 0)
  bot1.direction = Direction.Right
  val bank0 = new Bank()
  bank0.instructions :+= MoveInstruction()
  //bank0.instructions :+= TurnInstruction(0)
  bot1.banks += (0 -> bank0)
  board.addBot(bot1)

  val bot2 = Bot(board, 4, 6)
  bot2.direction = Direction.Left
  board.addBot(bot2)

  val bot3 = Bot(board, 2, 4)
  bot3.direction = Direction.Down
  board.addBot(bot3)*/

  val viz = new Viz(preload, board)

  val controller = new Controller(board, viz)
}