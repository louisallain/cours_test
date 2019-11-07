import scala.reflect.ClassTag;

object Essai {

    def basique() : Unit = {
        println("\u001b[41m");
        print("fond rouge");
        println("\u001b[0m");
    }

    def essaiAnsi() : Unit = {
        println("coucou" + Ansi.F_MAGENTA + Ansi.CYAN + "totu" + Ansi.RESET);
    }

    def essaiPieceColToString() : Unit = {
        val cavalier = new PieceCol("Cavalier", Ansi.JAUNE);
        println(cavalier);
        val defaut = new PieceCol("Defaut");
        println(defaut);
    }

    def essaiEchiquierPlacerEn() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e.placerEn(cavalier, 0, 0);
    }

    def essaiEchiquierUpdate() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e((1, 2)) = cavalier;
        e.tmpPrint();
    }

    def essaiEchiquierApply() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e((1, 2)) = cavalier;
        val expCavalier = e(1, 2)
        println(expCavalier)
    }

    def essaiEchiquiertmpPrint() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e((1, 2)) = cavalier;
        e.tmpPrint();
    }

    def essaiEchiquierVideLaCase() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e((1, 2)) = cavalier;
        e.videLaCase(1, 2);
        e.tmpPrint();
    }

    def essaiEchiquierVider() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        val cavalier : PieceCol = new PieceCol("Cavalier", Ansi.JAUNE);
        e((1, 2)) = cavalier;
        e((3, 4)) = cavalier;
        e((5, 6)) = cavalier;
        e.vider();
        e.tmpPrint();
    }

    def essaiEchiquierToString() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        e((0, 6)) = new PieceCol("Cavalier");
        e((3, 5)) = new PieceCol("Dame", Ansi.ROUGE);
        e((6, 3)) = new PieceCol("Fou", Ansi.VERT);
        e((5, 2)) = new PieceCol("Pion", Ansi.CYAN);
        print(e);
    }

    def essaiEchiquierQuestion15() : Unit = {
        val e : Echiquier[PieceCol] = new Echiquier();
        e((1, 0)) = Pion();
        e((1, 1)) = Pion();
        e((1, 2)) = Pion();
        e((1, 3)) = Pion();
        e((1, 4)) = Pion();
        e((1, 5)) = Pion();
        e((1, 6)) = Pion();
        e((1, 7)) = Pion();

        e((0, 0)) = Tour();
        e((0, 1)) = Cavalier();
        e((0, 2)) = Fou();
        e((0, 3)) = Dame();
        e((0, 4)) = Roi();
        e((0, 5)) = Fou();
        e((0, 6)) = Cavalier();
        e((0, 7)) = Tour();
        print(e);
    }

    def main(args: Array[String]): Unit = {

		// Essai.basique();
        // Essai.essaiAnsi();
        // Essai.essaiPieceColToString();
        // Essai.essaiEchiquierPlacerEn();
        // Essai.essaiEchiquierUpdate();
        // Essai.essaiEchiquierApply();
        // Essai.essaiEchiquiertmpPrint();
        // Essai.essaiEchiquierVideLaCase();
        // Essai.essaiEchiquierVider();
        // Essai.essaiEchiquierToString();
        Essai.essaiEchiquierQuestion15();
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
