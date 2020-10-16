import React from 'react'
import './EventsCalendar.css'

import { Calendar, Views, momentLocalizer } from 'react-big-calendar'
import moment from 'moment'
import 'moment/locale/fr'
import 'react-big-calendar/lib/css/react-big-calendar.css'

import events from '../events'

// Setup the localizer by providing the moment (or globalize) Object
// to the correct localizer.
const localizer = momentLocalizer(moment) // or globalizeLocalizer

class EventsCalendar extends React.Component {

    constructor(props) {

        super(props);
        this.state = {
            events: events,
            dayLayoutAlgorithm: 'no-overlap',
            slotLengthCalendar: 30,
            slotLengthChosen: 105,
            beginningOfTheDay: new Date(1970, 1, 1, 8, 0, 0),
            endOfTheDay: new Date(1970, 1, 1, 19, 30, 0)
        };
    } 

    handleSelect = ({ start, end }) => {

        end.setMinutes(end.getMinutes()+(this.state.slotLengthChosen - this.state.slotLengthCalendar))
        // vérifie qu'un créneau n'en chevauche pas un autre
        let isPossible = this.state.events.filter((ev) => (start >= ev.start && start < ev.end) || (end > ev.start && end <= ev.end)).length > 0 ? false : true

        if(start.getHours() >= this.state.beginningOfTheDay.getHours() && end.getHours() <= this.state.endOfTheDay.getHours()) {

            if(isPossible) {

                const title = window.prompt('Nouveau créneau : ')

                if (title)
                this.setState({
                    events: [
                    ...this.state.events,
                    {
                        start,
                        end,
                        title,
                    },
                    ],
                })
            } else {
                alert("Un autre créneau existe déjà à ce moment.")
            }
            
        } else {
            alert("Restez dans les limites de la journée.")
        }
    }
    
    render() {
        return (
            <div className="calendarContainer">
                <Calendar
                    selectable
                    localizer={localizer}
                    events={this.state.events}
                    defaultView={Views.WEEK}
                    views={[Views.WEEK]}
                    scrollToTime={new Date(1970, 1, 1, 6)}
                    defaultDate={new Date()}
                    onSelectEvent={event => alert(event.title)}
                    onSelectSlot={this.handleSelect}
                    dayLayoutAlgorithm={this.state.dayLayoutAlgorithm}
                    step={15}
                    timeslots={4}
                    min={this.state.beginningOfTheDay}
                    max={this.state.endOfTheDay}
                    onSelecting={() => {return false}} // éviter "d'étirer" un évnènement avec la souris
                    slotPropGetter={(date) => {return {className: "my_slot"}}}

                />
            </div>
        )
    }
  }

export default EventsCalendar;
