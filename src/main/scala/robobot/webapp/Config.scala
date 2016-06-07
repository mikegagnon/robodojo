package robobot.webapp

abstract class Config {

  val id = 1

  val numRows = 10
  val numCols = 15
  def mainDivId = "robobot-" + id
  val cellSize = 32

  def svgId = mainDivId + "-svg"
  def svgWidth = cellSize * numCols
  def svgHeight = cellSize * numRows

}

object ConfigPrimary extends Config {

}