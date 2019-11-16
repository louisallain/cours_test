// Q1
import java.util

import scala.{Array => $}
import scala.reflect.ClassTag

object Sudoku {

  // Q2
  def TwoDimArrayToString[T:ClassTag](a_ : $[$[T]]) : String = {
    a_.map(_.mkString(" ")).mkString("\n")
  }

  // Q3
  def replaceByInTwoDimArray[T:ClassTag](a_ : $[$[T]], coords_ : (Int, Int), newElem_ : T) : $[$[T]] = {
    a_.updated(coords_._1, a_(coords_._1).updated(coords_._2, newElem_))
  }

  // Q4
  def parcours_1(i_ : Int = 0) : (Int, Int) = {

    (i_ / 9, i_ % 9)
  }

  def parcours_2(i_ : Int = 0) : Unit = {

    val matchingCoords = Sudoku.parcours_1(i_)
    matchingCoords match {

      case (9, 0) => println(s"Travail terminé.")
      case matchingCoords => {
        println(s"$i_ -> $matchingCoords")
        parcours_2(i_ + 1)
      }
    }
  }

  def getCoordsOfLine(numLine_ : Int) : Seq[(Int, Int)] = numLine_ match {

    case numLine_ : Int if(numLine_ >= 0 && numLine_ < 9) => {
      (0 until 9).map(v => (numLine_, v))
    }
  }

  def getCoordsOfColumn(numCol_ : Int) : Seq[(Int, Int)] = numCol_ match {

    case numCol_ : Int if(numCol_ >= 0 && numCol_ < 9) => {
      (0 until 9).map(v => (v, numCol_))
    }
  }

  def getCoordsOfBlock(coords_ : (Int, Int)) : Seq[(Int, Int)] = {

    val xBlock = coords_._1 -(coords_._1 % 3)
    val yBlock = coords_._2 -(coords_._2 % 3)

    (0 until 9).map(v => (v/3 + xBlock, v%3 + yBlock))
  }

  def main(args: Array[String]): Unit = {

    // Q1
    val table : $[$[Int]] = $(
      $(5, 3, 0, 0, 7, 0, 0, 0, 0),
      $(6, 0, 0, 1, 9, 5, 0, 0, 0),
      $(0, 9, 8, 0, 0, 0, 0, 6, 0),
      $(8, 0, 0, 0, 6, 0, 0, 0, 3),
      $(4, 0, 0, 8, 0, 3, 0, 0, 1),
      $(7, 0, 0, 0, 2, 0, 0, 0, 6),
      $(0, 6, 0, 0, 0, 0, 2, 8, 0),
      $(0, 0, 0, 4, 1, 9, 0, 0, 5),
      $(0, 0, 0, 0, 8, 0, 0, 7, 9)
    )

    // Q2
    println(Sudoku.TwoDimArrayToString(table) + "\n")

    // Q3
    val ntable = Sudoku.replaceByInTwoDimArray(table, (4, 5), -1)
    println(Sudoku.TwoDimArrayToString(ntable) + "\n")

    // Q4
    Sudoku.parcours_2()

    println(Sudoku.getCoordsOfBlock((8, 8)))
  }
}