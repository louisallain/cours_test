import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import { Plugins } from '@capacitor/core';

import './Settings.css';

import * as VALUES from '../values';

interface State {
  mqttServerAdress: string;
  mqttPort: number;
  topic: string;
}

class Settings extends React.Component<{}, State> {

  constructor(props: any) {
    super(props);
    
    this.state = {
      mqttServerAdress: "",
      mqttPort: 1883,
      topic: "",
    }
  }

  /**
   * Récupère les paramètres depuis le Storage.
   */
  componentDidMount() {
    Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_BROKER_ADRESS_REF}).then((r) => this.setState({mqttServerAdress: r.value || ""}))
    Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_BROKER_PORT_REF}).then((r) => this.setState({mqttPort: Number(r.value)}))
    Plugins.Storage.get({key: VALUES.NATIVE_STORAGE_TOPIC_REF}).then((r) => this.setState({topic: r.value || ""}))
    
  }

  /**
   * Sauvegarde les paramètres sur le Storage.
   */
  saveConfigToStorage(state: any) {
    Plugins.Storage.set({key: VALUES.NATIVE_STORAGE_BROKER_ADRESS_REF, value: state.mqttServerAdress})
    Plugins.Storage.set({key: VALUES.NATIVE_STORAGE_BROKER_PORT_REF, value: state.mqttPort.toString()})
    Plugins.Storage.set({key: VALUES.NATIVE_STORAGE_TOPIC_REF, value: state.topic})
  }

  render() {
    return (
      <IonPage>
        <IonHeader>
          <IonToolbar>
            <IonTitle>Paramètres</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonContent fullscreen>
          <IonHeader collapse="condense">
            <IonToolbar>
              <IonTitle size="large">Paramètres</IonTitle>
            </IonToolbar>
          </IonHeader>
          <div className="formContainer">
            <input className="input" placeholder="Adresse du broker MQTT" value={this.state.mqttServerAdress} onChange={(event) => this.setState({mqttServerAdress: event.target.value})}/>
            <input className="input" type="number" placeholder="Port du broker MQTT" value={this.state.mqttPort} onChange={(event) => this.setState({mqttPort: parseInt(event.target.value)})}/>
            <input className="input" placeholder="Sujet (Topic)" value={this.state.topic} onChange={(event) => this.setState({topic: event.target.value})}/>
            <button className="input" onClick={() => this.saveConfigToStorage(this.state)}>Connexion</button>
          </div>
        </IonContent>
      </IonPage>
    );
  }
}

export default Settings;
