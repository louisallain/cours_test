import React from 'react'
import './HomePage.css'

import EventsCalendar from '../EventsCalendar/EventsCalendar'
import List from '../List/List'

import { v4 as uuidv4 } from 'uuid'

import * as firebase from '../utils/firebase_config'

const CALENDAR_PAGE = "calendar_page"
const WAITING_FOR_ACCEPTATION_USERS_PAGE = "waiting_for_accpt_users_page"
const VIP_USERS_PAGE = "vip_users_page"
const SHOW_ACCEPTED_USERS_PAGE = "show_accepted_users_page"

class HomePage extends React.Component {

    constructor(props) {

        super(props);
        this.state = {
            currentPage: CALENDAR_PAGE,
            events: [],
            eventsRetrieved: false,
            users: [],
            usersRetrieved: false,
        };
    } 

    componentDidMount() {
        this.retrieveEventsFromDB();
        this.retrieveUsersFromDB();
    }

    retrieveUsersFromDB = () => {
        console.log("Retrieving users from db...")
        firebase.fbDatabase
            .ref("users")
            .on("value", 
                (snapshot) => {
                    this.setState({
                        users: JSON.parse(snapshot.val()),
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

    retrieveEventsFromDB = () => {
        console.log("Retrieving events from db...")
        firebase.fbDatabase
            .ref("events")
            .on("value", 
                (snapshot) => {
                    let tmpEvents = JSON.parse(snapshot.val())
                    tmpEvents.map(e => {
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

    handleCalendarButton = () => {
        this.setState({currentPage: CALENDAR_PAGE})
    }

    handleWaitingForAcceptationUsersButton = () => {
        this.setState({currentPage: WAITING_FOR_ACCEPTATION_USERS_PAGE})
    }

    handleVIPUsersButton = () => {
        this.setState({currentPage: VIP_USERS_PAGE})
    }

    handleShowAcceptedUsers = () => {
        this.setState({currentPage: SHOW_ACCEPTED_USERS_PAGE})
    }

    /**
     * Met à jour en BDD les utilisateurs.
     * C'est à dire que pour chaque utilisateur, on vérifie si les créneaux auxquels ils ont accès existent toujours après les modifications.
     * De même que pour les créneaux auxquels ils souhaitent accéder.
     */
    matchUsersStateByExistingEventsOnDB = (events) => {
        let tmpUsers = [...this.state.users]
        tmpUsers.map((u) => {
            u.requestForEvents = u.requestForEvents.filter(er => events.flatMap(e => e.id).includes(er))
            u.acceptedForEvents = u.acceptedForEvents.filter(er => events.flatMap(e => e.id).includes(er))
        })
        
        this.setState({
            users: tmpUsers
        })
        this.saveUsersStateOnDB();
    }

    /**
     * Sauvegarde l'état actuel des utilisateurs sur la BDD.
     */
    saveUsersStateOnDB = () => {
        firebase.fbDatabase.ref("users").set(JSON.stringify(this.state.users), (error) => {
            if(error) {
                console.log(error)
                alert(error)
            } else {
                console.log("Changes of users list saved on DB")
            }
        })
    }

    denieAccessForThisUser = (index, item, ev, callback) => {
        let tmpUserIndex = this.state.users.findIndex(u => u.id === item.id)
        let tmpUser = this.state.users[tmpUserIndex]
        tmpUser.acceptedForEvents = tmpUser.acceptedForEvents.filter(e => e != ev.id)
        let tmpUsers = this.state.users;
        tmpUsers[tmpUserIndex] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()
        
        this.saveUsersStateOnDB();
    }

    rejectRequestAccessForThisUser = (index, item, ev, callback) => {
        let tmpUserIndex = this.state.users.findIndex(u => u.id === item.id)
        let tmpUser = this.state.users[tmpUserIndex]
        tmpUser.requestForEvents = tmpUser.requestForEvents.filter(e => e != ev.id)
        let tmpUsers = this.state.users;
        tmpUsers[tmpUserIndex] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()
        
        this.saveUsersStateOnDB();
    }

    acceptRequestAccesForThisUser = (index, item, ev, callback) => {
        let tmpUserIndex = this.state.users.findIndex(u => u.id === item.id)
        let tmpUser = this.state.users[tmpUserIndex]
        tmpUser.requestForEvents = tmpUser.requestForEvents.filter(e => e != ev.id)
        tmpUser.acceptedForEvents.push(ev.id)
        let tmpUsers = this.state.users;
        tmpUsers[tmpUserIndex] = tmpUser;
        this.setState({users: tmpUsers})
        if(callback) callback()

        this.saveUsersStateOnDB();
    }

    denieVIPForThisUser = (index, item) => {
        let tmpUserIndex = this.state.users.findIndex(u => u.id === item.id)
        let tmpUser = this.state.users[tmpUserIndex]
        tmpUser.isVIP = false
        tmpUser.requestVIP = false
        let tmpUsers = this.state.users;
        tmpUsers[tmpUserIndex] = tmpUser;
        this.setState({users: tmpUsers})

        this.saveUsersStateOnDB();
    }

    authorizeVIPForThisUser = (index, item) => {
        let tmpUserIndex = this.state.users.findIndex(u => u.id === item.id)
        let tmpUser = this.state.users[tmpUserIndex]
        tmpUser.isVIP = true
        tmpUser.requestVIP = false
        let tmpUsers = this.state.users;
        tmpUsers[tmpUserIndex] = tmpUser;
        this.setState({users: tmpUsers})

        this.saveUsersStateOnDB();
    }

    render() {
        
    let dateStringOptions = { hour: "numeric", minute: 'numeric' };
    let centerContent;
    switch(this.state.currentPage) {

        case CALENDAR_PAGE:
            centerContent = (
                <EventsCalendar 
                    events={this.state.events} 
                    users={this.state.users} 
                    denieAcessForThisUser={this.denieAccessForThisUser}
                    rejectRequestAccessForThisUser={this.rejectRequestAccessForThisUser}
                    acceptRequestAccesForThisUser={this.acceptRequestAccesForThisUser}
                    matchUsersStateByExistingEventsOnDB={this.matchUsersStateByExistingEventsOnDB}/>
            )
            break;
        
        case WAITING_FOR_ACCEPTATION_USERS_PAGE:
            let listEventsRequested = this.state.events.filter(e => this.state.users.filter(u => u.requestForEvents.length > 0).map(u => u.requestForEvents).flat().includes(e.id)).map(er => {
                    
                    return (
                    <div key={uuidv4()}>
                        <h3>Créneau du {er.start.toLocaleDateString("fr-FR", dateStringOptions)} - {er.end.toLocaleDateString("fr-FR", dateStringOptions)}</h3>
                        <List 
                            items={this.state.users.filter(u => u.requestForEvents.length > 0).filter(u => u.requestForEvents.includes(er.id))} 
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
                items={this.state.users.filter(u => u.requestVIP === true)} 
                removeItem={(index, item) => this.denieVIPForThisUser(index, item)}
                validateItem={(index, item) => this.authorizeVIPForThisUser(index, item)}
                hasValidateButton={true}/>)

            let VIPList = (<List 
                items={this.state.users.filter(u => u.isVIP === true)} 
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

            let listEventsAccepeted = this.state.events.filter(e => this.state.users.filter(u => u.acceptedForEvents.length > 0).map(u => u.acceptedForEvents).flat().includes(e.id)).map(er => {
                    
                    return (
                    <div key={uuidv4()}>
                        <h3>Créneau du {er.start.toLocaleDateString("fr-FR", dateStringOptions)} - {er.end.toLocaleDateString("fr-FR", dateStringOptions)}</h3>
                        <List 
                            items={this.state.users.filter(u => u.acceptedForEvents.length > 0).filter(u => u.acceptedForEvents.includes(er.id))} 
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

        default: centerContent = (<a>Erreur de routage</a>)

    }

    return (
        <div className="mainContainer">

            <div className="leftMenu">
                <button className="leftMenuButton" onClick={this.handleCalendarButton}>Créneaux</button>
                <button className="leftMenuButton" onClick={this.handleWaitingForAcceptationUsersButton}>En attente ({this.state.users.map(u => u.requestForEvents).flat().length})</button>
                <button className="leftMenuButton" onClick={this.handleVIPUsersButton}>Utilisateurs en accès libre ({this.state.users.filter(u => u.requestVIP === true).length})</button>
                <button className="leftMenuButton" onClick={this.handleShowAcceptedUsers}>Utilisateurs acceptés</button>
                <button className="leftMenuButton" onClick={this.props.handleLogoutButton}>Se déconnecter</button>
            </div>
            <div className="centerMain">
                {(this.state.eventsRetrieved === true && this.state.usersRetrieved === true) && centerContent}
                {(this.state.eventsRetrieved === false || this.state.usersRetrieved === false) && <p>Chargement...</p>}
            </div>
        </div>
    )
    }
}

export default HomePage;
