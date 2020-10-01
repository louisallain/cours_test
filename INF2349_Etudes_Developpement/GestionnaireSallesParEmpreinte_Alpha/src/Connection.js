import React, { Component } from 'react';
import { Image, Platform, StyleSheet, Text, View, TextInput, Button, Alert } from 'react-native';
import { BOB_EMAIL, BOB_PASSWORD } from '../utils/allowed_users_for_dev';

export default class Connection extends Component {
    
	constructor(props) {

		super(props);
		this.state = { 
			email: "",
			password: ""
        };
	}

	handleChangeEmailInput = (text) => {

		this.setState({
			email: text
		})
	}

	handleChangePasswordInput = (text) => {

		this.setState({
			password: text
		})
	}

	handleSignInButton = () => {

		if(this.state.email == BOB_EMAIL && this.state.password == BOB_PASSWORD) {
			console.log("Connected")
			this.props.navigation.navigate('Home')
		}
	}

	render() {
        
		return (
			<View style={styles.container}>
				
				<TextInput
					style={styles.loginInput}
					onChangeText={text => this.handleChangeEmailInput(text)}
					placeholder={"Email"}
					autoCapitalize={"none"}
				/>

				<TextInput
					style={styles.loginInput}
					onChangeText={text => this.handleChangePasswordInput(text)}
					placeholder={"Password"}
					autoCapitalize={"none"}
				/>

				<Button
					title="Sign In"
					color="#f194ff"
					onPress={() => this.handleSignInButton()}
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
	loginInput: { 
		height: 40, 
		borderColor: 'gray', 
		borderWidth: 1 
	}
});