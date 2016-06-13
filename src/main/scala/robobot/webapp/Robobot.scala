package robobot.webapp

import com.scalawarrior.scalajs.createjs

class Robobot(preload: createjs.LoadQueue)(implicit val config: Config) {
  val board = new Board()

  val bot1 = Bot(board, 5, 5)
  bot1.direction = Direction.Right
  val bank0 = new Bank()
  bank0.instructions :+= MoveInstruction()
  bank0.instructions :+= TurnInstruction(0)
  bot1.banks += (0 -> bank0)
  board.addBot(bot1)

  val bot2 = Bot(board, 4, 6)
  bot2.direction = Direction.Left
  board.addBot(bot2)

  val bot3 = Bot(board, 2, 4)
  bot3.direction = Direction.Down
  board.addBot(bot3)

  val viz = new Viz(preload, board)

  val controller = new Controller(config, board, viz)
}