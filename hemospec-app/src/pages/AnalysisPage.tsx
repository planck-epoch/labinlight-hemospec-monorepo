import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, IonSpinner, IonCard, IonCardContent } from '@ionic/react';
import { arrowBackOutline, checkmarkCircleOutline } from 'ionicons/icons';
import { useState, useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { apiService } from '../services/ApiService';
import { createMockAnalyzeData } from '../utils/MockDataFactory';

const AnalysisPage: React.FC = () => {
    const history = useHistory();
    const location = useLocation<{ patientId: string; age: number; sex: string }>();
    const { patientId, age, sex } = location.state || { patientId: '000', age: 30, sex: 'Male' };

    const [state, setState] = useState<'scanning' | 'analyzing' | 'complete' | 'error'>('scanning');
    const [progress, setProgress] = useState(0);
    const [result, setResult] = useState<any>(null);

    useEffect(() => {
        startScan();
    }, []);

    const startScan = async () => {
        // Simulate scanning process (0-100%)
        for (let i = 0; i <= 100; i += 10) {
            setProgress(i);
            await new Promise(r => setTimeout(r, 200));
        }

        setState('analyzing');
        submitAnalysis();
    };

    const submitAnalysis = async () => {
        try {
            const mockData = createMockAnalyzeData(patientId, age, sex);
            const response = await apiService.analyze(mockData);
            setResult(response);
            setState('complete');
        } catch (error) {
            console.error(error);
            setState('error');
        }
    };

    const renderContent = () => {
        if (state === 'scanning') {
            return (
                <div className="ion-text-center" style={{ marginTop: '50px' }}>
                    <h2>Scanning...</h2>
                    <h1 style={{ fontSize: '3rem', fontWeight: 'bold' }}>{progress}%</h1>
                    <div style={{ width: '80%', margin: '20px auto', height: '10px', background: '#eee', borderRadius: '5px' }}>
                        <div style={{ width: `${progress}%`, height: '100%', background: '#00B8D4', transition: 'width 0.2s', borderRadius: '5px' }} />
                    </div>
                    <p>Please keep the device steady.</p>
                </div>
            );
        }

        if (state === 'analyzing') {
             return (
                <div className="ion-text-center" style={{ marginTop: '50px' }}>
                    <IonSpinner name="bubbles" style={{ transform: 'scale(1.5)' }} />
                    <h2 style={{ marginTop: '20px' }}>Analyzing data...</h2>
                    <p>Sending to cloud...</p>
                </div>
            );
        }

        if (state === 'error') {
            return (
                 <div className="ion-text-center" style={{ marginTop: '50px' }}>
                    <h2 style={{ color: 'red' }}>Analysis Failed</h2>
                    <IonButton onClick={() => history.replace('/app/home')}>Back to Home</IonButton>
                </div>
            );
        }

        if (state === 'complete') {
            return (
                <div className="ion-padding">
                    <div className="ion-text-center">
                        <IonIcon icon={checkmarkCircleOutline} color="success" style={{ fontSize: '64px' }} />
                        <h2>Analysis Complete</h2>
                    </div>

                    <IonCard>
                        <IonCardContent>
                            <h3>Patient ID: {patientId}</h3>
                            <p>Age: {age} | Sex: {sex}</p>
                            <hr />
                            <h2>Results</h2>
                            {/* Display raw result for MVP or parsed if we knew structure */}
                            <pre style={{ overflow: 'auto', maxHeight: '300px' }}>
                                {JSON.stringify(result, null, 2)}
                            </pre>
                        </IonCardContent>
                    </IonCard>

                    <IonButton expand="block" onClick={() => history.replace('/app/home')} style={{ marginTop: '20px' }}>
                        Done
                    </IonButton>
                </div>
            );
        }
    };

    return (
        <IonPage>
            <IonHeader className="ion-no-border">
                <IonToolbar>
                    {state === 'complete' || state === 'error' ? (
                         <IonButtons slot="start">
                            <IonButton onClick={() => history.replace('/app/home')}>
                                <IonIcon icon={arrowBackOutline} color="dark" />
                            </IonButton>
                        </IonButtons>
                    ) : null}
                    <IonTitle>Analysis</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                {renderContent()}
            </IonContent>
        </IonPage>
    );
};

export default AnalysisPage;
