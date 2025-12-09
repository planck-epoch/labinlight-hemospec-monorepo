import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonCard, IonCardHeader, IonCardSubtitle, IonCardTitle, IonCardContent, IonGrid, IonRow, IonCol, IonButton, IonIcon } from '@ionic/react';
import { bluetoothOutline, statsChartOutline, documentTextOutline } from 'ionicons/icons';
import { authService } from '../services/AuthService';

const Home: React.FC = () => {
    const user = authService.getUser();

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Dashboard</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen className="ion-padding">
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Hello, {user?.name}</IonTitle>
          </IonToolbar>
        </IonHeader>

        <IonGrid>
            <IonRow>
                <IonCol size="12">
                    <IonCard color="primary" routerLink="/app/exam/patient">
                        <IonCardHeader>
                            <IonCardSubtitle style={{ color: 'rgba(255,255,255,0.8)' }}>Action</IonCardSubtitle>
                            <IonCardTitle>Start New Exam</IonCardTitle>
                        </IonCardHeader>
                        <IonCardContent>
                            Connect device and perform a new analysis.
                        </IonCardContent>
                    </IonCard>
                </IonCol>
            </IonRow>

            <IonRow>
                <IonCol size="6">
                     <IonCard routerLink="/app/history">
                        <IonCardContent className="ion-text-center">
                            <IonIcon icon={documentTextOutline} size="large" color="secondary" />
                            <h2 style={{ marginTop: '10px' }}>History</h2>
                            <p style={{ fontSize: '0.8rem', color: '#888' }}>View past results</p>
                        </IonCardContent>
                    </IonCard>
                </IonCol>
                <IonCol size="6">
                     <IonCard>
                        <IonCardContent className="ion-text-center">
                            <IonIcon icon={bluetoothOutline} size="large" color="medium" />
                            <h2 style={{ marginTop: '10px' }}>Device</h2>
                            <p style={{ fontSize: '0.8rem', color: '#888' }}>Status Check</p>
                        </IonCardContent>
                    </IonCard>
                </IonCol>
            </IonRow>

             <IonRow>
                <IonCol size="12">
                    <h3 style={{ paddingLeft: '10px' }}>Recent Activity</h3>
                    <IonCard>
                        <IonCardContent>
                            <p>No recent exams performed today.</p>
                        </IonCardContent>
                    </IonCard>
                </IonCol>
            </IonRow>
        </IonGrid>
      </IonContent>
    </IonPage>
  );
};

export default Home;
