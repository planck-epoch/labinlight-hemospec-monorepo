import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonToggle } from '@ionic/react';

const Settings: React.FC = () => {
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
             <IonItem detail button routerLink="/login" routerDirection="root">
                <IonLabel color="danger">Log Out</IonLabel>
            </IonItem>
        </IonList>
      </IonContent>
    </IonPage>
  );
};

export default Settings;
