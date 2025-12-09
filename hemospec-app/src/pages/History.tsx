import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonList, IonItem, IonLabel } from '@ionic/react';

const History: React.FC = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>History</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">History</IonTitle>
          </IonToolbar>
        </IonHeader>

        <div className="ion-padding ion-text-center" style={{ marginTop: '20%' }}>
            <p color="medium">No exams recorded yet.</p>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default History;
