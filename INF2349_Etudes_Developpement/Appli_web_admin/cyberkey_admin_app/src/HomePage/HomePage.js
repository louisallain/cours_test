import React from 'react'
import './HomePage.css'

import EventsCalendar from '../EventsCalendar/EventsCalendar'
import List from '../List/List'

import { v4 as uuidv4 } from 'uuid'

import * as firebase from '../utils/firebase_config'
import * as utils_function from '../utils/utils_function'

const CALENDAR_PAGE = "calendar_page"
const WAITING_FOR_ACCEPTATION_USERS_PAGE = "waiting_for_accpt_users_page"
const VIP_USERS_PAGE = "vip_users_page"
const SHOW_ACCEPTED_USERS_PAGE = "show_accepted_users_page"
const LOGS_PAGE = "logs_page"

// URL des créneaux de cours définis sur ADE dans un fichier ics
const ICS_EVENTS_ADE_FILE_URL = "https://planning.univ-ubs.fr/jsp/custom/modules/plannings/anonymous_cal.jsp?data=8241fc3873200214b0ceef9e5de578d6e0fa50826f0818af4a82a8fde6ce3f14906f45af276f59ae8fac93f781e86152d0472efb473cb41ff4beca69cf904027c2973627c2eb073ba5b915c2167188168d3f4109b6629391"

/**
 * Classe représentant le composant de la page d'accueil (après connexion) de l'application.
 */
class HomePage extends React.Component {

    /**
     * Initialise l'état du composant.
     * @param {*} props propriétés héritées du parent.
     */
    constructor(props) {

        super(props);
        this.state = {
            currentPage: CALENDAR_PAGE,
            events: [],
            eventsRetrieved: false,
            users: [],
            usersRetrieved: false,
            ADE_Events: null,
            ADE_EventsRetrieved: false,
            logs: null,
            logsRetrieved: false,
        };
    } 

    /**
     * Fonction exécuté après le montage du composant.
     * Récupère les créneaux et les utilisateurs depuis la BDD.
     * Récupère également les créneaux définis sur l'emploi du temps ADE de l'université.
     */
    componentDidMount() {
        this.retrieveEventsFromDB();
        this.retrieveUsersFromDB();
        this.retrieveADE_Events();
        this.retrieveLogsFromDB();
    }

    retrieveLogsFromDB = () => {
        firebase.fbDatabase
            .ref("/logs")
            .on("value", (snapshot) => {
                this.setState({
                    logs: snapshot.val(),
                    logsRetrieved: true
                })
            })

    }

    /**
     * Récupère depuis ADE les créneaux de cours définis par l'administration.
     */
    retrieveADE_Events = () => {
        const CORS_PROXY_URL = "https://api.allorigins.win/get?url=" // CORS proxy
        fetch(CORS_PROXY_URL+ICS_EVENTS_ADE_FILE_URL, {method: 'POST'}) // passe par un proxy CORS
        .then(res => res.json())
        .then(json => {
            this.setState({
                ADE_Events: utils_function.parseICS(json.contents),
                ADE_EventsRetrieved: true
            })
        })
        .catch((error) => console.log(error))
    }

    /**
     * Récupère les utilisateurs depus la BDD.
     */
    retrieveUsersFromDB = () => {
        console.log("Retrieving users from db...")
        firebase.fbDatabase
            .ref("users")
            .on("value", 
                (snapshot) => {
                    this.setState({
                        users: snapshot.val(),
                        usersRetrieved: true,
                    })
                }, 
                (error) => {
                    console.error(error)
                    this.setState({
                        users: [],
                        usersRetrieved: false,
                    })
                }
            )
    }

    /**
     * Récupère les créneaux depuis la BDD.
     */
    retrieveEventsFromDB = () => {
        console.log("Retrieving events from db...")
        firebase.fbDatabase
            .ref("events")
            .on("value", 
                (snapshot) => {
                    let tmpEvents = JSON.parse(snapshot.val())                    
                    tmpEvents.map(e => { // créer des dates à partir des heures de début et de fin (en UTC) des créneaux.
                        e.start = new Date(e.start)
                        e.end = new Date(e.end)
                    })
                    this.setState({
                        events: tmpEvents,
                        eventsRetrieved: true,
                    })
                }, 
                (error) => {
                    console.error(error)
                    this.setState({
                        events: [],
                        eventsRetrieved: false,
                    })
                }
            )
    }

    /**
     * Handler du bouton menant au calendrier des créneaux du menu de gauche.
     */
    handleCalendarButton = () => {
        this.setState({currentPage: CALENDAR_PAGE})
    }

    /**
     * Handler du bouton menant aux demandes d'accès des utilisateurs.
     */
    handleWaitingForAcceptationUsersButton = () => {
        this.setState({currentPage: WAITING_FOR_ACCEPTATION_USERS_PAGE})
    }

    /**
     * Handler du bouton menant aux demandes d'accès VIP des utilisateurs.
     */
    handleVIPUsersButton = () => {
        this.setState({currentPage: VIP_USERS_PAGE})
    }

