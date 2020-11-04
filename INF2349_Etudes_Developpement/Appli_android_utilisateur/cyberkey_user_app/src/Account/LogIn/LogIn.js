import React, { Component } from "react";
import { Container, Header, Content, Form, Item, Input, Label, Body, Title, Left, Button, Text, Toast } from 'native-base';
import auth from '@react-native-firebase/auth';

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
   * Handler du bouton de connexion.
   * Vérifie si l'email est bien du domaine de l'ubs (univ-ubs.fr).
   */
  handleLogIn = () => {
    if(this.state.email.match(UBS_EMAIL_REGEX)) {
        auth().signInWithEmailAndPassword(this.state.email, this.state.password)
            .then((userCredential) => {
                console.log(`User log in with email : ${userCredential.user.email}`)
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