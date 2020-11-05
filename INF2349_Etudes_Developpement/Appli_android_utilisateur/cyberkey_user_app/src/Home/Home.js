import React, { Component } from "react";
import { Container, Header, Content, Body, Title, Left, Tabs, Tab, Text, TabHeading, Icon, Spinner } from 'native-base';
import Settings from './Settings/Settings';
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
        }
    }

    /**
     * Méthode exécutée après le rendu du composant.
     * Recherche toutes les informations liés à l'utilisateur courant dans la BDD.
     *  - 
     */
    componentDidMount() {
        
        database() // récupère les informations générales.
            .ref("/users")
            .on("value", 
                (snapshot) => {
                this.setState({
                    user: JSON.parse(snapshot.val()).filter(u => u.id === auth().currentUser.email)[0],
                    userInformationsRetrieved: true
                })},
                (error) => console.log(error)
            )
        database() // récupère la clef publique
            .ref(`public_keys/${auth().currentUser.email.replace(/[.]/g, '')}/public_key`)
            .once("value")
            .then((snapshot) => {
                this.setState({
                    userPublicKey: snapshot.val(),
                    userPublicKeyRetrieved: true
                })
            })
            .catch((error) => console.log(error))
    }

    /**
     * Modifie la base de données pour demander l'accès VIP pour l'uilisateur courant.
     */
    requestVIP = () => {
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
            <Content>
            <Tabs>
                <Tab heading={ <TabHeading><Icon name="calendar" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved) ? <Text>TODO: liste des créneaux</Text> : <Spinner color="blue"/>}
                </Tab>
                <Tab heading={ <TabHeading><Icon name="key" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved) ? <Text>TODO: dévérouillage de la porte</Text> : <Spinner color="blue"/>}
                </Tab>
                <Tab style={styles.settingsTab} heading={ <TabHeading><Icon name="settings" /></TabHeading>}>
                    {(this.state.userInformationsRetrieved && this.state.userPublicKeyRetrieved) ? <Settings user={this.state.user} requestVIP={this.requestVIP}/> : <Spinner color="blue"/>}
                </Tab>
            </Tabs>
            </Content>
        </Container>
        );
    }
}

export default Home;