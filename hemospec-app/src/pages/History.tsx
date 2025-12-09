import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, IonList, IonItem, IonLabel, IonSearchbar, IonSpinner } from '@ionic/react';
import { useState, useEffect } from 'react';
import { apiService, HistoryItem } from '../services/ApiService';
import { authService } from '../services/AuthService';
import { refreshOutline } from 'ionicons/icons';

const History: React.FC = () => {
    const [historyData, setHistoryData] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [patientId, setPatientId] = useState('24058P'); // Default mock ID

    useEffect(() => {
        loadHistory();
    }, []);

    const loadHistory = async () => {
        setLoading(true);
        try {
            const token = authService.getToken();
            if (token) {
                const data = await apiService.getHistory(patientId, token);
                setHistoryData(Array.isArray(data) ? data : []);
            }
        } catch (error) {
            console.error('Failed to load history', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <IonPage>
             <IonHeader className="ion-no-border">
                <IonToolbar style={{ '--background': '#F0F4F5' }}>
                    <IonTitle style={{ color: 'black', fontWeight: 'bold' }}>History</IonTitle>
                    <IonButtons slot="end">
                        <IonButton onClick={loadHistory}>
                            <IonIcon icon={refreshOutline} color="dark" />
                        </IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonHeader>
            <IonContent fullscreen style={{ '--background': '#F0F4F5' }}>
                <div style={{ padding: '10px' }}>
                    <IonSearchbar
                        value={patientId}
                        onIonChange={e => setPatientId(e.detail.value!)}
                        placeholder="Search Patient ID"
                        onIonClear={() => setPatientId('')}
                    />
                </div>

                {loading ? (
                    <div className="ion-text-center" style={{ marginTop: '20px' }}>
                        <IonSpinner />
                    </div>
                ) : (
                    <IonList style={{ background: 'transparent' }}>
                        {historyData.length === 0 ? (
                            <div className="ion-text-center" style={{ padding: '20px', color: '#666' }}>
                                No history found for {patientId}
                            </div>
                        ) : (
                            historyData.map((item, index) => (
                                <IonItem key={index} lines="none" style={{ marginBottom: '10px', borderRadius: '10px', '--background': 'white', margin: '10px' }}>
                                    <IonLabel>
                                        <h2>Date: {new Date(item.created_at || Date.now()).toLocaleDateString()}</h2>
                                        <p>Patient: {item.patientId}</p>
                                        <p>Result: {item.result ? 'Completed' : 'Pending'}</p>
                                    </IonLabel>
                                </IonItem>
                            ))
                        )}
                    </IonList>
                )}
            </IonContent>
        </IonPage>
    );
};

export default History;
