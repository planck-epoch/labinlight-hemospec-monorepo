import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonList, IonItem, IonLabel, IonNote, IonButton, IonIcon, IonButtons, IonBackButton } from '@ionic/react';
import { useLocation, useHistory } from 'react-router-dom';
import { shareOutline, downloadOutline, homeOutline } from 'ionicons/icons';
import { DeviceResult } from '../../services/DeviceService';

interface ResultState {
    result: DeviceResult;
}

const Results: React.FC = () => {
  const location = useLocation<ResultState>();
  const history = useHistory();
  const result = location.state?.result;

  if (!result) {
      history.replace('/app/home');
      return null;
  }

  const parameters = [
      { name: 'Hematocrit', value: result.hematocrit + ' %', range: '37 - 47 %' },
      { name: 'Hemoglobin', value: result.hemoglobin + ' g/dL', range: '12 - 16 g/dL' },
      { name: 'RBC', value: result.rbc + ' M/µL', range: '4.2 - 5.4 M/µL' },
      { name: 'RDW', value: result.rdw + ' %', range: '11.5 - 14.5 %' },
  ];

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Results</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">

        <div className="ion-text-center" style={{ marginBottom: '20px' }}>
            <h1 style={{ color: 'var(--ion-color-success)', fontWeight: 'bold' }}>Analysis Complete</h1>
            <p color="medium">{new Date(result.timestamp).toLocaleString()}</p>
        </div>

        <IonCard>
            <IonCardHeader>
                <IonCardTitle>Summary</IonCardTitle>
            </IonCardHeader>
            <IonCardContent className="ion-no-padding">
                <IonList lines="full">
                    {parameters.map((param, index) => (
                        <IonItem key={index}>
                            <IonLabel>
                                <h2>{param.name}</h2>
                                <p>Ref: {param.range}</p>
                            </IonLabel>
                            <IonNote slot="end" color="dark" style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>
                                {param.value}
                            </IonNote>
                        </IonItem>
                    ))}
                </IonList>
            </IonCardContent>
        </IonCard>

        <div className="ion-margin-top" style={{ display: 'flex', gap: '10px' }}>
            <IonButton expand="block" fill="outline" style={{ flex: 1 }}>
                <IonIcon icon={shareOutline} slot="start" /> Share
            </IonButton>
            <IonButton expand="block" fill="outline" style={{ flex: 1 }}>
                <IonIcon icon={downloadOutline} slot="start" /> PDF
            </IonButton>
        </div>

        <div className="ion-margin-top">
            <IonButton expand="block" onClick={() => history.replace('/app/home')}>
                <IonIcon icon={homeOutline} slot="start" /> Back to Home
            </IonButton>
        </div>

      </IonContent>
    </IonPage>
  );
};

export default Results;
