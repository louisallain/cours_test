import React, { Component } from "react";
import { Container, Header, Content, Form, Item, Input, Label, Body, Title, Left, Button, Text, Toast } from 'native-base';

import styles from './SignInCSS'

const UBS_EMAIL_REGEX = /.+@.*univ-ubs.fr$/g

class SignIn extends Component {

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

  handleEmailInput = (text) => {
    if(text === "") this.setState({emailLabel: "Email (univ-ubs.fr)", email: ""})
    else if(!text.match(UBS_EMAIL_REGEX)) this.setState({emailLabel: "Email non valide !", email: ""})
    else this.setState({emailLabel: "Email valide", email: text})
  }

  handlePasswordInput = (text) => {
    if(this.state.passwordConfirmation === "") this.setState({passwordConfirmLabel: "Confirmer le mot de passe"})
    else if(text === this.state.passwordConfirmation) this.setState({passwordConfirmLabel: "Confirmation validée"})
    else this.setState({passwordConfirmLabel: "Les mots de passe ne correspondent pas !"})
    if(text === "") this.setState({passwordLabel: "Mot de passe", password: ""})
    else if(text.length < 8) this.setState({passwordLabel: "Au moins 8 caractères !", password: ""})
    else this.setState({passwordLabel: "Mot de passe valide", password: text})
  }

  handlePasswordConfirmationInput = (text) => {
    this.setState({passwordConfirmation: text})
    if(text === "") this.setState({passwordConfirmLabel: "Confirmer le mot de passe"})
    else if(this.state.password === "") this.setState({passwordConfirmLabel: "Saisir d'abord un mot de passe !"})
    else if(text !== this.state.password) this.setState({passwordConfirmLabel: "Les mots de passe ne correspondent pas !"})
    else if(text === this.state.password) this.setState({passwordConfirmLabel: "Confirmation validée"})
  }

  handleSignIn = () => {
    if(this.state.email.match(UBS_EMAIL_REGEX) && this.state.password.length >= 8 && this.state.password === this.state.passwordConfirmation) {
      // TODO: créer le compte
      // TODO: créer une paire de clefs RSA, sauvegarder la clef privée localement (AsyncStorage) et la clef publique sur la bdd
    } else {
      Toast.show({
        text: 'Vérifier les champs !'
      })
    }
  }

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
              <Input onChangeText={this.handleEmailInput}/>
            </Item>
            <Item floatingLabel last>
              <Label>{this.state.passwordLabel}</Label>
              <Input secureTextEntry={true} onChangeText={this.handlePasswordInput}/>
            </Item>
            <Item floatingLabel last>
              <Label>{this.state.passwordConfirmLabel}</Label>
              <Input secureTextEntry={true} onChangeText={this.handlePasswordConfirmationInput} value={this.state.passwordConfirmationValue} />
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