import React, { Component } from 'react';
import { Image, Platform, StyleSheet, Text, View, TextInput, Button, Input } from 'react-native';
import auth from '@react-native-firebase/auth';
import { GoogleSignin } from '@react-native-community/google-signin';

GoogleSignin.configure({
	androidClientId: '623460427677-dq28nvhofofatnl7r49d0q1evlma86hl.apps.googleusercontent.com',
});

export default class Connection extends Component {
    
	constructor(props) {

		super(props);
		this.state = { 
		
		};
	}

	handleGoogleLogin = async () => {
	
		// Get the users ID token
		const { idToken } = await GoogleSignin.signIn();

		// Create a Google credential with the token
		const googleCredential = auth.GoogleAuthProvider.credential(idToken);
	  
		// Sign-in the user with the credential
		return auth().signInWithCredential(googleCredential);
	}

	render() {
		
		return (
			<View style={styles.container}>
				
				<Button
					title="Login with Google (Android)"
					onPress={() => {
						this.handleGoogleLogin()
						.then(() => console.log("connecté !"))
						.catch((error) => console.error(error))
					}}
				/>
			</View>
		);
	}
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: '#F5FCFF',
	},
});