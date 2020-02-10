function getRandomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}

/**
 * Trouve toutes les séquences de nombres (i.e [1, 1, 1]) dans un tableau.
 * Renvoie un ensemble de suite d'indices.
 * @param {number} tableau tableau dans lequel chercher
 * @param {number} nbOccMin le nombre d'occurences min
 * @param {number} ElemToIgnore les éléments à ignorer
 */
function trouveSequenceNombreDans(tab, nbOccMin, ...ElemToIgnore) {

  let pile = []
  let ret = []

  tab.map((elem, index) => {
    
    if(![...ElemToIgnore].includes(elem)) {

      if(pile.length == 0) {
        pile.push({elem, index})
      }
      else {

        if(elem == pile[0].elem) {
          pile.push({elem, index})
          if(index == tab.length-1 && pile.length >= nbOccMin) {
            pile.map(x => {ret.push(x.index)})
          }
        } 
        else {
          if(pile.length >= nbOccMin) {
            pile.map(x => {ret.push(x.index)})
          } 
          pile = []
          pile.push({elem, index})
        }
      }
    }
  })

  return ret
}

/**
 * Retourne la colonne d'une matrice.
 * @param {Array} matrice La matrice depuis laquelle extraire la colonne
 * @param {Number} n Le numéro de la colonne à extraire
 */
function colonneDe(matrice, n) {
  return matrice.map(x => x[n])
}

class Bonbon {

  constructor(type, taille) {

    this.type = type
    this.taille = taille
    this.image = new Image()
    this.estSelectionne = false

    switch (type) {
      case 1:
        this.image.src = './images/Blue.png'
        break
      case 2:
        this.image.src = './images/Green.png'
        break
      case 3:
        this.image.src = './images/Orange.png'
        break
      case 4:
        this.image.src = './images/Red.png'
        break
      case 5:
        this.image.src = './images/Yellow.png'
        break
    }
  }

  /**
   * Donne une nouvelle position au bonbon.
   * @param {Number} x nouveau x pour le bonbon
   * @param {Number} y nouveau y pour le bonbon
   */
  positionneEn(x, y) {

  }
}

class Modele {

  constructor(taille = 10) {

      this.taille = taille
      this.NB_ALIGNE = 2 // le nbre mini de bonbons à aligner pour les éclater
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
              tmpBoard[i][j] = getRandomInt(5) + 1
          }
      }
      this.donnees = tmpBoard
  }

  /**
   * Retourne une liste de coordonnées des bonbons à exploser ou une liste vide
   * s'il n'y en a pas.
   * @return une liste de coordonnées de bonbons à exploser
   */
  explosionPossible() {
      
    let aExploser = [] // liste de coordonnées à exploser

    // Regarde les lignes
    this.donnees.map((row, indexOfRow) => {
      trouveSequenceNombreDans(row, this.NB_ALIGNE, 0).map(index => {
        aExploser.push({x: indexOfRow, y: index})
      })
    })

    // Regarde les colonnes
    for(let i in this.donnees) {
      trouveSequenceNombreDans(colonneDe(this.donnees, i), this.NB_ALIGNE, 0).map(index => {
        aExploser.push({x: index, y: Number(i)})
      })
    }
    
    return aExploser
  }

  /**
   * Explose tous les "bonbons" qui sont alignés dans le modèle.
   * @param {Function} callback Fonction appellée après avoir explosé un bonbon
   */
  explose(callback) {

    let coordsAExploser = this.explosionPossible()
    let cpt = 0 // compte le nombre d'itérations pour parvenir à une grille stable

    while(coordsAExploser.length > 0) { // tant qu'il reste des explosions à faire
      
      cpt++
      coordsAExploser
      .filter((e, i) => {
        return coordsAExploser.findIndex((tmp) => {
          return tmp.x == e.x && tmp.y == e.y 
        }) == i
      })
      .sort((a, b) => a.x - b.x || a.y - b.y)
      .map((coords) => {
        
        let x = coords.x
        let y = coords.y
        this.donnees[x][y] = 0 // vide la case

        // exécute la méthode de callback si besoin
        if(callback) callback(coords)
        
        let col = colonneDe(this.donnees, y)
        if(x > 0) {
          let tmp = col.slice(0, x)
          col.splice(1, tmp.length, ...tmp) 
        }
        col[0] = getRandomInt(5) + 1

        // remplace la colonne
        for(let i in col) this.donnees[i][y] = col[i]
      })
      coordsAExploser = this.explosionPossible() 
    }
    console.info(`[Modele.explose] nb itérations : ${cpt}`)
  }

  /**
   * Echange deux cases dans la grille du modèle.
   * L'échange est possible ssi deux cases sont côte à côte.
   * @param {Number} x1 x de la 1ere case
   * @param {Number} y1 y de la 1ere case
   * @param {Number} x2 x de la 2nde case
   * @param {Number} y2 y de la 2nde case
   * @param {Function} callback fonction exécutée après avoir fait l'échange
   */
  echange2Cases(x1, y1, x2, y2, callback) {

    if(x1 == x2 && y1 == y2) {
      console.info(`[Modele.echange2Case] c'est la même case`)
    }
    else if((x2 == x1 && y2 == y1-1) || (x2 == x1-1 && y2 == y1) || (x2 == x1 && y2 == y1+1) || (x2 == x1+1 && y2==y1)) {
      let tmp = this.donnees[x1][y1]
      this.donnees[x1][y1] = this.donnees[x2][y2]
      this.donnees[x2][y2] = tmp
      if(callback) callback()
    } else {
      console.error(`[Modele.echange2Cases] échange impossible`)
    }
  }
}

