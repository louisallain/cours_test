import React, { Component } from "react";
import { Container, Text, Button } from 'native-base';

import styles from './SettingsCSS';


/**
 * Classe représentant la page des paramètres.
 */
class Settings extends Component {

    /**
     * Constructeur du composant.
     * @param {Object} props propriétés du composant
     */
    constructor(props) {
        super(props)
    }

    /**
     * Méthode rendu graphique du composant.
     */
    render() {

        let vipButtonText
        if(this.props.user.isVIP) vipButtonText = "Déjà VIP !"
        else if(this.props.user.requestVIP) vipButtonText = "Accès VIP déjà demandé !"
        else vipButtonText = "Demander l'accès VIP"

        return (
        <Container style={styles.container}>
            <Button style={styles.button} warning disabled={this.props.user.isVIP || this.props.user.requestVIP} onPress={this.props.requestVIP}>
                <Text>{vipButtonText}</Text>
            </Button>
        </Container>
        );
    }
}

export default Settings;