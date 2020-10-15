import React from 'react';
import './MainContainer.css'
import * as firebase from './utils/firebase_config'

import ConnectionPage from './ConnectionPage/ConnectionPage'
import HomePage from './HomePage/HomePage'

const CONNECTION_PAGE = "connection_page"
const HOME_PAGE = "home_page"

class MainContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      user: null,
      currentPage: CONNECTION_PAGE, // par défaut
    }
  } 

  handleLogoutButton = () => {
    firebase.fbAuth.signOut()
    .then(() => console.log("Admin disconnected"))
    .catch((error) => console.error(error))
  }

  componentDidMount = () => {
    firebase.fbAuth.onAuthStateChanged((user) => { // observer sur l'état d'authentification de Firebase
      if(user) this.setState({currentPage: HOME_PAGE, user: user}) // un user connecté
      else this.setState({currentPage: CONNECTION_PAGE, user: null}) // pas d'user connecté
    })
  }

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
