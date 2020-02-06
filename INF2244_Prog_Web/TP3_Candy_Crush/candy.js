

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
        this.NB_ALIGNE = 3 // le nbre mini de bonbons à aligner pour les éclater
        this.donnees = []
        this.initialiseGrille()
    }

    /**
     * Rempli la grille aléatoirement tant qu'ilgetRandomInt peut y avoir
     * des explosions.
     */
    initialiseGrille() {
        
        let tmpBoard = []
        for(let i = 0 ; i < this.taille ; i++){
            tmpBoard[i] = Array()
            for(let j = 0 ; j < this.taille ; j++){
                tmpBoard[i][j] = getRandomInt(4) + 1
            }
        }
        this.donnees = tmpBoard
    }

    /**
     * Retourne une liste de coordonnées des bonbons à exploser ou une liste vide
     * s'il n'y en a pas.
     */
    explosionPossible() {
        aExploser = [] // liste de coordonnées à exploser
        
    }
}

var modele = new Modele()
console.table(modele.donnees)