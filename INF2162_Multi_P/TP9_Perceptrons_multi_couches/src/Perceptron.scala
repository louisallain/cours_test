import scala.{Array => $}

case class X(x_ : Array[Double]) // entree
case class Y(y_ : Array[Double]) // sortie

class Perceptron(couches_ : Array[Int]) {

  var outputsI = (0 until couches_.length).map(i => new Array[Double](couches_(i))).toArray
  var inputI = (0 until couches_.length).map( i => new Array[Double](couches_(i))).toArray
  var dI = (0 until couches_.length).map( i => new Array[Double](couches_(i))).toArray
  var poids = (1 until couches_.length).map( i => Array.ofDim[Double](couches_(i), couches_(i-1))).toArray

  this.poidsHasard();

  def poidsHasard() { // met au hasard les poids du réseau

    for( c <- this.poids.indices) // chaque couche
      for (n <- this.poids(c).indices) // chaque neurone
        for(p <- this.poids(c)(n).indices) this.poids(c)(n)(p) = 1-2*Math.random() // [-1.0, 1.0]
  }

  // active la couche d'entrée
  def apply(in_ : Array[Double]) : Array[Double] = {

    // fabrique la couche d'entrée
    for(i <- 0 until couches_(0)) {

      this.inputI(0)(i) = in_(i)
      this.outputsI(0)(i) = Perceptron.f(this.inputI(0)(i))
    }

    // fabrique les couches suivantes
    for(c <- 1 until couches_.length) {// chaque couche
      for (n <- 0 until couches_(c)) { // chaque neurone

        this.inputI(c)(n) = Perceptron.prodScal(this.poids(c - 1)(n), this.outputsI(c - 1))
        this.outputsI(c)(n) = Perceptron.f(this.inputI(c)(n))
      }
    }

    this.outputsI(couches_.length - 1)
  }

  def retroPropag(observe_ : Array[Double], souhaite_ : Array[Double], pas_ : Double = 0.1): Unit = {

    val iCSortie = couches_.length-1 // indice de la couche de sortie

    for(c <- 1 until couches_.length) { // chaque couche
      for (n <- 0 until couches_(c)) { // chaque neurone

        // calcul du dI
        if(c == iCSortie) { // cas particulier de la couche de sortie

          this.dI(c)(n) = 2*(observe_(n) - souhaite_(n))*Perceptron.fp(this.inputI(c)(n))
        } else {

          val h : Int = c + 1 // indice de la couche suivante
          var somme : Double = 0
          for(i <- 0 until couches_(h)) { // pour chaque neurone de la couche suivante
            somme = somme + this.dI(h)(i) * this.poids(c)(i)(n)
          }
          this.dI(c)(n) = somme * Perceptron.fp(this.inputI(c)(n))
        }

        // correction du poids
        for(j <- 0 until couches_(c-1)) { // pour chaque neurone de la couche précédente
          this.poids(c-1)(n)(j) = this.poids(c-1)(n)(j) - (pas_ * this.dI(c)(n) * this.outputsI(c-1)(j))
        }
      }
    }
  }

  def erreur(ex_ : List[Tuple2[X, Y]]) : Double = { // 3 Lignes maxi

    ex_.map{
      case (X(entree), Y(sortieSouhaitee)) => Perceptron.errQuad(sortieSouhaitee, this(entree))
    }.sum
  }

  def apprendreUneFois(ex_ : List[Tuple2[X, Y]]): Unit = {

    ex_.foreach{
      case (X(entree), Y(sortieSouhaitee)) => {
        this.retroPropag(this(entree), sortieSouhaitee)
      }
    }
  }
}

object  Perceptron {

  def f(x_ : Double) : Double = 1/(1 + Math.exp(-x_))

  def fp(x_ : Double) : Double = f(x_) * (1-f(x_))

  def prodScal(t1_ : Array[Double], t2_ : Array[Double]) : Double = {
    require(t1_.length == t2_.length, "Les vecteurs doivent être de la même taille.")

    t1_.zip(t2_).map{case (a,b) => a*b}.sum
  }

  def errQuad(t1_ : Array[Double], t2_ : Array[Double]) : Double = {
    require(t1_.length == t2_.length, "Les vecteurs doivent être de la même taille.")

    t1_.zip(t2_).map{case (a,b) => (a-b)*(a-b)}.sum
  }

  // 0: nb entrées, 1, 2, 3 ... : nb de neurones pour la cache x, n : nb neurones couche de sortie
  def apply(couches_ : Int*): Perceptron = {
    new Perceptron(couches_.toArray)
  }

  def main(args: Array[String]): Unit = {

    val tolerance = 0.01
    val dataAnd = List[Tuple2[X,Y]]((X($[Double](0,0,1)),Y($[Double](0))),(X($[Double](0,1,1)),Y($[Double](0))),(X($[Double](1,0,1)),Y($[Double](0))),(X($[Double](1,1,1)),Y($[Double](1))))
    val perceptron = Perceptron(3,1)
    var nbIt = 0
    var err = 1.0
    println("Start")
    while(err > tolerance){
      err = Math.abs(perceptron.erreur(dataAnd))
      perceptron.apprendreUneFois(dataAnd)
      nbIt = nbIt + 1
      println(err)
    }
    println(s"Done, nbIt : $nbIt")
    println(perceptron($[Double](0,0,1)).toList)
    println(perceptron($[Double](0,1,1)).toList)
    println(perceptron($[Double](1,0,1)).toList)
    println(perceptron($[Double](1,1,1)).toList)
  }
}