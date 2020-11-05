import React from 'react'
import { Calendar, Views, momentLocalizer } from 'react-big-calendar'
import moment from 'moment'
import 'moment/locale/fr'
import { v4 as uuidv4 } from 'uuid'
import Modal from 'react-modal'
import * as firebase from '../utils/firebase_config'
import * as myUtils from '../utils/utils_function'
import List from '../List/List'

import './react-big-calendar.css'
import './EventsCalendar.css'


const navigate = {
    PREVIOUS: 'PREV',
    NEXT: 'NEXT',
    TODAY: 'TODAY',
    DATE: 'DATE',
}

// Setup the localizer by providing the moment (or globalize) Object
// to the correct localizer.
const localizer = momentLocalizer(moment) // or globalizeLocalizer

/**
 * Classe du composant représentant la barre d'outil du calendrier.
 */
class CustomToolbar extends React.Component {

    /**
     * Méthode de rendu du composant.
     */
    render() {
        let { localizer: { messages }, label } = this.props
        return(
            <div className="rbc-toolbar">
                <span className="rbc-btn-group">
                    <button type="button" onClick={this.navigate.bind(null, navigate.PREVIOUS)}>Précédent</button>
                    <button type="button" onClick={this.navigate.bind(null, navigate.TODAY)}>Aujourd'hui</button>
                    <button type="button" onClick={this.navigate.bind(null, navigate.NEXT)}>Suivant</button>
                </span>
                <span className="rbc-toolbar-label">{label}</span>
                {this.props.changesSaved ? 
                <button type="button" disabled style={{backgroundColor: "grey"}}>Sauvegardé !</button>
                :
                <button type="button" style={{backgroundColor: "#4CAF50"}} onClick={this.props.saveEventsOnDB}>Sauvegarder</button>
                }
                <span className="rbc-btn-group">
                    <button type="button" onClick={this.props.downloadJSONEvents}>Télécharger les créneaux</button>
                    <button type="button" onClick={this.props.openJSONLoadModal}>Charger des créneaux</button>
                </span>
            </div>
        )
    }

    /**
     * Méthode permettant de naviguer entre les semaines du calendrier.
     */
    navigate = action => {
        this.props.onNavigate(action)
    }
}

/**
 * Composant représentant un créneau sur le calendrier.
 * @param {*} props propriétés héritées du parent.
 */
function CustomEvent(props) {
    return (
        <div className="customEventContainer" title="Double cliquer pour plus d'infos">
            <p>
                {props.usersAcceptedForThisEvent.length} accès<br/>
                {props.usersRequestForThisEvent.length} demandes
            </p>
            <button className="deleteEventButton" title="Supprimer ce créneau" onClick={() => props.deleteEvent(props.event)}>X</button>
        </div>
    )
}

/**
 * Classe représentant le calendrier des créneaux.
 */
class EventsCalendar extends React.Component {

    /**
     * Initialise l'état du composant.
     * @param {Object} props propriétés héritées du parent.
     */
    constructor(props) {

        super(props);
        this.JSONEventsFileInput = React.createRef()
        this.keepExistingEventsCheckbox = React.createRef()
        this.fileReader = new FileReader()
        this.state = {
            events: this.props.events,
            dayLayoutAlgorithm: 'no-overlap',
            slotLengthCalendar: 30,
            slotLengthChosen: 90,
            beginningOfTheDay: new Date(1970, 1, 1, 8, 0, 0),
            endOfTheDay: new Date(1970, 1, 1, 19, 30, 0),
            showLoadJSONModal: false,
            changesSaved: true,
            showShowMoreForEventModal: false,
            usersOfTheSelectedEvent: [],
            usersRequestOfTheSelectedEvent: [],
            selectedEvent: null,
        };
    } 

    /**
     * Fonction invoquée lorsque le composant est monté.
     * Ajoute un listener au chargement de l'entrée d'un fichier des créneaux.
     * Ajoute un listener lorsque l'utilisateur quitte la page afin de vérifier si l'état des créneaux dans le calendrier a été sauvegardé en BDD.
     */
    componentDidMount() {
        this.fileReader.onload = (event) => {
            try {
                if(this.keepExistingEventsCheckbox.current.checked === false) {
                    this.setState({events: []})
                }
                let tmpEvents = JSON.parse(event.target.result);
                tmpEvents.forEach(e => {
                    e.start = new Date(e.start)
                    e.end = new Date(e.end)
                    this.addEvent(e, false)
                })
                this.handleCloseLoadJSONModal()
            }
            catch(e) {
                console.log(e)
                alert("Veuillez respecter le format des créneaux suivant : {start: instanceOf(Date), end: instanceOf(Date)}")
            }
        }

        window.onbeforeunload = (e) => {
            if(!this.state.changesSaved) return "Les changements sur les créneaux ne sont pas sauvegardés, êtes vous sûr de vouloir quitter la page ?"
        }
    }

    /**
     * Fonction invoquée avant que le composant soit démonté.
     * Vérifie que les changements courants sont sauvegardés en BDD.
     */
    componentWillUnmount = () => {
        if(this.state.changesSaved === false) {
            let c = window.confirm("Voulez vous enregistrer les changements apportés aux créneaux ?")
            if(c) this.saveEventsOnDB();
        }
    }

