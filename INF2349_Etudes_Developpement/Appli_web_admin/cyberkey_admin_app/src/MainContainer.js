import React from 'react';
import './MainContainer.css'
import * as firebase from './utils/firebase_config'

import ConnectionPage from './ConnectionPage/ConnectionPage'
import HomePage from './HomePage/HomePage'

const CONNECTION_PAGE = "connection_page"
const HOME_PAGE = "home_page"

/**
 * Classe représentant le conteneur principale de l'application.
 * Gère la transition de l'état connecté / déconnecté de l'administrateur.
 */
class MainContainer extends React.Component {

  /**
   * Initialise l'état du composant.
   * @param {*} props propriétés héritées du parent.
   */
  constructor(props) {
    super(props);
    this.state = {
      user: null,
      currentPage: CONNECTION_PAGE, // par défaut
    }
  } 

  /**
   * Handler du bouton de déconnection.
   */
  handleLogoutButton = () => {
    let c = window.confirm("Voulez-vous vous déconnecter ?")
    if(c) {
      firebase.fbAuth.signOut()
      .then(() => console.log("Admin disconnected"))
      .catch((error) => console.error(error))
    }
  }

  /**
   * Fonction invoquée après le montage du composant.
   * Ajoute un listener sur l'état de connection.
   * Si l'admin se connecte, on va à la page d'accueil sinon on va à la page de connexion.
   */
  componentDidMount = () => {
    firebase.fbAuth.onAuthStateChanged((user) => { // observer sur l'état d'authentification de Firebase
      if(user) {
        this.setState({currentPage: HOME_PAGE, user: user}) // l'admin s'est connecté
      }
      else this.setState({currentPage: CONNECTION_PAGE, user: null}) // pas d'user connecté
    })
  }

  /**
   * Méthode de rendu du composant.
   */
  render() {
    switch(this.state.currentPage) {
      case CONNECTION_PAGE:
        return (
          <div>
            <ConnectionPage />
          </div>
        )
      case HOME_PAGE:
        return (
          <div>
            <HomePage
              user={this.state.user}
              handleLogoutButton={this.handleLogoutButton}
            />
          </div>
        )
      default: console.error("[MainContainer:render] System don't know what is the page to render.")
    }
  }
}

export default MainContainer;
