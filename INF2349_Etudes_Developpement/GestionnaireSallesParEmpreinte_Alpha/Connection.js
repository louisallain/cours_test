import React, { Component } from 'react';
import { Image, Platform, StyleSheet, Text, View, TextInput, Button, Input } from 'react-native';

export default class Connection extends Component {
    
	constructor(props) {
		
		super(props);
		this.state = { 
		
		};
	}

	handleLogin = () => {
		//this.props.navigation.navigate('Home')
	}

	render() {
		
		return (
			<View style={styles.container}>
				
				<Button
					title="Login with Google (Web)"
					onPress={() => this.handleLogin()}
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