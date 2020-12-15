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
import Messages from './pages/Messages';
import Settings from './pages/Settings';
import { Plugins } from '@capacitor/core';
import * as VALUES from './values';

interface State {
  messages: any,
  mqttClient: any,
}

class App extends React.Component<{}, State> {

  constructor(props: any) {
    super(props)
    this.state = {
      messages: [], // tableau des messages reçus depuis le sujet mqtt
      mqttClient: null
    }
  }

  /**
   * Retrouve la configuration donné par l'utilisateur dans le storage. Indique les erreurs avec des toasts.
   * Se connecte au broker MQTT et s'abonne au sujet défini dans les paramètres.
   */
  connectToMQTTBrokerAndSubscribe = () => {
    var mqtt = require('mqtt');
    // var client  = mqtt.connect('wss://test.mosquitto.org:8081');

    Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_BROKER_ADRESS_REF})
    .then((broker_addr) => {
      Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_BROKER_PORT_REF})
      .then((broker_port) => {
        Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_TOPIC_REF})
        .then((topic) => {

          var mqtt    = require('mqtt');
          if(this.state.mqttClient != null) {
            this.state.mqttClient.end()
          }
          var client  = mqtt.connect(`${broker_addr.value}:${broker_port.value}`, {
            reconnectPeriod: 0
          });

          // Après connexion, s'abonne au topic 
          client.on('connect', function () {
            client.subscribe(topic.value, function (err : any) {
              if (!err) {
                console.log("Connexion au broker mqtt ok")
                Plugins.Toast.show({
                  text: "Connexion réussi !",
                  duration: "long",
                  position: "bottom"
                })
              }
              else {
                console.error(err)
                Plugins.Toast.show({
                  text: "Erreur de connexion au Broker : " + err.toString(),
                  duration: "long",
                  position: "bottom"
                })
              }
            })
          })
          
          // Handler de la réception d'un message
          client.on('message', (topic : any, message : any) => {
            console.log(message)
            // on ne vérifie pas le topic puisque l'on suppose qu'il n'y que un seul abonnement à un topic
            // Ajoute le nouveau message
            console.log(this.state)
            this.setState({messages : [...this.state.messages, message.toString()]})
          })

          client.on('error', () => {
            console.log("ERREUER")
            Plugins.Toast.show({
              text: "Erreur MQTT",
              duration: "long",
              position: "bottom"
            })
          })

          this.setState({mqttClient: client})
        })
      })
    })
  }

   /**
   * Handler de l'état de connectivité du réseau.
   * Si connecté, alors connexion au broker mqtt et souscription au topic définis dans les paramètres à l'aide de la fonction this.connectToMQTTBroker
   */
  handlerNetworkStatusChanged = (status: any) => {
    console.log("status changed", status)
    if(status.connected === false) {
      console.log("déconnecté !")
      this.state.mqttClient.end()
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
              <Route path="/messages" exact={true}>
                <Messages messages={this.state.messages} />
              </Route>
              <Route path="/settings" exact={true}>
                <Settings setupMQTT={this.connectToMQTTBrokerAndSubscribe} />
              </Route>
              <Route path="/" render={() => <Redirect to="/messages" />} exact={true} />
            </IonRouterOutlet>
            <IonTabBar slot="bottom">
              <IonTabButton tab="messages" href="/messages">
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
