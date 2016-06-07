package robobot.webapp

abstract class Config {

  val id = 1

  val numRows = 10
  val numCols = 15

  def mainDivId = "robobot-" + id

  val cellSize = 32

  // SVG border element
  val svgBorderStrokeWidth = 2
  val svgBorderStroke = "#777"
  def svgBorderRxRy = cellSize / 4
  def svgBorderWidth = cellSize * numCols
  def svgBorderHeight = cellSize * numRows
  def svgBorderFill = "#fff"

  // SVG element
  def svgId = mainDivId + "-svg"
  def svgWidth = cellSize * numCols + svgBorderStrokeWidth * 2
  def svgHeight = cellSize * numRows + svgBorderStrokeWidth * 2

}

object ConfigPrimary extends Config {

}