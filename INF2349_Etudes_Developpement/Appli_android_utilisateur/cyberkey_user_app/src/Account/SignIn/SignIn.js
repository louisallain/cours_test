import React, { Component } from "react";
import { Container, Header, Content, Form, Item, Input, Label, Body, Title, Left, Button, Text, Toast } from 'native-base';
import auth from '@react-native-firebase/auth';
import database from '@react-native-firebase/database';
import RNSecureKeyStore, {ACCESSIBLE} from "react-native-secure-key-store";
import { RSAKeychain } from 'react-native-rsa-native';

import * as STORAGE_NAMING from '../../utils/storage_naming';
import * as UTILS_FUNCTION from '../../utils/functions';

import styles from './SignInCSS'

const UBS_EMAIL_REGEX = /.+@.*univ-ubs.fr$/g

/**
 * Classe représentant la page de création de comptes.
 */
class SignIn extends Component {

  /**
   * Constructeur du composant.
   * @param {Object} props propriétés du composant
   */
  constructor(props) {
    super(props)
    this.state = {
      email: "",
      password: "",
      passwordConfirmation: "",
      emailLabel: "Email (univ-ubs.fr)",
      passwordLabel: "Mot de passe",
      passwordConfirmLabel: "Confirmer le mot de passe",
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
    if(this.state.passwordConfirmation === "") this.setState({passwordConfirmLabel: "Confirmer le mot de passe"})
    else if(text === this.state.passwordConfirmation) this.setState({passwordConfirmLabel: "Confirmation validée"})
    else this.setState({passwordConfirmLabel: "Les mots de passe ne correspondent pas !"})
    if(text === "") this.setState({passwordLabel: "Mot de passe", password: ""})
    else if(text.length < 8) this.setState({passwordLabel: "Au moins 8 caractères !", password: ""})
    else this.setState({passwordLabel: "Mot de passe valide", password: text})
  }

  /**
   * Handler de la modification du champ conformation de mot de passe.
   * @param {string} text valeur du champ confirmation de mot de passe
   */
  handlePasswordConfirmationInput = (text) => {
    this.setState({passwordConfirmation: text})
    if(text === "") this.setState({passwordConfirmLabel: "Confirmer le mot de passe"})
    else if(this.state.password === "") this.setState({passwordConfirmLabel: "Saisir d'abord un mot de passe !"})
    else if(text !== this.state.password) this.setState({passwordConfirmLabel: "Les mots de passe ne correspondent pas !"})
    else if(text === this.state.password) this.setState({passwordConfirmLabel: "Confirmation validée"})
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
   * Handler du bouton de création de compte.
   * Vérifie si l'email est bien du domaine de l'ubs (univ-ubs.fr), si le mot de passe comporte au moins 8 caractères et si les mot deux mots de passe correspondent.
   * Ajoute le nouvel utilisateur à la base de données.
   * Créer une paire de clefs RSA.
   * La clef privée est stockée dans le AsyncStorage du mobile et la clef publique est ajoutée à la base de données.
   */
  handleSignIn = () => {
    
    if(this.state.email.match(UBS_EMAIL_REGEX) && this.state.password.length >= 8 && this.state.password === this.state.passwordConfirmation) {

      auth()
        .createUserWithEmailAndPassword(this.state.email, this.state.password)
        .then(() => {
          console.log(`User succesufully created with email : ${this.state.email}`)
          Toast.show({
            text: "Compte créé !"
          })

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

          // Ajoute l'utilisateur à la BDD sous /users/<email_utilisateur> NOTE : EMAIL SANS LES POINTS CAR INTERDITS DANS BDD
          database()
          .ref(`/users/${userKey}`)
          .set({
            id: this.state.email,
            isVIP: false,
            requestVIP: false,
          })
          .then(() => console.log("New user added to /users"))
          .catch((error) => console.log(error))
        })
        .catch((error) => {
          console.log(error)
          if(error.code === 'auth/email-already-in-use') {
            console.log(`Email ${this.state.email} already in use`)
            Toast.show({
              text: "Cette email est déjà utilisé !"
            })
          }
          else if(error.code === 'auth/invalid-email') {
            console.log(`Email ${this.state.email} invalide`)
            Toast.show({
              text: "Cette email est invalide !"
            })
          }
        })
    } else {
      Toast.show({
        text: 'Vérifier les champs !'
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
          <Title>Créer son compte</Title>
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
            <Item floatingLabel last>
              <Label>{this.state.passwordConfirmLabel}</Label>
              <Input secureTextEntry={true} textContentType="password" onChangeText={this.handlePasswordConfirmationInput} value={this.state.passwordConfirmationValue} />
            </Item>
          </Form>
          <Button style={styles.createButton} block onPress={this.handleSignIn}>
            <Text>Créer !</Text>
          </Button>
        </Content>
      </Container>
    );
  }
}

export default SignIn;