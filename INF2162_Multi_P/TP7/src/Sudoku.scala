class Sudoku(startConfig_ : Array[Array[Int]]) {

  var model : Array[Array[Int]] = startConfig_

  def solver() : List[Array[Array[Int]]] = {
    //var workGrid : Array[Array[Int]] =
    List()
  }

  def fillXY(x_ : Int, y_ : Int): Unit = {

  }

  def isPossible(number_ : Int, x_ : Int, y_ : Int) : Boolean = {

    false
  }

  override def toString: String = {

    var ret : String = ""
    val lineSep : String = "+---" * 9 + "+\n"
    ret = ret + lineSep
    for (i <- 0 to 8) {
      for(j <- 0 to 8) {
        ret = ret + "|" + " " + this.model(i)(j).toString + " "
      }
      ret = ret + "|\n"
      ret = ret + lineSep
    }

    ret
  }
}

object Sudoku {
  def main(args: Array[String]): Unit = {

    val grid = Array.fill[Int](9, 9)(0)
    val sudoku : Sudoku = new Sudoku(grid)
    println(sudoku.toString)
  }
}