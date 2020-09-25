
//import 'react-native-gesture-handler'; // désactivé car problème avec la version web
import React, { Component } from 'react';
import { Image, Platform, StyleSheet, Text, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
const Stack = createStackNavigator();

/**
 * Custom components
 */
import ConnectionScreen from './Connection';
import HomeScreen from './Home';

export default class App extends Component {
  
	render() {
  
		return (
      <NavigationContainer>
        <Stack.Navigator>
          <Stack.Screen
            name="Connexion"
            component={ConnectionScreen}
            options={{ title: 'Connexion' }}
          />

          <Stack.Screen
            name="Home"
            component={HomeScreen}
            options={{ title: 'Home' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
		);
	}
}