import React from 'react';
import * as firebase from '../utils/firebase_config';
import './ConnectionPage.css'

class ConnectionPage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      email: '',
      passwordText: '',
    };
  }

  /**
   * Vérifie toutes les conditions pour que l'admin se connecte :
   * - vérifie que c'est bien l'email de l'admin et son mot de passe
   */
  handleLoginButton = () => {

    firebase.fbDatabase.ref('admin_email').once('value', (snapshot) => {
      if(this.state.email === snapshot.val()) { // si l'email saisi correspond à l'email de l'admin sauvegardé sur la bdd
        
        firebase.fbAuth.signInWithEmailAndPassword(this.state.email, this.state.passwordText).catch((error) => {
          console.error(error)
          if(error.code === "auth/wrong-password") {
            window.alert("Mauvais mot de passe !")
          }
        });
      } 
      else {
        window.alert("Seule l'adresse mail de l'utilisateur peut être utilisé pour se connecter à cette application.")
      }
    })
  } 

  /**
   * Envoie un mail à l'email de l'admin afin de réinitialiser le mot de passe.
   */
  handleResetAdminPassword = () => {
    firebase.fbDatabase.ref('admin_email').once('value', (snapshot) => {
      firebase.fbAuth.sendPasswordResetEmail(snapshot.val()).then(() => {
        alert("Un email de réinitialisation de mot de passe a été envoyé à l'adresse mail de l'administrateur.")
      }).catch((error) => console.error(error))
    })
  }

  render() {
    return (
      <div className="loginContainer">

        <h1 className="appTitle">Application d'administration de CyberKey</h1>
        <form className="loginForm" action="">
          <input type="text" name="email" placeholder="Admin email" onChange={(event) => this.setState({email : event.target.value})}/>
          <input type="password" name="password" placeholder="Mot de passe" onChange={(event) => this.setState({passwordText : event.target.value})}/>
        </form>

        <button className="validateButton" onClick={this.handleLoginButton}>
            Valider
        </button>
        <button className="validateButton" onClick={this.handleResetAdminPassword}>
            Réinitialiser mot de passe
        </button>
      </div>
    );
  }
}

export default ConnectionPage;
