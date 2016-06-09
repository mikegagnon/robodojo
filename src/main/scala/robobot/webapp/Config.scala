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

  // TODO: do SVG templates belong somewhere else?
  private val botTemplateDefault = """
    <g id="botTemplate" height="32" width="32" >

      <circle
        stroke="black"
        fill="steelblue"
        r="9"
        cx="16"
        cy="16" ></circle>

      <!-- the head -->
      <g transform="rotate(45 16 16)">
        <!-- fill in the head -->
        <path
          d="M7 7 L17 7 L7 17 Z"
          stroke="none"
          fill="steelblue" />

        <!-- outline the head -->
        <path
          d="M7 7 L17 7 M7 7 L7 17"
          stroke="black"
          stroke-width="1"
          fill="none" />
        </g>
    </g>
  """

  val botTemplate = params.getOrElse("botTemplate", botTemplateDefault).asInstanceOf[String]

}
