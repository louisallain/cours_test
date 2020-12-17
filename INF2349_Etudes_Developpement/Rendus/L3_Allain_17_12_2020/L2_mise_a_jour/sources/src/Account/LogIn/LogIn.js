import React, { Component } from "react";
import { Container, Header, Content, Form, Item, Input, Label, Body, Title, Left, Button, Text, Toast } from 'native-base';
import auth from '@react-native-firebase/auth';
import { RSAKeychain } from 'react-native-rsa-native';
import database from '@react-native-firebase/database';

import * as STORAGE_NAMING from '../../utils/storage_naming';
import * as UTILS_FUNCTION from '../../utils/functions';
import * as PAGES from '../../utils/pages';

import styles from './LogInCSS'

const UBS_EMAIL_REGEX = /.+@.*univ-ubs.fr$/g

/**
 * Classe représentant la page de connexion.
 */
class LogIn extends Component {

  /**
   * Constructeur du composant.
   * @param {Object} props propriétés du composant
   */
  constructor(props) {
    super(props)
    this.state = {
      email: "",
      password: "",
      emailLabel: "Email (univ-ubs.fr)",
      passwordLabel: "Mot de passe",
    }
  }

  /**
   * Handler de la modification du champ email
   * @param {string} text valeur de l'entrée du champ de l'email
   */
  handleEmailInput = (text) => {
    if(text === "") this.setState({emailLabel: "Email (univ-ubs.fr)", email: ""})
    else if(!text.match(UBS_EMAIL_REGEX)) this.setState({emailLabel: "Email non valide !", email: ""})
    else this.setState({emailLabel: "Email valide", email: text})
  }

  /**
   * Handler de la modification du champ mot de passe
   * @param {string} text valeur du champ mot de passe
   */
  handlePasswordInput = (text) => {
      this.setState({password: text})
  }

    /**
     * Créer un couple de clef RSA de 258 octets (2048 bits).
     * Retourne une nouvelle promise résolu avec une fonction prenant en paramètre la clef publique.
     * @param {String} privateKeyTag Identifiant pour retrouver la clef dans le "KeyChain" du système.
     */
    createRSAKeys = (privateKeyTag) => {
        return new Promise((resolve, reject) => {
        RSAKeychain.generate(privateKeyTag).then((keys) => resolve(keys.public)).catch((error) => reject(error))
        })
    }

  /**
   * Handler du bouton de connexion.
   * Vérifie si l'email est bien du domaine de l'ubs (univ-ubs.fr).
   */
  handleLogIn = () => {
    if(this.state.email.match(UBS_EMAIL_REGEX)) {
        auth().signInWithEmailAndPassword(this.state.email, this.state.password)
            .then((userCredential) => {
                console.log(`User log in with email : ${userCredential.user.email}`)

                // Défini l'id de l'utilisateur courant dans la BDD
                let userKey = this.state.email.replace(/[.]/g, '') // NOTE : EMAIL SANS LES POINTS CAR INTERDITS DANS BDD

                // Génère et sauvegarde les clefs
                let keyTag = `${STORAGE_NAMING.PRIVATE_KEY_NAME}-${this.state.email}`
                this.createRSAKeys(keyTag).then((pub) => {
                    // Ajoute la clef publique à la BDD sous /public_key/<email_utilisateur> NOTE : EMAIL SANS LES POINTS CAR INTERDITS DANS BDD        
                    database().ref(`/public_keys/${userKey}`).set({public_key: UTILS_FUNCTION.publicKey_PEM_to_hex(pub)})
                    .then(() => console.log(`Public key added to the BDD for user : ${this.state.email}`))
                    .catch((error) => console.log(error))
                })
            })
            .catch((error) => {
                console.log(error)
                if(error.code === "auth/user-not-found") {
                    Toast.show({
                        text: "Utilisateur inconnu !"
                    })
                }
                else if(error.code === "auth/wrong-password") {
                    Toast.show({
                        text: "Mot de passe incorrect !"
                    })
                }
                else {
                    Toast.show({
                        text: error.code
                    })
                }
            })
    }
    else {
        Toast.show({
            text: "L'email n'est pas du domaine univ-ubs.fr !"
        })
    }
  }

  /**
   * Méthode rendu graphique du composant.
   */
  render() {
    return (
      <Container>
        <Header style={styles.header}>
          <Left/>
          <Body>
          <Title>Connexion</Title>
          </Body>
        </Header>
        <Content>
            <Form>
                <Item floatingLabel>
                    <Label>{this.state.emailLabel}</Label>
                    <Input autoCapitalize="none" keyboardType="email-address" textContentType="emailAddress" onChangeText={this.handleEmailInput}/>
                </Item>
                <Item floatingLabel last>
                    <Label>{this.state.passwordLabel}</Label>
                    <Input secureTextEntry={true} textContentType="password" onChangeText={this.handlePasswordInput}/>
                </Item>
            </Form>
            <Container style={styles.buttonContainer}>
                <Button transparent style={styles.goToCreateAccountPageButton} onPress={() => this.props.switchToPage(PAGES.ACCOUNT_SIGN_IN)}>
                    <Text>Créer son compte ?</Text>
                </Button>
                <Button style={styles.logInButton} block onPress={this.handleLogIn}>
                    <Text>Connexion</Text>
                </Button>
            </Container>
        </Content>
      </Container>
    );
  }
}

export default LogIn;