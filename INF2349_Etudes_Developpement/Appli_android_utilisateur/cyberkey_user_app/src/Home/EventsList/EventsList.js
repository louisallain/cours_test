import React, { Component } from "react";
import { FlatList } from "react-native";
import { Container, Card, CardItem, Text, Right, Left, Button, Icon, Body, ListItem } from 'native-base';

import styles from './EventsListCSS';

const months = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"]


/**
 * Classe représentant la liste des créneaux
 */
class EventsList extends Component {

    /**
     * Constructeur du composant.
     * @param {Object} props propriétés du composant
     */
    constructor(props) {
        super(props)        
    }

    /**
     * Retourne le lundi (Date) précédent une date
     * @param {Date} d date d'où retourner le lundi précédent
     */
    getMondayOfDate = (d) => {
        d = new Date(d);
        let day = d.getDay()
        let diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
        return new Date(d.setDate(diff));
    }

    /**
     * Retourne une nouvelle date correspondant à date + le nombre de jours.
     * @param {Date} date date à laquelle ajoutée des jours
     * @param {Number} days nombre de jours à ajouter  
     */
    addDays = (date, days) => {
        let result = new Date(date);
        result.setDate(result.getDate() + days);
        return result;
    }

    /**
     * Retourne un tableau contenant les donneés permettant de construire la liste des créneaux à afficher.
     * Sépare notamment les jours par des "header" de semaine.
     * @param {Object} events liste des créneaux
     */
    buildDataForList = (events) => {
        let data = []
        let orderedByDateEvents = events.sort((e1, e2) => {
            let e1StartDate = new Date(e1.start)
            let e2StartDate = new Date(e2.start)
            return (e1StartDate > e2StartDate) ? 1: -1
        })
        let currentMonday = this.getMondayOfDate(orderedByDateEvents[0].start)
        let endOfCurrentWeek = this.addDays(currentMonday, 6)
        currentMonday.setHours(0, 0, 0, 0)
        data.push({key: currentMonday.getTime().toString(), name: `Du ${currentMonday.getDate()} ${months[currentMonday.getMonth()]} au ${endOfCurrentWeek.getDate()} ${months[endOfCurrentWeek.getMonth()]}`, header: true})
        orderedByDateEvents.map(e => {
            let tmpCurrentMonday = this.getMondayOfDate(e.start)
            tmpCurrentMonday.setHours(0, 0, 0, 0)
            if(currentMonday.toString() !== tmpCurrentMonday.toString()) {
                currentMonday = tmpCurrentMonday
                endOfCurrentWeek = this.addDays(currentMonday, 6)
                data.push({key: currentMonday.getTime().toString(), name: `Du ${currentMonday.getDate()} ${months[currentMonday.getMonth()]} au ${endOfCurrentWeek.getDate()} ${months[endOfCurrentWeek.getMonth()]}`, header: true})
            }
            let start = new Date(e.start)
            let startMinutes = (start.getMinutes()<10?'0':'') + start.getMinutes()
            let end = new Date(e.end)
            let endMinutes = (end.getMinutes()<10?'0':'') + end.getMinutes()
            data.push({key: start.getTime().toString(), event: e, name: `Le ${start.getDate()} ${months[start.getMonth()]} de ${start.getHours()}h${startMinutes} à ${end.getHours()}h${endMinutes}`, header: false})
        })
        return data
    }

    /**
     * Méthode rendant un code JSX correspondant au bouton de demande d'accès.
     * Soit l'utilisateur n'a pas demandé l'accès et le bouton est activé et bleu foncé.
     * Soit l'utilisateur a demandé l'accès et cette demande est en cours et le bouton est désactivé et gris.
     * Soit l'utilisateur a demandé l'accès et cette demande a été validé le bouton est jaune.
     * @param {Object} event le créneau associé au bouton de demande d'accès.
     */
    renderRequestAccessButton = (event) => {
        let state = "not_requested"
        if(!this.props.user.requestForEvents) state = "not_requested"
        else if(this.props.user.requestForEvents) {
            if(this.props.user.requestForEvents.filter(id => id === event.id).length > 0) state = "requested"
        }
        if(this.props.user.acceptedForEvents) {
            if(this.props.user.acceptedForEvents.filter(id => id === event.id).length > 0) state = "accepted"
        }
        if(state === "not_requested") {
            return (
                <Button style={styles.eventButton} onPress={() => this.props.requestAccessForTheEvent(event)}>
                    <Text>Demander l'accès</Text>
                </Button>
            )
        }
        else if(state === "requested") {
            return (
                <Button disabled style={styles.eventButton}>
                    <Text>Accès demandé</Text>
                    <Right>
                        <Icon type="FontAwesome5" name="ellipsis-h" style={styles.requestButtonIcon}/>
                    </Right>
                </Button>
            )
        }
        else if(state === "accepted") {
            return (
                <Button disabled style={[styles.eventButton, styles.eventButtonAccepted]}>
                    <Text>Accès ok</Text>
                    <Right>
                        <Icon type="FontAwesome5" name="check-circle" style={styles.acceptedButtonIcon}/>
                    </Right>
                </Button>
            )
        }
    }

    /**
     * Méthode rendant un code JSX correspondant à un créneau de la list des créneaux.
     * @param {Object} param0 item de la liste des créneaux
     */
    renderItem = ({ item }) => {
        if (item.header) {
          return (
            <ListItem itemDivider>
                <Text style={{ fontWeight: "bold" }}>
                    {item.name}
                </Text>
            </ListItem>
          );
        } 
        else if (!item.header) {
          return (
            <Card>
                <CardItem>
                    <Left>
                        <Icon type="FontAwesome5" name="laptop-code" style={styles.icon}/>
                        <Body>
                            <Text>{item.name}</Text>
                            <Text note>Créneau CyberLab</Text>
                        </Body>
                    </Left>
                </CardItem>
                <CardItem cardBody>
                </CardItem>
                <CardItem>
                    <Left />
                    <Body />
                    <Right>
                        {this.renderRequestAccessButton(item.event)}
                    </Right>
                </CardItem>
            </Card>
          );
        }
    }

    /**
     * Méthode rendu graphique du composant.
     */
    render() {

        // Construit ici la liste des créneaux à afficher afin de la mettre à jour automatique lorsque l'état change (this.props.events)
        // Construit cette liste à partir des créneaux disponibles du jour courant et + 
        let listData = this.buildDataForList(this.props.events.filter(e => {
            let tmpDate = new Date(e.start)
            let currentDay = new Date()
            currentDay.setHours(0, 0, 0, 0)
            return tmpDate >= currentDay
        }))
        let stickyHeaderIndices = [];
        listData.map(obj => {
            if(obj.header) stickyHeaderIndices.push(listData.indexOf(obj))
        })
        stickyHeaderIndices.push(0);
        return (
            <Container style={styles.container}>
                <FlatList
                    data={listData}
                    renderItem={this.renderItem}
                    keyExtractor={item => item.key}
                    stickyHeaderIndices={stickyHeaderIndices}
                />
            </Container>
        );
    }
}

export default EventsList;