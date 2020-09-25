import React, { Component } from 'react';
import { Image, Platform, StyleSheet, Text, View } from 'react-native';

export default class Home extends Component {
    
	constructor(props) {

		super(props);
		this.state = { 
        
        };
	}
	render() {
        
		return (
			<View style={styles.container}>
				
				<Text>Connecté</Text>
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