
import 'react-native-gesture-handler';
import React, { Component } from 'react';
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
            name="Connection"
            component={ConnectionScreen}
            options={{ title: 'Connection' }}
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