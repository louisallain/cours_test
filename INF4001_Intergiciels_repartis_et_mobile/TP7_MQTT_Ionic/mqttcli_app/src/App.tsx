import React from 'react';
import { Redirect, Route } from 'react-router-dom';
import {
  IonApp,
  IonIcon,
  IonLabel,
  IonRouterOutlet,
  IonTabBar,
  IonTabButton,
  IonTabs
} from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { ellipse, square, triangle } from 'ionicons/icons';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/* Theme variables */
import './theme/variables.css';
import { Cordova, cordova } from '@ionic-native/core';

/* Mes imports */
import Tab1 from './pages/Tab1';
import Settings from './pages/Settings';
import { Plugins } from '@capacitor/core';
var mqtt = require('mqtt');

interface State {
  mqttClient: any
}

class App extends React.Component<{}, State> {

  constructor(props: any) {
    super(props)
    this.state = {
      mqttClient: null
    }
  }

  /**
   * Handler de l'état de connectivité du réseau.
   * Si connecté, alors connexion au broker mqtt et souscription au topic définis dans les paramètres à l'aide de la fonction this.connectToMQTTBroker
   */
  handlerNetworkStatusChanged(status: any) {
    console.log("status changed", status)
    if(status.connected === false) {
      console.log("déconnecté !")
      Plugins.Toast.show({
        text: "Déconnecté du réseau, veuillez vous connecter à l'Internet",
        duration: "long",
        position: "bottom"
      })
    }
    else {
      console.log("connecté !")
      // Connexion au broker et souscription au topic de nouveau
      this.connectToMQTTBrokerAndSubscribe()
      Plugins.Toast.show({
        text: "Connecté au réseau. Connexion au broker de nouveau...",
        duration: "long",
        position: "bottom"
      })
    }
  }

  connectToMQTTBrokerAndSubscribe() {
    var mqtt    = require('mqtt');
    var options = {
        protocol: 'mqtts',
        // clientId uniquely identifies client
        // choose any string you wish
        clientId: 'b0908855'    
    };
    var client  = mqtt.connect('mqtt://test.mosquitto.org:8081', options);

    // preciouschicken.com is the MQTT topic
    client.subscribe('preciouschicken.com');
    client.on('message', function (topic: any, message: any) {
      let note = message.toString();
      console.log(note);
      client.end();
      });
    //this.setState({mqttClient: client})
  }

  /**
   * Ajoute un listener sur l'état de la connectivité réseau.
   * Se connecte au broker mqtt et souscrit au topic si il y a une connexion à internet.
   * Ajoute un handler sur la réception de messages depuis l'abonnement au topic mqtt.
   */
  componentDidMount() {
    Plugins.Network.addListener('networkStatusChange', this.handlerNetworkStatusChanged)
    Plugins.Network.getStatus().then((status: any) => {
      if(status.connected === true) {
        this.connectToMQTTBrokerAndSubscribe()
        
      }
    })
  }

  /**
   * Détruit tous les listeners.
   */
  componentWillUnmount() {
    Plugins.Network.removeAllListeners()
  }

  render() {
    return (
    <IonApp>
        <IonReactRouter>
          <IonTabs>
            <IonRouterOutlet>
              <Route path="/tab1" component={Tab1} exact={true} />
              <Route path="/settings" component={Settings} exact={true} />
              <Route path="/" render={() => <Redirect to="/tab1" />} exact={true} />
            </IonRouterOutlet>
            <IonTabBar slot="bottom">
              <IonTabButton tab="tab1" href="/tab1">
                <IonIcon icon={triangle} />
                <IonLabel>Messages</IonLabel>
              </IonTabButton>
              <IonTabButton tab="settings" href="/settings">
                <IonIcon icon={ellipse} />
                <IonLabel>Paramètres</IonLabel>
              </IonTabButton>
            </IonTabBar>
          </IonTabs>
        </IonReactRouter>
      </IonApp>
    );
  }
}

export default App;
