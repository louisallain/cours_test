function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
  }

// On part sur le principe que le modele va générer une grille aléatoire puis tant
// qu'il y a des explosions on continue à la remplir aléatoirement.
// Une fois qu'une grille correcte est produite, la vue pourra alors déclencher l'animation
// qui fait tomber les bonbons depuis le dessus.
class Modele {

    constructor(taille = 10) {

        this.taille = taille
        this.donnees = Array(this.taille).fill(Array(this.taille).fill(0))
        this.NB_ALIGNE = 3

        this.initialiseGrille()
    }

    /**
     * Rempli la grille aléatoirement tant qu'il peut y avoir
     * des explosions.
     */
    initialiseGrille() {
        
        for(let x in this.donnees) {
            for(let y in this.donnees[x]) {
                this.donnees[x][y] = getRandomInt(4)
            }
        }
    }

    /**
     * Retourne une liste de coordonnées des bonbons à exploser ou une liste vide
     * s'il n'y en a pas.
     */
    explosionPossible() {
        aExploser = []
        //for()
    }
}

var modele = new Modele()
console.log(modele.donnees)