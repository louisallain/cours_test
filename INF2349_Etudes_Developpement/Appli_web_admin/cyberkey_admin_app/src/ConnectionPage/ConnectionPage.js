import React from 'react';
import * as firebase from '../utils/firebase_config';

class ConnectionPage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      email: '',
      passwordText: '',
    };
  }

  /**
   * Compare le nom d'utilisateur et le hash du mot de passe aux informations depuis la base de données
   */
  handleLoginButton = () => {

    firebase.fbDatabase.ref('admin_email').on('value', (snapshot) => {
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

  render() {
    return (
      <div>
        <form action="">
          <label>
            Email de l'administrateur :
            <input type="text" name="email" onChange={(event) => this.setState({email : event.target.value})}/>
          </label>
          <label>
            Mot de passe :
            <input type="password" name="password" onChange={(event) => this.setState({passwordText : event.target.value})}/>
          </label>
        </form>
        <button onClick={this.handleLoginButton}>
            Valider
        </button>
      </div>
    );
  }
}

export default ConnectionPage;