    /**
     * Handler du bouton menant aux utilisateurs acceptés.
     */
    handleShowAcceptedUsers = () => {
        this.setState({currentPage: SHOW_ACCEPTED_USERS_PAGE})
    }

    /**
     * Handler du bouton permettant d'afficher les journaux.
     */
    handleShowLogs = () => {
        this.setState({currentPage: LOGS_PAGE})
    }

    /**
     * Met à jour en BDD les utilisateurs.
     * C'est à dire que pour chaque utilisateur, on vérifie si les créneaux auxquels ils ont accès existent toujours après les modifications.
     * De même que pour les créneaux auxquels ils souhaitent accéder.
     */
    matchUsersStateByExistingEventsOnDB = (events) => {
        let tmpUsers = this.state.users
        let entries = Object.entries(this.state.users)
        
        for(let [id, u] of entries) {
            if(u.requestForEvents) tmpUsers[id].requestForEvents = u.requestForEvents.filter(er => events.flatMap(e => e.id).includes(er))
            if(u.acceptedForEvents) tmpUsers[id].acceptedForEvents = u.acceptedForEvents.filter(er => events.flatMap(e => e.id).includes(er))
        }

        this.setState({
            users: tmpUsers
        })
        this.saveUsersStateOnDB();
    }

    /**
     * Sauvegarde l'état actuel des utilisateurs sur la BDD.
     */
    saveUsersStateOnDB = () => {
        firebase.fbDatabase.ref("users").set(this.state.users, (error) => {
            if(error) {
                console.log(error)
                alert(error)
            } else {
                console.log("Changes of users list saved on DB")
            }
        })
    }

    /**
     * Réfute l'accès d'un utilisateur pour un créneau.
     */
    denieAccessForThisUser = (index, item, ev, callback) => {
        let userKey = item.id.replace(/[.]/g, '')
        let tmpUser = this.state.users[userKey]
        tmpUser.acceptedForEvents = tmpUser.acceptedForEvents.filter(e => e != ev.id)
        let tmpUsers = this.state.users;
        tmpUsers[userKey] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()
        
        this.saveUsersStateOnDB();
    }

    /**
     * Rejette la demande d'accès d'un utilisateur pour un créneau.
     */
    rejectRequestAccessForThisUser = (index, item, ev, callback) => {
        let userKey = item.id.replace(/[.]/g, '')
        let tmpUser = this.state.users[userKey]
        tmpUser.requestForEvents = tmpUser.requestForEvents.filter(e => e != ev.id)
        let tmpUsers = this.state.users;
        tmpUsers[userKey] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()
        
        this.saveUsersStateOnDB();
    }

    /**
     * Accepte la demande d'accès d'un utilisateur pour un créneau.
     */
    acceptRequestAccesForThisUser = (index, item, ev, callback) => {
        let userKey = item.id.replace(/[.]/g, '')
        let tmpUser = this.state.users[userKey]
        tmpUser.requestForEvents = tmpUser.requestForEvents.filter(e => e != ev.id)
        if(tmpUser.acceptedForEvents) tmpUser.acceptedForEvents.push(ev.id)
        else tmpUser.acceptedForEvents = [ev.id]
        let tmpUsers = this.state.users;
        tmpUsers[userKey] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()

        this.saveUsersStateOnDB();
    }

    /**
     * Rejette la demande d'accès VIP d'un utilisateur.
     */
    denieVIPForThisUser = (index, item) => {
        let userKey = item.id.replace(/[.]/g, '')
        let tmpUser = this.state.users[userKey]
        tmpUser.isVIP = false
        tmpUser.requestVIP = false
        let tmpUsers = this.state.users;
        tmpUsers[userKey] = tmpUser;
        this.setState({users: tmpUsers})

        this.saveUsersStateOnDB();
    }

    /**
     * Accepte la demande d'accès VIP d'un utilisateur
     */
    authorizeVIPForThisUser = (index, item) => {
        let userKey = item.id.replace(/[.]/g, '')
        let tmpUser = this.state.users[userKey]
        tmpUser.isVIP = true
        tmpUser.requestVIP = false
        let tmpUsers = this.state.users;
        tmpUsers[userKey] = tmpUser;
        this.setState({users: tmpUsers})

        this.saveUsersStateOnDB();
    }

    /**
     * Télécharge le fichier des logs.
     */
    downloadLogsFile = () => {
        utils_function.downloadFileFromText(JSON.stringify(Object.values(this.state.logs)), "logs.json")
    }