    /**
     * Handler d'ouverture du modal de chargement d'un fichier de créneaux.
     */
    handleOpenLoadJSONModal = () => {
        this.setState({showLoadJSONModal: true})
    }

    /**
     * Handler de fermeture du modal de chargement d'un fichier de créneaux.
     */
    handleCloseLoadJSONModal = () => {
        this.setState({showLoadJSONModal: false})
    }

    /**
     * Ajoute un évènement sur le calendrier en précisant la date de début et de fin.
     */
    addEvent = ({ start, end}, activateFeedback) => {

        start = new Date(start)
        end = new Date(start) 
        end.setMinutes(start.getMinutes() + this.state.slotLengthChosen) // recalcule la fin du créneau en fonction de la durée prévu d'un créneau
        // vérifie qu'un créneau n'en chevauche pas un autre
        let isPossible = this.state.events.filter((ev) => (start >= ev.start && start < ev.end) || (end > ev.start && end <= ev.end)).length > 0 ? false : true

        if(start.getHours() >= this.state.beginningOfTheDay.getHours() && end.getHours() <= this.state.endOfTheDay.getHours()) {

            if(isPossible) {
                const title = "CyberLab dispo"

                if (title)
                this.setState({
                    events: [
                    ...this.state.events,
                    {
                        id: uuidv4(),
                        start,
                        end,
                        title,
                    },],
                    changesSaved: false,
                })
            } else {
                if(activateFeedback) alert("Un autre créneau existe déjà à ce moment.")
            }
            
        } else {
            if(activateFeedback) alert("Restez dans les limites de la journée.")
        }
    }

    /**
     * Supprime un créneau.
     */
    handleRemoveEvent = (event) => {
        let c = window.confirm("Supprimer le créneau ?")
        if(c) this.setState({
                                events: this.state.events.filter(e => e.id !== event.id),
                                changesSaved: false,
                            })
    }

    /**
     * Handler permettant d'afficher le modal affichangt les détails d'un créneau.
     */
    handleShowMoreOfTheEvent = (event) => {
        this.setState({
            showShowMoreForEventModal: true, 
            usersOfTheSelectedEvent: Object.values(this.props.users).filter(u => u.acceptedForEvents).filter(u => u.acceptedForEvents.includes(event.id)), 
            usersRequestOfTheSelectedEvent: Object.values(this.props.users).filter(u => u.requestForEvents).filter(u => u.requestForEvents.includes(event.id)), 
            selectedEvent: event
        })
    }

    /**
     * Handler permettant de fermer le modal affichant les détails d'un créneau.
     */
    handleCloseShowMoreOfEventModal = () => {
        this.setState({showShowMoreForEventModal: false})
    }

    /**
     * Handler permettant d'envoyer un fichier ce créneaux JSON.
     */
    handleSubmitJSONEventsFile = (evt) => {
        evt.preventDefault()
        if(this.JSONEventsFileInput.current.files[0]) {
            this.fileReader.readAsText(this.JSONEventsFileInput.current.files[0])
        }
        else {
            alert("Sélectionner un fichier d'abord.")
        }
    }

    /**
     * Télécharge le fichiers des créneaux DEPUIS LA BDD et non pas forcémément les créneaux affichés (si l'utilisateur n'a pas sauvegardé).
     */
    downloadJSONEvents = () => {

        firebase.fbDatabase
            .ref("events")
            .once("value", 
                (snapshot) => {
                    console.log(snapshot.val())
                    myUtils.downloadFileFromText(snapshot.val(), "creneaux.json")
                }, 
                (error) => console.error(error)
            )
    }

    /**
     * Sauvegarde l'état actuel des créneaux en BDD.
     */
    saveEventsOnDB = () => {       
        firebase.fbDatabase.ref("events").set(JSON.stringify(this.state.events), (error) => {
            if(error) {
                console.log(error)
                alert(error)
            } else {
                this.props.matchUsersStateByExistingEventsOnDB(this.state.events);
                this.setState({changesSaved: true})
                alert("Changements sauvegardés !")
            }
        })
    }

    /**
     * Handler du bouton où l'admin réfute l'accès d'un utilisateur à un créneau.
     */
    handleDenyUserAccess = (index, item) => {
        this.props.denieAcessForThisUser(index, item, this.state.selectedEvent, () => {
            this.setState({
                usersOfTheSelectedEvent: this.state.usersOfTheSelectedEvent.filter(u => u.id !== item.id)
            })
        })
    }

    /**
     * Handler du bouton où l'admin rejette la demande d'accès d'un utilisateur à un créneau.
     */
    handleRejectUserRequestAccess = (index, item) => {
        this.props.rejectRequestAccessForThisUser(index, item, this.state.selectedEvent, () => {
            this.setState({
                usersRequestOfTheSelectedEvent: this.state.usersRequestOfTheSelectedEvent.filter(u => u.id !== item.id)
            })
        })
    }

