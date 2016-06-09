package robobot.webapp

class Robobot(implicit val config: Config) {
  val board = new Board()

  val bot1 = new Bot()
  bot1.row = 0
  bot1.col = 0
  bot1.direction = Direction.Right
  val bank0 = new Bank()
  bank0.instructions :+= MoveInstruction()
  bot1.banks += (0 -> bank0)
  board.addBot(bot1)

  val bot2 = new Bot()
  bot2.row = 1
  bot2.col = 1
  bot2.direction = Direction.Left
  board.addBot(bot2)

  val bot3 = new Bot()
  bot3.row = 2
  bot3.col = 4
  bot3.direction = Direction.Down
  board.addBot(bot3)

  val viz = new Viz(board)
}