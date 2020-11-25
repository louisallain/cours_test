import React, { Component } from "react";
import { Container, Header, Content, Body, Title, Left, Tabs, Tab, Text, TabHeading, Icon, Spinner, Toast } from 'native-base';
import { Alert } from 'react-native';
import prompt from 'react-native-prompt-android';
import { Notifications } from 'react-native-notifications';
import Settings from './Settings/Settings';
import Unlock from './Unlock/Unlock';
import EventsList from './EventsList/EventsList';
import auth from '@react-native-firebase/auth';
import database from '@react-native-firebase/database';

import styles from './HomeCSS'

/**
 * Classe représentant la page d'accueil.
 */
class Home extends Component {

    /**
     * Constructeur du composant.
     * @param {Object} props propriétés du composant
     */
    constructor(props) {
        super(props)
        this.state = {
            user: null,
            userPublicKey: "",
            userInformationsRetrieved: false,
            userPublicKeyRetrieved: false,
            events: null,
            eventsRetrieved: false,
        }
    }

    /**
     * Méthode exécutée après le rendu du composant.
     * Recherche toutes les informations liés à l'utilisateur courant dans la BDD.
     * - les informations générales
     * - la clef publique
     * - ajoute un listener sur l'accès VIP pour notifier l'utilisateur
     * Recherche les créneaux dans la BDD.
     */
    componentDidMount() {
        
        let userKey = auth().currentUser.email.replace(/[.]/g, '')

        database() // récupère les évènements
            .ref("/events")
            .on(
                "value", 
                (snapshot) => this.setState({events: JSON.parse(snapshot.val()), eventsRetrieved: true}),
                (error) => console.log(error)
            )
        
        database() // récupère les informations générales de l'utilisateur
            .ref(`/users/${userKey}`)
            .on("value", 
                (snapshot) => {
                    console.log(snapshot.val())
                    this.setState({
                        user: snapshot.val(),
                        userInformationsRetrieved: true
                    })
                },
                (error) => console.log(error)
            )

        database() // récupère la clef publique
            .ref(`public_keys/${userKey}/public_key`)
            .once("value")
            .then((snapshot) => {
                this.setState({
                    userPublicKey: snapshot.val(),
                    userPublicKeyRetrieved: true
                })
            })
            .catch((error) => console.log(error))

        database() // listener sur l'état d'accès libre
            .ref(`/users/${userKey}/isVIP`)
            .on("value", (snapshot) => {
                if(snapshot.val() === true) {
                    Notifications.postLocalNotification({
                        title: "CyberKey",
                        body: "L'accès VIP vous a été accordé !",
                    });
                }
            }, (error) => console.log(error))
    }

    /**
     * Modifie la base de données pour demander l'accès VIP pour l'uilisateur courant.
     */
    requestVIP = () => {
        Alert.alert(
            "Demande VIP",
            "L'accès VIP permet d'accéder à la salle à n'importe quel moment sans contrainte de créneaux. Cette demande pourra être acceptée ou refusée par l'administrateur",
            [
                {
                    text: 'Envoyer la demande',
                    onPress: () => {
                        let userKey = auth().currentUser.email.replace(/[.]/g, '')
                        if(this.state.user.isVIP === false && this.state.user.requestVIP === false) {
                            let tmpUser = {...this.state.user}
                            tmpUser.requestVIP = true
                            this.setState({
                                user: tmpUser
                            })
                            database()
                                .ref(`/users/${userKey}`)
                                .set(this.state.user)
                                .then(() => {
                                    console.log(`Request VIP set to DB for user : ${auth().currentUser.email}`)
                                    Toast.show({text: "Demande envoyée !"})
                                })
                                .catch((error) => console.log(error))
                        }
                    }
                },
                {
                    text: 'Annuler',
                    onPress: () => Toast.show({text: "Demande annulée !"}),
                    style: 'cancel'
                }
            ],
            { cancelable: false }
        )
    }

