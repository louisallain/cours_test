import React from 'react'
import { Calendar, Views, momentLocalizer } from 'react-big-calendar'
import moment from 'moment'
import 'moment/locale/fr'
import { v4 as uuidv4 } from 'uuid';
import Modal from 'react-modal'
import * as firebase from '../utils/firebase_config'
import * as myUtils from '../utils/utils_function'

import './EventsCalendar.css'
import 'react-big-calendar/lib/css/react-big-calendar.css'

const navigate = {
    PREVIOUS: 'PREV',
    NEXT: 'NEXT',
    TODAY: 'TODAY',
    DATE: 'DATE',
}

var events = []

// Setup the localizer by providing the moment (or globalize) Object
// to the correct localizer.
const localizer = momentLocalizer(moment) // or globalizeLocalizer


class CustomToolbar extends React.Component {

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
                <button type="button" style={{backgroundColor: "#42f575"}} onClick={this.props.saveEventsOnDB}>Sauvegarder</button>
                }
                <span className="rbc-btn-group">
                    <button type="button" onClick={this.props.downloadJSONEvents}>Télécharger le fichier des créneaux</button>
                    <button type="button" onClick={this.props.openJSONLoadModal}>Charger un fichier de créneaux</button>
                </span>
            </div>
        )
    }
    navigate = action => {
        this.props.onNavigate(action)
    }
}

function CustomEvent(props) {
    return (
        <div className="customEventContainer">
            <a className="titleEvent">{props.event.title}</a>
            <button className="deleteEventButton" title="Supprimer ce créneau" onClick={() => props.deleteEvent(props.event)}>X</button>
        </div>
    )
}

class EventsCalendar extends React.Component {

    constructor(props) {

        super(props);
        this.JSONEventsFileInput = React.createRef()
        this.keepExistingEventsCheckbox = React.createRef()
        this.fileReader = new FileReader()
        this.state = {
            events: events,
            dayLayoutAlgorithm: 'no-overlap',
            slotLengthCalendar: 30,
            slotLengthChosen: 105,
            beginningOfTheDay: new Date(1970, 1, 1, 8, 0, 0),
            endOfTheDay: new Date(1970, 1, 1, 19, 30, 0),
            showLoadJSONModal: false,
            changesSaved: true,
            eventsRetrieved: false,
        };

        this.fileReader.onload = (event) => {
            try {
                if(this.keepExistingEventsCheckbox.current.checked === false) {
                    this.setState({events: []})
                }
                JSON.parse(event.target.result).map(e => {
                    if(e.start && e.end) {
                        e.start = new Date(e.start)
                        e.end = new Date(e.end)
                        this.addEvent(e)
                    }
                })
                this.handleCloseLoadJSONModal()
            }
            catch(e) {
                console.log(e)
                alert("Veuillez respecter le format des créneaux suivant : {start: instanceOf(Date), end: instanceOf(Date)}")
            }
        }
    } 

    componentDidMount = () => {
        this.retrieveEventsFromDB()
    }

    handleOpenLoadJSONModal = () => {
        this.setState({showLoadJSONModal: true})
    }

    handleCloseLoadJSONModal = () => {
        this.setState({showLoadJSONModal: false})
    }

    addEvent = ({ start, end }) => {

        end.setMinutes(end.getMinutes()+(this.state.slotLengthChosen - this.state.slotLengthCalendar))
        // vérifie qu'un créneau n'en chevauche pas un autre
        let isPossible = this.state.events.filter((ev) => (start >= ev.start && start < ev.end) || (end > ev.start && end <= ev.end)).length > 0 ? false : true

        if(start.getHours() >= this.state.beginningOfTheDay.getHours() && end.getHours() <= this.state.endOfTheDay.getHours()) {

            if(isPossible) {

                //const title = window.prompt('Nouveau créneau : ')
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
                alert("Un autre créneau existe déjà à ce moment.")
            }
            
        } else {
            alert("Restez dans les limites de la journée.")
        }
    }

    handleRemoveEvent = (event) => {
        let c = window.confirm("Supprimer le créneau ?")
        if(c) this.setState({
                                events: this.state.events.filter(e => e.id !== event.id),
                                changesSaved: false,
                            })
    }

    handleShowMoreOfTheEvent = (event) => {
        console.log("Affiche les personnes acceptés pour ce créneaux : ", event)
    }

    handleSubmitJSONEventsFile = (evt) => {
        evt.preventDefault()
        if(this.JSONEventsFileInput.current.files[0]) {
            this.fileReader.readAsText(this.JSONEventsFileInput.current.files[0])
        }
        else {
            alert("Séléectionner un fichier d'abord.")
        }
    }

    downloadJSONEvents = () => {

        firebase.fbDatabase
            .ref("events")
            .on("value", 
                (snapshot) => {
                    myUtils.downloadFileFromText(snapshot.val(), "creneaux.json")
                }, 
                (error) => console.error(error)
            )
    }

    saveEventsOnDB = () => {
        console.log("Events to be saved : ", this.state.events)
        

        firebase.fbDatabase.ref("events").set(JSON.stringify(this.state.events), (error) => {
            if(error) {
                console.log(error)
                alert(error)
            } else {
                alert("Changements sauvegardés !")
                this.setState({changesSaved: true})
            }
        })
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
    
    render() {

        if(this.state.eventsRetrieved) {
            return (
                <div className="calendarContainer">
                    <Modal 
                        isOpen={this.state.showLoadJSONModal}
                        contentLabel="Load JSON custom events"
                        className="loadJSONModal">
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
                        onSelectSlot={this.addEvent}
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
                                    deleteEvent={this.handleRemoveEvent}
                                    />)
                        }}
    
                    />
                </div>
            )
        } else {
            return (
                <div>
                    <a>Chargement des créneaux...</a>
                </div>
            )
        }
        
    }
  }

export default EventsCalendar;
