package robobot.webapp

class Config(params: Map[String, Any]) {

  val id: String = params.getOrElse("id", "robobot").asInstanceOf[String]

  val numRows: Int = params.getOrElse("numRows", 3).asInstanceOf[Int]
  val numCols: Int = params.getOrElse("numCols", 6).asInstanceOf[Int]

  def mainDivId = "robobot-" + id

  val cellSize = params.getOrElse("cellSize", 32).asInstanceOf[Int]

  // SVG border element
  val svgBorderStrokeWidth = params.getOrElse("svgBorderStrokeWidth", 2).asInstanceOf[Int]
  val svgBorderStroke = params.getOrElse("svgBorderStroke", "#777").asInstanceOf[String]

  def svgBorderRxRy = cellSize / 4
  def svgBorderWidth = cellSize * numCols
  def svgBorderHeight = cellSize * numRows

  // grid lines
  val svgGridStroke = params.getOrElse("svgGridStroke", "#ccc").asInstanceOf[String]

  // SVG element
  def svgId = mainDivId + "-svg"
  def svgWidth = cellSize * numCols + svgBorderStrokeWidth * 2
  def svgHeight = cellSize * numRows + svgBorderStrokeWidth * 2

}
