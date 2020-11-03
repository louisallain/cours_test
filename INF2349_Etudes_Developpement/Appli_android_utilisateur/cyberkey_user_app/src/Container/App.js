import React, { Component } from "react";
import { View, Text, Button } from "react-native";
import auth from '@react-native-firebase/auth';
import SignIn from '../Account/SignIn/SignIn';
import { Root } from "native-base";

import * as PAGES from '../utils/pages';

class App extends Component {

  constructor(props) {
    super(props)
    this.state = {
      current_page: PAGES.ACCOUNT_SIGN_IN
    }
  }

  componentDidMount() {
    auth().onAuthStateChanged((user) => {
      if(user) this.setState({current_page: PAGES.HOME})
      else this.setState({current_page: PAGES.ACCOUNT_SIGN_IN})
    })
  }

  signOut = () => {
    auth().signOut()
  }

  render() {
    
    let content

    switch(this.state.current_page) {
      case PAGES.ACCOUNT_SIGN_IN: {
        content = <SignIn />
        break;
      }

      case PAGES.HOME:{
        content = <Text onPress={this.signOut}>Connecté</Text>
        break;
      }

      default:
        content = <SignIn />
        break;
    }

    return (
      <Root>
        {content}
      </Root>
    )
  }
}

export default App;