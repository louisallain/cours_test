import React from 'react'
import './HomePage.css'

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

            </div>
            <div className="centerMain">
                <p>Bienvenue {this.props.user.email}</p>
                <button onClick={this.props.handleLogoutButton}>Se déconnecter</button>
            </div>
        </div>
    )
    }
}

export default HomePage;
