package robobot.webapp

abstract class Config {

  val id = 1

  val numRows = 3
  val numCols = 6

  def mainDivId = "robobot-" + id

  val cellSize = 32

  // SVG border element
  val svgBorderStrokeWidth = 2
  val svgBorderStroke = "#777"
  def svgBorderRxRy = cellSize / 4
  def svgBorderWidth = cellSize * numCols
  def svgBorderHeight = cellSize * numRows

  // grid lines
  val svgGridStroke = "#ccc"

  // SVG element
  def svgId = mainDivId + "-svg"
  def svgWidth = cellSize * numCols + svgBorderStrokeWidth * 2
  def svgHeight = cellSize * numRows + svgBorderStrokeWidth * 2

  // TODO: do SVG templates belong somewhere else?
  val botTemplate = """
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

}

object ConfigPrimary extends Config {

}