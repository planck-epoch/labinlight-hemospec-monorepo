import React, { useState } from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonToggle, IonIcon } from '@ionic/react';
import { bugOutline } from 'ionicons/icons';
import DebugConsoleModal from '../components/DebugConsoleModal';

const Settings: React.FC = () => {
  const [showDebug, setShowDebug] = useState(false);

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Settings</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonList inset>
            <IonItem>
                <IonLabel>Dark Mode</IonLabel>
                <IonToggle slot="end" checked={true} />
            </IonItem>
            <IonItem detail button>
                <IonLabel>Language</IonLabel>
                <IonLabel slot="end">English</IonLabel>
            </IonItem>
            <IonItem detail button>
                <IonLabel>Account</IonLabel>
            </IonItem>

            <IonItem button onClick={() => setShowDebug(true)}>
                <IonIcon icon={bugOutline} slot="start" color="warning" />
                <IonLabel>Debug Logs</IonLabel>
            </IonItem>

             <IonItem detail button routerLink="/login" routerDirection="root">
                <IonLabel color="danger">Log Out</IonLabel>
            </IonItem>
        </IonList>

        <DebugConsoleModal isOpen={showDebug} onClose={() => setShowDebug(false)} />
      </IonContent>
    </IonPage>
  );
};

export default Settings;
