import scala.reflect.ClassTag;

class CavalierEuler (nbCote_ : Int = 8) {

    val nbCote = nbCote_;
    val modele = Array.fill[Int](this.nbCote, this.nbCote)(-1);
    val heuristique = Array.fill[Int](this.nbCote, this.nbCote)(-1);
    val vue : Echiquier[PieceCol] = new Echiquier(this.nbCote);
    var etape : Int = 1;

    def controleur(x_ : Int, y_ : Int): Unit = {

        for {
            i <- 0 to this.nbCote - 1
            j <- 0 to this.nbCote - 1
        } this.heuristique(i)(j) = trouverDeplacementsCavalier((i, j)).size;

        this.modele(x_)(y_) = 0;
        trouvePositions((x_, y_), this.etape);
        synchroniserVueAuModele();
        
        def trouverDeplacementsCavalier(xy_ : Tuple2[Int, Int]) : List[Tuple2[Int, Int]] = {
            
            val x0 = xy_._1;
            val y0 = xy_._2;

            var ret : List[Tuple2[Int, Int]] = (x0-1, y0-2)::(x0-2, y0-1)::(x0-2, y0+1)::(x0-1, y0+2)::(x0+1, y0-2)::(x0+2, y0-1)::(x0+2, y0+1)::(x0+1, y0+2)::List();
            ret =  ret
                    .filter(xy =>  xy._1 >= 0 && xy._1 < this.nbCote && xy._2 >= 0 && xy._2 < this.nbCote)
            return ret;
        }

        def trouvePositions(xy_ : Tuple2[Int, Int], etape_ : Int) : Boolean = {
            
            // solution trouvée
            if((this.nbCote*this.nbCote) == etape_) {
                return true;
            }
            else {

                trouverDeplacementsCavalier(xy_)
                    .filter(coords1 => this.modele(coords1._1)(coords1._2) == -1)
                    .sortBy(coords3 => this.heuristique(coords3._1)(coords3._2))
                    .map(coords2 => {
                        this.modele(coords2._1)(coords2._2) = etape_;
                        if(trouvePositions(coords2, etape_ + 1)) {
                            return true;
                        }
                        this.modele(coords2._1)(coords2._2) = -1;
                    });
                return false;
            }
        }

        def synchroniserVueAuModele() : Unit = {
            for {
                i <- 0 to this.nbCote_ - 1
                j <- 0 to this.nbCote_ - 1
            } this.vue((i, j)) = new PieceCol(this.modele(i)(j).toString);
        }
    }

    override def toString() : String = {
        return this.vue.toString;
    }
}

object CavalierEuler {

    def main(args: Array[String]): Unit = {
        
        val ce = new CavalierEuler(8);

        val start = System.currentTimeMillis;
        ce.controleur(0, 0);
        val total = System.currentTimeMillis - start;
        println("time : " + total + " ms");

        println(ce);
	}
}

object Ansi {

    val NOIR : String = "\u001b[30m";
    val F_NOIR : String = "\u001b[40m";

    val BLEU : String = "\u001b[34m";
    val F_BLEU : String = "\u001b[44m";

    val CYAN : String = "\u001b[36m";
    val F_CYAN : String = "\u001b[46m";

    val VERT : String = "\u001b[32m";
    val F_VERT : String = "\u001b[42m";

    val MAGENTA : String = "\u001b[35m";
    val F_MAGENTA : String = "\u001b[45m";

    val ROUGE : String = "\u001b[31m";
    val F_ROUGE : String = "\u001b[41m";

    val BLANC : String = "\u001b[37m";
    val F_BLANC : String = "\u001b[47m";

    val JAUNE : String = "\u001b[33m";
    val F_JAUNE : String = "\u001b[43m";

    val RESET : String = "\u001b[0m";
}   

class Echiquier[Piece : ClassTag] (cote_ : Int = 8) {

    private val cote : Int = cote_;
    private val plateau = Array.ofDim[Option[Piece]](cote_, cote_);

    this.vider();

    def placerEn(piece_ : Piece, x_ : Int, y_ : Int) : Unit = {
        
        if(x_ < this.cote && y_ < this.cote) this.plateau(x_)(y_) = Some(piece_);
        else println("Mauvaises coordonnées");
    }

    def update(coupleXY_ : Tuple2[Int, Int], piece_ : Piece) = {
        this.placerEn(piece_, coupleXY_._1, coupleXY_._2);
    }

    def apply(x_ : Int, y_ : Int) : Option[Piece] = {
        val piece = this.plateau(x_)(y_)
        return piece
    }

    def videLaCase(x_ : Int, y_ : Int) = {
        this.plateau(x_)(y_) = None
    }

    def vider() = {
        for( i <- 0 to this.cote -1 ; j <- 0 to this.cote -1) this.videLaCase(i,j)
    }

    override def toString() : String = {
        var ret : String = "";
        // indice lignes
        for(i <- 0 to this.cote -1) ret += " " * 5 + i;
        ret += "\n";
        // bande du haut
        ret += (" " * 2) + Ansi.F_BLEU + ((" " * 6) * this.cote) + " " + Ansi.RESET;
        ret += "\n";
        
        for(i <- 0 to this.cote -1) {
            ret += " " + i;
            for(j <- 0 to this.cote -1) {
                val pieceCourante = this.plateau(i)(j);
                var nomPieceCourante = " " * 5;
                if(pieceCourante != None) {
                    val Some(p : PieceCol) = this.plateau(i)(j);
                    nomPieceCourante = p.toShorterString(5);
                    nomPieceCourante += " " * (5 - p.getEtiquetteLength);    
                }
                ret += Ansi.F_BLEU + " " + Ansi.RESET + nomPieceCourante;
            }
            ret += Ansi.F_BLEU + " " + Ansi.RESET;
            ret += "\n";
            ret += (" " * 2) + Ansi.F_BLEU + ((" " * 6) * this.cote) + " " + Ansi.RESET;
            ret += "\n";
        }

        return ret;
    }

    // TODO : supprimer cette méthode plus tard
    // uniquement pour le debug.
    def tmpPrint() : Unit = {
        this.plateau foreach { row => row foreach print; println }
    }
}

trait Piece {

}

class PieceCol(etiquette_ : String, codeAnsi_ : String = Ansi.BLANC) extends Piece {

    private var etiquette : String = etiquette_;
    private var codeAnsi : String = codeAnsi_;

    override def toString = this.codeAnsi + this.etiquette + Ansi.RESET;

    def toShorterString(sizeWanted_ : Int) : String = {
        if(sizeWanted_ > this.etiquette.length) {
            this.toString();
        }
        else {
            this.codeAnsi + this.etiquette.slice(0, sizeWanted_) + Ansi.RESET;
        }
    }

    def getEtiquetteLength = this.etiquette.length;

}

object Cavalier {

    def apply() : PieceCol = {
        return new PieceCol("Cavalier", Ansi.JAUNE);
    }
}

object Dame {

    def apply() : PieceCol = {
        return new PieceCol("Dame", Ansi.ROUGE);
    }
}

object Fou {

    def apply() : PieceCol = {
        return new PieceCol("Fou", Ansi.BLEU);
    }
}

object Roi {

    def apply() : PieceCol = {
        return new PieceCol("Roi", Ansi.MAGENTA);
    }
}

object Tour {

    def apply() : PieceCol = {
        return new PieceCol("Tour", Ansi.VERT);
    }
}

object Pion {

    def apply() : PieceCol = {
        return new PieceCol("Pion", Ansi.BLANC);
    }
}
