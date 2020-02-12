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
}

class Modele {

  constructor(taille = 10) {

      this.taille = taille
      this.NB_ALIGNE = 3 // le nbre mini de bonbons à aligner pour les éclater
      this.donnees = []
      this.initialiseGrille()
      this.score = 0
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
   */
  explose() {

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
        this.score = this.score + 1
        
        // fait tomber la colonne
        let col = colonneDe(this.donnees, y)
        if(x > 0) {
          let tmp = col.slice(0, x)
          col.splice(1, tmp.length, ...tmp) 
        }
        // remplace le 0 créé parce que la colonne est tombée par un nouveau bonbon
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
   * L'échange est possible ssi deux cases sont côte à côte et que ce sont les même bonbons
   * @param {Number} x1 x de la 1ere case
   * @param {Number} y1 y de la 1ere case
   * @param {Number} x2 x de la 2nde case
   * @param {Number} y2 y de la 2nde case
   * @param {Function} callback fonction exécutée après avoir fait l'échange
   * @return {Boolean} vrai si l'échange est effectif et donc s'il y a des explosions à faire
   */
  echange2Cases(x1, y1, x2, y2) {

    if(x1 == x2 && y1 == y2) {
      return true
    }
    // vérifie que les cases sont côte à côte
    else if((x2 == x1 && y2 == y1-1) || (x2 == x1-1 && y2 == y1) || (x2 == x1 && y2 == y1+1) || (x2 == x1+1 && y2==y1)) {

      // vérifie que l'échange produit bien une explosion
      let tmp = this.donnees[x1][y1]
      this.donnees[x1][y1] = this.donnees[x2][y2]
      this.donnees[x2][y2] = tmp
      
      if(this.explosionPossible().length > 0) {
        return true
      } else {
        let tmp = this.donnees[x1][y1]
        this.donnees[x1][y1] = this.donnees[x2][y2]
        this.donnees[x2][y2] = tmp
        return false
      }
    } else {
      return false
    }
  }
}

class Vue {

  constructor(modele, canevas, tailleBonbon) {

    this.modele = modele
    this.canevas = canevas
    this.tailleBonbon = tailleBonbon
    this.grille = []

    // fixe la taille du canevas en fonction de la taille et du nombre de bonbon
    this.canevas.width = this.modele.donnees.length * this.tailleBonbon
    this.canevas.height = this.modele.donnees.length * this.tailleBonbon

    this.dessineLaGrille()   
    this.afficheScore()
  }

  /**
   * Dessine un simple carré gris accueillant le jeu.
   */
  dessineLaGrille() {
    this.canevas.getContext('2d').fillStyle = "gray"
    this.canevas.getContext('2d').fillRect(0, 0, this.canevas.width, this.canevas.height)
  }

  afficheScore() {
    document.getElementById("score").innerHTML = `Score : ${this.modele.score}`
  }

  /**
   * Met à jour le canevas en fonction des données présentes dans le modèle.
   * Sert surtout pour l'initialisation.
   */
  metAJourAPartirDuModele() {
    
    return new Promise((resolve, reject) => {

      let tmpBoard = []

      for(let x in this.modele.donnees) {

        tmpBoard[x] = Array()
        for(let y in this.modele.donnees[x]) {
          
          let bonbonTmp = new Bonbon(this.modele.donnees[x][y], this.tailleBonbon)
          tmpBoard[x][y] = bonbonTmp
          this.dessineUnBonbonA(bonbonTmp, x * this.tailleBonbon, y * this.tailleBonbon)
        }
      }
      this.grille = tmpBoard
    })
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

    let t = this.tailleBonbon
    if(!bonbon.image.complete) {
      bonbon.image.onload = () => {
        this.canevas.getContext('2d').drawImage(bonbon.image, y, x, t, t)
      }
    } else {
      this.canevas.getContext('2d').drawImage(bonbon.image, y, x, t, t)
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
    this.canevas.getContext('2d').fillRect(y, x, t, t)
  }

  /**
   * Descend une colonne avec animation ssi il y a au moins un 0 (pas de bonbon) dans la colonne
   * @param {Number} y y de la colonne dans la grille
   * @param {Array} colonne les données de la colonne à faire descendre
   */
  descendColonne(y, colonne) {
      
    for(let i in colonne) {
      
      
      if(colonne[i] == 0) {
        console.log(i)
        for(let j = i; ; (j-1) >= 0) {
          
          if((j-1) >= 0) {
            this.bougeBonbon(j-1, y, j, y, 10, 100)
            j = j - 1
          } else break;
        }
      }
    }
  }

  /**
   * Bouge un bonbon
   * @param {Number} x0 x de départ
   * @param {Number} y0 y de départ
   * @param {Number} x1 x d'arrivée
   * @param {Number} x2 y d'arrivée
   * @param {Number} pas le nombre de pixel par pas (<= tailleBonbon)
   * @param {Number} vitesse la vitesse en ms entre chaque pas
   */
  bougeBonbon(x0, y0, x1, y1, pas, vitesse) {
    
    return new Promise((resolve, reject) => {

      this.canevas.getContext('2d').fillStyle = "gray"
      let t = this.tailleBonbon
      let bonbon = this.grille[x0][y0]
      let currX = x0 * t
      let currY = y0 * t
      let that = this
      if(pas > t) pas = t
    
      let interId = setInterval(() => {

        
        // efface à la position courante
        that.effaceUnBonbon(currX, currY)

        // dessine à la position suivante
        if(x0 != x1) currX = currX + pas
        if(y0 != y1) currY = currY + pas
        if(currX >= (x1 * t) && currY >= (y1 * t)) {
          that.dessineUnBonbonA(bonbon, x1 * t, y1 * t)
          clearInterval(interId)
        } else {
          that.dessineUnBonbonA(bonbon, currX, currY)
        }
        
      }, vitesse)
    })
  }

  /**
   * Sélectionne un bonbon dans la grille. 
   * Rend la case plus foncé.
   * @param {Number} x x du bonbon
   * @param {Number} y y du bonbon
   */
  selectionneBonbon(x, y) {

    return new Promise((resolve, reject) => {

      let t = this.tailleBonbon
      let currX = x * t
      let currY = y * t
      this.grille[x][y].estSelectionne = true
      
      this.canevas.getContext('2d').globalAlpha = 0.5
      this.canevas.getContext('2d').fillStyle = "gray"
      this.canevas.getContext('2d').fillRect(currY, currX, t, t)
      this.canevas.getContext('2d').globalAlpha = 1.0
    })
  }

  /**
   * Déselectionne un bonbon.
   * @param {Number} x x du bonbon
   * @param {Number} y y du bonbon
   */
  deselectionneBonbon(x, y) {

    return new Promise((resolve, reject) => {

      let t = this.tailleBonbon
      let currX = x * t
      let currY = y * t
      this.grille[x][y].estSelectionne = false
      
      this.effaceUnBonbon(x*t, y*t)
      this.dessineUnBonbonA(this.grille[x][y], x*t, y*t)
    })
  }
}

class Controleur {

  constructor(tailleJeu, tailleBonbon) {

    this.tailleJeu = tailleJeu
    this.tailleBonbon = tailleBonbon
    this.modele = new Modele(tailleJeu)
    this.vue = new Vue(this.modele, document.getElementById("candy_canevas"), this.tailleBonbon)
    this.bonbonsSelecionne = []

    this.modele.explose()
    this.vue.metAJourAPartirDuModele().then(this.autoriseClick())
  }

  /**
   * Gère le clique souris dans l'écran
   * @param {Event} event évènement généré par le click.
   */
  click(event, that) {

    let y = event.pageX - event.target.offsetLeft
    let x = event.pageY - event.target.offsetTop

    // Met en foncé la case sélectionné
    let iX = Math.trunc(x / that.tailleBonbon)
    let iY = Math.trunc(y / that.tailleBonbon)
    if(iX < that.tailleJeu && iY < that.tailleJeu) {
      
      if(that.vue.grille[iX][iY].estSelectionne) {
        that.vue.deselectionneBonbon(iX, iY)
      } else {
       
        if(that.bonbonsSelecionne.length < 2) {

          that.vue.selectionneBonbon(iX, iY)
          that.bonbonsSelecionne.push({x: iX, y: iY})
        } else {

          that.bonbonsSelecionne.map(coords => that.vue.deselectionneBonbon(coords.x, coords.y))
          that.bonbonsSelecionne = []
        }

        if(that.bonbonsSelecionne.length == 2) {

          if(that.modele.echange2Cases(that.bonbonsSelecionne[0].x, that.bonbonsSelecionne[0].y, that.bonbonsSelecionne[1].x, that.bonbonsSelecionne[1].y)) {

            that.vue.dessineLaGrille()
            that.modele.explose()
            that.vue.metAJourAPartirDuModele()
            that.vue.afficheScore()
            that.bonbonsSelecionne.map(coords => that.vue.deselectionneBonbon(coords.x, coords.y))
            that.bonbonsSelecionne = []
          } else {

            that.bonbonsSelecionne.map(coords => that.vue.deselectionneBonbon(coords.x, coords.y))
            that.bonbonsSelecionne = []
          }
        }
      }
    }
  }

  /**
   * Autorise les clicks.
   */
  autoriseClick() {
    document.addEventListener("click", () => this.click(event, this))
  }

  /**
   * Interdit les clicks.
   */
  interditClique() {
    document.removeEventListener("click", this.click) 
  }
}

var jeu = new Controleur(30, 25)