class Vue {

  constructor(modele, canevas, tailleBonbon) {

    this.modele = modele
    this.canevas = canevas
    this.tailleBonbon = tailleBonbon

    // fixe la taille du canevas en fonction de la taille et du nombre de bonbon
    this.canevas.width = this.modele.donnees.length * this.tailleBonbon
    this.canevas.height = this.modele.donnees.length * this.tailleBonbon

    // optionnel
    this.canevas.getContext('2d').fillStyle = "gray"
    this.canevas.getContext('2d').fillRect(0, 0, this.canevas.width, this.canevas.height)
  }

  /**
   * Met à jour le canevas en fonction des données présentes dans le modèle.
   * Sert surtout pour l'initialisation.
   */
  metAJourAPartirDuModele() {
    
    for(let x in this.modele.donnees) {
      
      for(let y in this.modele.donnees[x]) {
        
        let bonbonTmp = new Bonbon(this.modele.donnees[x][y], this.tailleBonbon)
        this.dessineUnBonbonA(bonbonTmp, x * this.tailleBonbon, y * this.tailleBonbon)
      }
    }
  }

  /**
   * Dessine un bonbon dans le canevas.
   * Les coordonnées en paramètres sont des entier, la méthode se charge de faire la conversion
   * en fonction de la taile de la représentation d'un bonbon (ie son image).
   * @param {Bonbon} bonbon le bonbon à dessiner
   * @param {Number} x x du bonbon à dessiner
   * @param {Number} y y du bonbon à dessiner
   */
  dessineUnBonbonA(bonbon, x, y) {

    bonbon.image.onload = () => {
      let t = this.tailleBonbon
      this.canevas.getContext('2d').drawImage(bonbon.image, x , y, t, t)
    }
  }

  /**
   * Vide une case de la grille (détruit un bonbon).
   * @param {Number} x x de la case à vider
   * @param {Number} y y de la case à vider
   */
  effaceUnBonbon(x, y) {
    let t = this.tailleBonbon
    this.canevas.getContext('2d').fillStyle = "gray"
    this.canevas.getContext('2d').fillRect(x, y, t, t)
  }

  /**
   * Descend une colonne avec animation ssi il y a au moins un 0 (pas de bonbon) dans la colonne
   * @param {Number} y y de la colonne dans la grille
   * @param {Array} colonne les données de la colonne à faire descendre
   */
  descendColonne(y, colonne) {

  }

  /**
   * 
   * @param {Number} x0 x de départ
   * @param {Number} y0 y de départ
   * @param {Number} x1 x d'arrivée
   * @param {Number} x2 y d'arrivée
   * @param {Number} pas le nombre de pixel par pas (<= tailleBonbon)
   */
  bougeBonbon(x0, y0, x1, y1, pas) {
    
    this.canevas.getContext('2d').fillStyle = "gray"
    let t = this.tailleBonbon
    let bonbon = new Bonbon(this.modele.donnees[x0][y0], t)
    let currX = x0 * t
    let currY = y0 * t
    if(pas > t) pas = t

    let interId = setInterval(() => {
      
      // efface à la position courante
      this.effaceUnBonbon(currX, currY)

      // dessine à la position suivante
      if(x0 != x1) currX = currX + pas
      if(y0 != y1) currY = currY + pas
      if(currX >= (x1 * t) && currY >= y1 * t) {
        console.log((x1 * t))
        this.dessineUnBonbonA(bonbon, x1 * t, y1 * t)
        clearInterval(interId)
      } else {
        console.log("ok")
        this.dessineUnBonbonA(bonbon, currX, currY)
      }
    }, 100)
  }
}

var canevas = document.getElementById("candy_canevas")

var modele = new Modele(4)
console.table(modele.donnees)

var tailleBonbon = 50

var vue = new Vue(modele, canevas, tailleBonbon)

var tmpColonne = [1, 1, 0, 1, 1]

var bonbon1 = new Bonbon(1, tailleBonbon)
vue.dessineUnBonbonA(bonbon1, 1 * tailleBonbon, 1 * tailleBonbon)

setTimeout(() => {
  vue.bougeBonbon(1, 1, 1, 2, 10)
}, 1000)