    /**
     * Handler du bouton où l'admin accepte la demande d'accès d'un utilisateur à créneau.
     */
    handleAcceptUserRequestAccess = (index, item) => {
        let tmp = this.state.usersOfTheSelectedEvent
        tmp.push(item)
        this.props.acceptRequestAccesForThisUser(index, item, this.state.selectedEvent, () => {
            this.setState({
                usersOfTheSelectedEvent: tmp,
                usersRequestOfTheSelectedEvent: this.state.usersRequestOfTheSelectedEvent.filter(u => u.id !== item.id)
            })
        })
    }

    /**
     * Méthode de rendu du composant.
     */
    render() {
        return (
            <div className="calendarContainer">
                {/* Modal servant à charger un fichier de créneaux JSON */}
                <Modal 
                    isOpen={this.state.showLoadJSONModal}
                    contentLabel="Load JSON custom events"
                    className="modal loadJSONModal">
                    <button className="closeJSONModal" onClick={this.handleCloseLoadJSONModal}>Fermer</button>
                    <p>
                        Charger un fichier de créneaux JSON contenant un tableau où chaque élément contient :<br/>
                        <code>
                        start: instanceOf(Date) // début du créneau<br/>
                        end : instanceOf(Date) // fin du créneau<br/>
                        </code>
                        Les créneaux chevauchant d'autres créneaux ne seront pas pris en compte.<br/>
                    </p>
                    <form onSubmit={this.handleSubmitJSONEventsFile}>
                        <label>Garder les créneaux existants
                            <input type="checkbox" name="keepExistingEvents" ref={this.keepExistingEventsCheckbox}/>
                        </label>
                        <br/>
                        <input type="file" accept=".json" ref={this.JSONEventsFileInput} />
                        <br />
                        <button type="submit">Envoyer</button>
                    </form>
                </Modal>
                {/* Modal utilisé lorsque l'utilisteur double clique sur un créneau */}
                <Modal 
                    isOpen={this.state.showShowMoreForEventModal}
                    contentLabel="Show more of event"
                    className="modal showMoreOfTheEventModal">
                    <div className="closeShowMoreModalContainer">
                        <button className="closeShowMoreOfEventModalButton" onClick={this.handleCloseShowMoreOfEventModal}>Fermer</button>
                    </div>
                    <div className="listContainerShowMore">
                        {this.state.usersOfTheSelectedEvent.length > 0 && 
                        <div className="usersAcceptedListContainer">
                            <h2>Liste des utilisateurs autorisés pour ce créneau :</h2>
                            <List 
                                items={this.state.usersOfTheSelectedEvent} 
                                removeItem={(index, item) => this.handleDenyUserAccess(index, item)}/>
                        </div>}
                        {this.state.usersRequestOfTheSelectedEvent.length > 0 &&
                        <div className="userRequestListContainer">
                            <h2>Liste des utilisateurs en attentes pour ce créneau :</h2>
                            <List 
                                items={this.state.usersRequestOfTheSelectedEvent} 
                                hasValidateButton={true}
                                removeItem={(index, item) => {this.handleRejectUserRequestAccess(index, item)}}
                                validateItem={(index, item) => {this.handleAcceptUserRequestAccess(index, item)}}/>
                        </div>
                        }
                    </div>
                    
                </Modal>
                    {/* Calendrier des créneaux */}
                <Calendar
                    selectable
                    localizer={localizer}
                    events={this.state.events}
                    defaultView={Views.WEEK}
                    views={[Views.WEEK]}
                    scrollToTime={new Date(1970, 1, 1, 6)}
                    defaultDate={new Date()}
                    //onSelectEvent={event => alert(event.title)}
                    onDoubleClickEvent={this.handleShowMoreOfTheEvent}
                    onSelectSlot={(t) => this.addEvent(t, true)}
                    dayLayoutAlgorithm={this.state.dayLayoutAlgorithm}
                    step={15}
                    timeslots={4}
                    min={this.state.beginningOfTheDay}
                    max={this.state.endOfTheDay}
                    onSelecting={() => {return false}} // éviter "d'étirer" un évnènement avec la souris
                    slotPropGetter={(date) => {return {className: "my_slot"}}}
                    components={{
                        toolbar: props => ( 
                                <CustomToolbar 
                                    {...props} 
                                    openJSONLoadModal={this.handleOpenLoadJSONModal}
                                    downloadJSONEvents={this.downloadJSONEvents}
                                    changesSaved={this.state.changesSaved}
                                    saveEventsOnDB={this.saveEventsOnDB}/>),
                        event : props => (
                                <CustomEvent
                                {...props}
                                usersAcceptedForThisEvent={Object.values(this.props.users).filter(u => u.acceptedForEvents).filter(u => u.acceptedForEvents.includes(props.event.id))}
                                usersRequestForThisEvent={Object.values(this.props.users).filter(u => u.requestForEvents).filter(u => u.requestForEvents.includes(props.event.id))}
                                deleteEvent={this.handleRemoveEvent}
                                />)
                    }}

                />
            </div>
        )
    }
  }

export default EventsCalendar;
