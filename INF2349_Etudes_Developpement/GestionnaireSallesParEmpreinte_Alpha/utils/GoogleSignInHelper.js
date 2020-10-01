import React, { Component, useEffect } from 'react'
import {
  View,
  Text,
  StatusBar,
  Image,
  StyleSheet,
  Button,
  Alert
} from 'react-native'
import {
  GoogleSigninButton,
  GoogleSignin,
  statusCodes
} from '@react-native-community/google-signin'
import { WEB_CLIENT_ID } from './keys'

configureGoogleSign = () => {
		
	GoogleSignin.configure({
		webClientId: WEB_CLIENT_ID,
		offlineAccess: false
	})
}

export default class Connection extends Component {
    
	constructor(props) {

		super(props);
		this.state = { 
			userInfo: null,
			isLoggedIn: false,
			error: null
		};

		configureGoogleSign();
	}

	getCurrentGoogleUserInfo = async () => {
		try {
			const userInfo = await GoogleSignin.signInSilently()
			setUserInfo(userInfo)
		} 
		catch (error) {
			if (error.code === statusCodes.SIGN_IN_REQUIRED) {
				// when user hasn't signed in yet
				Alert.alert('Please Sign in')
				this.setState({isLoggedIn: false})
			} 
			else {
				Alert.alert('Something else went wrong... ', error.toString())
				this.setState({isLoggedIn: false})
			}
		}
	}

	handleGoogleSignOut = async () => {

		try {
			
			await GoogleSignin.revokeAccess()
			await GoogleSignin.signOut()
			this.setState({isLoggedIn: false})
		} 
		catch (error) {
			Alert.alert('Something else went wrong... ', error.toString())
		}
	}

	handleGoogleSignIn = async () => {
		
		try {

			await GoogleSignin.hasPlayServices()
			const user = await GoogleSignin.signIn()
			this.setState({
				userInfo : user,
				isLoggedIn: true,
				error: null
			})
		} 
		catch (error) {

			if (error.code === statusCodes.SIGN_IN_CANCELLED) {
				// when user cancels sign in process,
				Alert.alert('Process Cancelled')
			} else if (error.code === statusCodes.IN_PROGRESS) {
				// when in progress already
				Alert.alert('Process in progress')
			} else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
				// when play services not available
				Alert.alert('Play services are not available')
			} else {
				// some other error
				Alert.alert('Something else went wrong... ', error.toString())
				this.setState({error: error})
			}
		}
	}

	render() {
		
		return (
			<View style={styles.container}>
				
				<GoogleSigninButton
					style={styles.signInButton}
					size={GoogleSigninButton.Size.Wide}
					color={GoogleSigninButton.Color.Dark}
					onPress={() => this.handleGoogleSignIn()}
				/>

				<View style={styles.statusContainer}>
					{this.state.isLoggedIn === false ? 
						(
							<Text>You must sign in!</Text>
						) 
						: 
						(
							<View>
								<Text>Hello {this.state.userInfo.user.name}</Text>
								<Button onPress={() => this.handleGoogleSignOut()} title='Sign out' color='#332211' />
							</View>
						)
					}
				</View>
			</View>
		);
	}
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center'
	},
	statusContainer: {
		marginVertical: 20
	},
});