import firebase from 'firebase';

/**
 * Configuration for Firebase.
 */
const firebaseConfig = {
    apiKey: "AIzaSyC6kPaCF2EsVck5p0qE1rPmRzz6HA414fA",
    authDomain: "gestionnairesallesparempreinte.firebaseapp.com",
    databaseURL: "https://gestionnairesallesparempreinte.firebaseio.com",
    projectId: "gestionnairesallesparempreinte",
    storageBucket: "gestionnairesallesparempreinte.appspot.com",
    messagingSenderId: "623460427677",
    appId: "1:623460427677:web:0a3c55934f7515f09bf174"
};

firebase.initializeApp(firebaseConfig);

/**
 * Reference to the database service.
 */
export var fbDatabase = firebase.database()
/**
 * Reference to the authentication service.
 */
export var fbAuth = firebase.auth()