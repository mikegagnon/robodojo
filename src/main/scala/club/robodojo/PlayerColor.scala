package club.robodojo

object PlayerColor {
  sealed trait EnumVal
  case object Blue extends EnumVal
  case object Red extends EnumVal
  case object Green extends EnumVal
  case object Yellow extends EnumVal

  val colors = List(Blue, Red, Green, Yellow)

  def numToColor(playerNum: Int): EnumVal =
    playerNum match {
      case 0 => Blue
      case 1 => Red
      case 2 => Green
      case 3 => Yellow
      case _ => throw new IllegalArgumentException("Bad playerNum: " + playerNum)
    }

  def toHtmlColor(playerColor: PlayerColor.EnumVal): String =
    playerColor match {
      case PlayerColor.Blue => "#4381b6"
      case PlayerColor.Red => "#b84644"
      case PlayerColor.Green => "#5eb83f"
      case PlayerColor.Yellow => "#dad800"
    }

}