    deleteAccount = () => {
        prompt(
            "Entrer votre mot de passe",
            "Veuillez confirmer votre mot de passe pour supprimer votre compte.",
            [
                {
                    text: "Annuler",
                    onPress: () => Toast.show({text: "Annulation !"}),
                    style: "cancel"
                },
                {
                    text: "OK",
                    onPress: (password) => {
                        const credential = auth.EmailAuthProvider.credential(
                            auth().currentUser.email, 
                            password
                        );
                        auth().currentUser
                            .reauthenticateWithCredential(credential)
                            .then(() => {
                                let userKey = auth().currentUser.email.replace(/[.]/g, '')
                                database() 
                                    .ref(`/users/${userKey}`)
                                    .once("value", 
                                        (snapshot) => {
                                            let userBackup = snapshot.val()
                                            
                                            database() 
                                                .ref(`/users/${userKey}`)
                                                .remove()
                                                .then(() => {
                                                    auth().currentUser.delete().then(() => {
                                                        Toast.show({text: "Compte supprimé !"})
                                                        auth().signOut()
                                                    })
                                                    .catch(error => {
                                                        // Backup
                                                        database() 
                                                            .ref(`/users/${userKey}`)
                                                            .set(userBackup)
                                                    })
                                                })
                                                .catch(error => {
                                                    // Backup
                                                    database() 
                                                        .ref(`/users/${userKey}`)
                                                        .set(userBackup)
                                                })
                                        },
                                        (error) => console.log(error)
                                    )
                            })
                            .catch((error) => Toast.show({text: "Mauvais mot de passe !"}))
                    }
                }
            ],
            {
                type: 'secure-text',
                cancelable: false,
                placeholder: 'Mot de passe'
            },
        )
    }

    /**
     * Ajoute en BDD une demande d'accès de l'utilisateur courant pour le créneau en paramètre.
     * @param {Object} event le créneau auquel l'utilisateur courant demande l'accès
     */
    requestAccessForTheEvent = (event) => {
        let userKey = auth().currentUser.email.replace(/[.]/g, '')
        let tmpUser = {...this.state.user}
        if(!tmpUser.requestForEvents) tmpUser.requestForEvents = []
        if(tmpUser.requestForEvents.filter(id => id === event.id).length === 0) { // vérifie en plus que l'utilisateur n'a pas déjà demandé l'accès à ce créneau
            
            tmpUser.requestForEvents.push(event.id)
            database()
                .ref(`/users/${userKey}`)
                .set(tmpUser)
                .then(() => {
                    console.log(`Request for event ${event.id} set to DB for user : ${auth().currentUser.email}`)
                })
                .catch((error) => console.log(error))
        }
    }


    /**
     * Méthode rendu graphique du composant.
     */
    render() {
        return (
        <Container>
            <Header hasTabs style={styles.header}>
            <Left/>
                <Body>
                    <Title>CyberKey</Title>
                </Body>
            </Header>
            <Tabs locked={true}>
                <Tab heading={ <TabHeading><Icon name="calendar" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved && this.state.eventsRetrieved) ? <EventsList requestAccessForTheEvent={this.requestAccessForTheEvent} events={this.state.events} user={this.state.user}/> : <Spinner color="blue"/>}
                </Tab>
                <Tab heading={ <TabHeading><Icon name="key" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved && this.state.eventsRetrieved) ? <Unlock user={this.state.user} events={this.state.events}/> : <Spinner color="blue"/>}
                </Tab>
                <Tab style={styles.settingsTab} heading={ <TabHeading><Icon name="settings" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved && this.state.eventsRetrieved) ? <Settings user={this.state.user} requestVIP={this.requestVIP} deleteAccount={this.deleteAccount}/> : <Spinner color="blue"/>}
                </Tab>
            </Tabs>
        </Container>
        );
    }
}

export default Home;