    /**
     * Méthode de rendu du composant.
     */
    render() {
        
    let dateStringOptions = { hour: "numeric", minute: 'numeric' };
    let centerContent;
    switch(this.state.currentPage) {

        case CALENDAR_PAGE:
            centerContent = (
                <EventsCalendar
                    ADEevents={this.state.ADE_Events} 
                    events={this.state.events} 
                    users={this.state.users} 
                    denieAcessForThisUser={this.denieAccessForThisUser}
                    rejectRequestAccessForThisUser={this.rejectRequestAccessForThisUser}
                    acceptRequestAccesForThisUser={this.acceptRequestAccesForThisUser}
                    matchUsersStateByExistingEventsOnDB={this.matchUsersStateByExistingEventsOnDB}/>
            )
            break;
        
        case WAITING_FOR_ACCEPTATION_USERS_PAGE:
            let listEventsRequested = this.state.events.filter(e => Object.values(this.state.users).filter(u => u.requestForEvents).filter(u => u.requestForEvents.length > 0).map(u => u.requestForEvents).flat().includes(e.id)).map(er => {
                    
                    return (
                    <div key={uuidv4()}>
                        <h3>Créneau du {er.start.toLocaleDateString("fr-FR", dateStringOptions)} - {er.end.toLocaleDateString("fr-FR", dateStringOptions)}</h3>
                        <List 
                            items={Object.values(this.state.users).filter(u => u.requestForEvents).filter(u => u.requestForEvents.length > 0).filter(u => u.requestForEvents.includes(er.id))} 
                            removeItem={(index, item) => this.rejectRequestAccessForThisUser(index, item, er)}
                            validateItem={(index, item) => this.acceptRequestAccesForThisUser(index, item, er)}
                            hasValidateButton={true}/>
                    </div>)
            })
            centerContent = (
                <div>
                    <h2>Demandes d'accès au CyberLab : </h2>
                    {listEventsRequested}
                </div>
            )
            break;

        case VIP_USERS_PAGE:
            let requestVIPList = (<List 
                items={Object.values(this.state.users).filter(u => u.requestVIP === true)} 
                removeItem={(index, item) => this.denieVIPForThisUser(index, item)}
                validateItem={(index, item) => this.authorizeVIPForThisUser(index, item)}
                hasValidateButton={true}/>)

            let VIPList = (<List 
                items={Object.values(this.state.users).filter(u => u.isVIP === true)} 
                removeItem={(index, item) => this.denieVIPForThisUser(index, item)}/>)

            centerContent = (
                <div>
                    <h2>Demandes de libre accès au CyberLab : </h2>
                    {requestVIPList}
                    <h2>Utilisateurs en accès libre : </h2>
                    {VIPList}
                </div>
            )
            break;

        case SHOW_ACCEPTED_USERS_PAGE: 

            let listEventsAccepeted = this.state.events.filter(e => Object.values(this.state.users).filter(u => u.acceptedForEvents).filter(u => u.acceptedForEvents.length > 0).map(u => u.acceptedForEvents).flat().includes(e.id)).map(er => {
                    
                    return (
                    <div key={uuidv4()}>
                        <h3>Créneau du {er.start.toLocaleDateString("fr-FR", dateStringOptions)} - {er.end.toLocaleDateString("fr-FR", dateStringOptions)}</h3>
                        <List 
                            items={Object.values(this.state.users).filter(u => u.acceptedForEvents).filter(u => u.acceptedForEvents.length > 0).filter(u => u.acceptedForEvents.includes(er.id))} 
                            removeItem={(index, item) => this.denieAccessForThisUser(index, item, er)}/>
                    </div>)
            })
            centerContent = (
                <div>
                    <h2>Accès autorisés pour le CyberLab : </h2>
                    {listEventsAccepeted}
                </div>
            )
            break;

        case LOGS_PAGE:
            let logs = Object.values(this.state.logs).sort((l1, l2) => l2.timestamp - l1.timestamp).map(l => {
                let dateObject = new Date(l.timestamp*1000)
                return (<code key={uuidv4()}>. {l.user_id} est entré dans le CyberLab à la date du {dateObject.toLocaleString()}<br/></code>)
            })
            centerContent = <div className="logsContainer"><button onClick={this.downloadLogsFile}>Exporter</button><br/>{logs}</div>
            break;

        default: centerContent = (<a>Erreur de routage</a>)

    }

    return (
        <div className="mainContainer">

            <div className="leftMenu">
                <button className="leftMenuButton" onClick={this.handleCalendarButton}>Créneaux</button>
                <button className="leftMenuButton" onClick={this.handleWaitingForAcceptationUsersButton}>En attente ({Object.values(this.state.users).filter(u => u.requestForEvents).map(u => u.requestForEvents).flat().length})</button>
                <button className="leftMenuButton" onClick={this.handleVIPUsersButton}>Utilisateurs en accès libre ({Object.values(this.state.users).filter(u => u.requestVIP === true).length})</button>
                <button className="leftMenuButton" onClick={this.handleShowAcceptedUsers}>Utilisateurs acceptés</button>
                <button className="leftMenuButton" onClick={this.handleShowLogs}>Journaux</button>
                <button className="leftMenuButton" onClick={this.props.handleLogoutButton}>Se déconnecter</button>
            </div>
            <div className="centerMain">
                {(this.state.eventsRetrieved === true && this.state.usersRetrieved === true && this.state.ADE_EventsRetrieved === true && this.state.logsRetrieved === true) && centerContent}
                {(this.state.eventsRetrieved === false || this.state.usersRetrieved === false || this.state.ADE_EventsRetrieved === false || this.state.logsRetrieved === false) && <p>Chargement...</p>}
            </div>
        </div>
    )
    }
}

export default HomePage;
