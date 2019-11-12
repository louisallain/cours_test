class Sudoku(startConfig_ : Array[Array[Int]]) {

  var model : Array[Array[Int]] = startConfig_

  def solver() : Array[Array[Int]] = {

    this.fillXY()
    this.model
  }

  def generateRandomGrid(nbClues : Int) : Unit = {

    val nbCaseToRemove : Int = 81 - nbClues
    val r = scala.util.Random

    while(this.model.flatten.count(_ == 0) != nbCaseToRemove) {
      val randX : Int = r.nextInt(9)
      val randY : Int = r.nextInt(9)
      this.model(randX)(randY) = 0
    }
  }

  def fillXY(): Unit = {
    this.fillXYUtils(0, 0)
  }

  def fillXYUtils(x_ : Int, y_ : Int): Boolean = {

    var ret = false
    val numbers = 1 to 9

    if(y_ == 9) ret = true

    else {

      if(this.model(x_)(y_) != 0) {

        if(x_ == 8) ret = fillXYUtils(0, y_ + 1)
        else ret = fillXYUtils(x_ + 1, y_)
      }
      else {

        var i = 1
        val shuffledNumbers : List[Int] = scala.util.Random.shuffle(numbers.toList)

        while(!ret && i < 10) {

          val nb : Int = shuffledNumbers(i-1)

          if(this.isPossible(nb, x_, y_)) {

            this.model(x_)(y_) = nb

            if(x_ == 8) ret = fillXYUtils(0, y_ + 1)
            else ret = fillXYUtils(x_ + 1, y_)

            if (!ret) this.model(x_)(y_) = 0
          }
          i = i + 1
        }
      }
    }
    ret
  }

  def isPossible(number_ : Int, x_ : Int, y_ : Int) : Boolean = {

    val xBlock = x_ -(x_ % 3)
    val yBlock = y_ -(y_ % 3)

    val tested : List[Int] = this.model(x_).toList ++ this.model.map{_(y_)}.toList ++ this.model(xBlock).slice(yBlock, yBlock + 3) ++ this.model(xBlock+1).slice(yBlock, yBlock + 3) ++ this.model(xBlock+2).slice(yBlock, yBlock + 3)
    !tested.exists(_ == number_)
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
    sudoku.solver()
    println("Full sudoku grid : \n" + sudoku.toString)
    sudoku.generateRandomGrid(17)
    println("Random sudoko grid : \n" + sudoku.toString)
    sudoku.solver()
    println("Solved sudoku grid : \n" + sudoku.toString)
  }
}