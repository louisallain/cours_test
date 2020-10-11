import React from 'react';
import './MainContainer.css'
import * as firebase from './utils/firebase_config'

import ConnectionPage from './ConnectionPage/ConnectionPage';

class MainContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      user: null,
    };
  } 

  handleLogoutButton = () => {
    firebase.fbAuth.signOut().then(() => {
      console.log("Admin disconnected")
    }).catch(function(error) {
      console.error(error)
    });
  }

  componentDidMount = () => {
    firebase.fbAuth.onAuthStateChanged((user) => this.setState({user: user})); // observer sur l'état d'authentification de Firebase
  }

  render() {

    if(!this.state.user) { // admin non connecté
      return (
        <div>
          <ConnectionPage/>
        </div>
      );
    } 
    else { // admin connecté
      return (
        <div>
          <p>Bienvenue {this.state.user.email}</p>
          <button onClick={this.handleLogoutButton}>
            Se déconnecter
        </button>
        </div>
      )
    }
  }
}

export default MainContainer;
