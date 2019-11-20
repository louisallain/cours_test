// Q1
import java.util

import scala.{Array => $}
import scala.reflect.ClassTag

object Sudoku {

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

  def parcours_5(i_ : Int = 0): Unit = {

    val (x, y) = Sudoku.parcours_1(i_)
    (x, y) match {
      case(9, 0) => println("Travail terminé.")
      case(x, y) => {
        println(s"($x, $y) : ")
        val xBlock = x -(x % 3)
        val yBlock = y -(y % 3)
        def indicesAVoir(j_ : Int) = List((x, j_), (j_, y), (j_ / 3 + xBlock, j_ % 3 + yBlock))
        println((0 until 9).flatMap(indicesAVoir).toSet)
        Sudoku.parcours_5(i_ + 1)
      }
    }
  }

  def parcours_7(i_ : Int = 0): Unit = {

    val (x, y) = Sudoku.parcours_1(i_)
    (x, y) match {
      case(9, 0) => println("Travail terminé.")
      case(x, y) => {
        println(s"($x, $y) : ")
        val xBlock = x -(x % 3)
        val yBlock = y -(y % 3)
        def nombresDejaPris(j_ : Int) = List(table(x)(j_), table(j_)(y), table(j_ / 3 + xBlock)(j_ % 3 + yBlock))
        println("Deja pris : " + (0 until 9).flatMap(nombresDejaPris).toSet)
        Sudoku.parcours_7(i_ + 1)
      }
    }
  }

  def parcours_9(i_ : Int = 0): Unit = {

    val (x, y) = Sudoku.parcours_1(i_)
    (x, y) match {
      case(9, 0) => println("Travail terminé.")
      case(x, y) => {
        println(s"($x, $y) : ")
        val xBlock = x -(x % 3)
        val yBlock = y -(y % 3)
        def nombresDejaPris(j_ : Int) = List(table(x)(j_), table(j_)(y), table(j_ / 3 + xBlock)(j_ % 3 + yBlock))
        val dejaPris = (0 until 9).flatMap(nombresDejaPris).toSet
        val aEssayer = (0 to 9).diff(dejaPris.toSeq)

        println("A essayer : " + aEssayer)
        Sudoku.parcours_9(i_ + 1)
      }
    }
  }

  def parcours_11(t_ : Array[Array[Int]], i_ : Int = 0) : Option[Array[Array[Int]]] = {

    val (x, y) = (i_ / 9, i_ % 9)
    (x, y) match {
      case(9, 0) => Some(t_)
      case(x, y) => {
        parcours_11(t_.updated(x, t_(x).updated(y, i_)), i_ + 1)
      }
    }
  }

  /**
   * Solution sudoku ; fonctionel
   * @param t_ sudoku
   * @param i_ n° case
   * @return solution
   */
  def parcours_12(t_ : Array[Array[Int]], i_ : Int = 0) : Option[Array[Array[Int]]] = {

    val (x, y) = (i_ / 9, i_ % 9)
    (x, y) match {

      case(9, 0) => {
        Some(t_)
      } // renvoie le résultat qui est le tableau courant t_
      case(x, y) if t_(x)(y) != 0 => Sudoku.parcours_12(t_, i_ + 1) // case déjà occupée : on passe à la case suivante, inutile de modifier celle-ci
      case(x, y) => { // sinon, on est sur une case et on va devoir essayer plusieurs numéros

        val xBlock = x -(x % 3)
        val yBlock = y -(y % 3)
        def nombresDejaPris(j_ : Int) = List(t_(x)(j_), t_(j_)(y), t_(j_ / 3 + xBlock)(j_ % 3 + yBlock))
        val dejaPris = (0 until 9).flatMap(nombresDejaPris).toSet
        val aEssayer = (0 to 9).diff(dejaPris.toSeq)
        def placer(v_ : Int) = parcours_12(t_.updated(x, t_(x).updated(y, v_)), i_ + 1)

        if (aEssayer.isEmpty) None // pas de solution (rien à essayer)
        else {
          val s = aEssayer.map(placer).filter(_ != None)
          if(s.length > 0) s(0) else None
        }
      }
    }
  }

  def permute(m_ : String) : List[String] = {
    m_ match {
      case(m_) if(m_.length == 1) => List(m_)
      case(m_) => {
        m_.map(l1 => permute(m_.toSeq.diff(l1.toString).toString).map(reste => l1+reste)).toList.flatten
      }
    }
  }

  def main(args: Array[String]): Unit = {

    // SUDOKU
    parcours_12(table) match {
      case Some(res) => println("12: \n"+ res.map( _.mkString(" ") ).mkString("\n"))
      case None => println("pas de solution")
    }

    // ANAGRAMME
    println(permute("aka"))
    println(permute("food"))
    println(permute("cloaque"))
  }
}