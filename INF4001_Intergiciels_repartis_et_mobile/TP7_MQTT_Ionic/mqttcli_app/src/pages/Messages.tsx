import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/react';
import ExploreContainer from '../components/ExploreContainer';
import './Messages.css';

interface Props {
  messages: Array<String>
}

class Messages extends React.Component<Props, {}> {

  render() {
    return (
      <IonPage>
        <IonHeader>
          <IonToolbar>
            <IonTitle>Messages</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonContent fullscreen>
          <IonHeader collapse="condense">
            <IonToolbar>
              <IonTitle size="large">Tab 1</IonTitle>
            </IonToolbar>
          </IonHeader>
          
          <div className="listContainer">
            {
              this.props.messages.map(m => {
                return (
                  <div className="listItem">
                    <p>{m}</p>
                  </div>
                )
              })
            }
          </div>
        </IonContent>
      </IonPage>
    );
  }
  
};

export default Messages;
