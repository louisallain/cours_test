import React from 'react'
import './HomePage.css'

import EventsCalendar from '../EventsCalendar/EventsCalendar'

class HomePage extends React.Component {

    constructor(props) {

        super(props);
        this.state = {
        };
    } 

    render() {

    return (
        <div className="mainContainer">

            <div className="leftMenu">
                <p>Bienvenue {this.props.user.email}</p>
                <button onClick={this.props.handleLogoutButton}>Se déconnecter</button>
            </div>
            <div className="centerMain">
                <EventsCalendar/>
            </div>
        </div>
    )
    }
}

export default HomePage;
