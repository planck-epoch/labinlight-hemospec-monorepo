import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonProgressBar, IonText } from '@ionic/react';
import { useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { deviceService } from '../../services/DeviceService';

const Analysis: React.FC = () => {
  const history = useHistory();
  const [progress, setProgress] = useState(0);
  const [statusText, setStatusText] = useState('Initializing...');

  useEffect(() => {
    const run = async () => {
        // Start simulated progress bar
        const interval = setInterval(() => {
            setProgress(prev => {
                if (prev >= 1) {
                    clearInterval(interval);
                    return 1;
                }
                return prev + 0.01;
            });
        }, 50); // 5000ms total roughly

        setTimeout(() => setStatusText('Analyzing Sample...'), 1500);
        setTimeout(() => setStatusText('Calibrating Results...'), 3500);

        try {
            const result = await deviceService.runAnalysis();
            // Pass results via state or context (for simplicity using location state here or just re-fetching/mocking)
            // In a real app we'd use a Context or Redux store.
            // For now, let's just navigate.

            history.replace({
                pathname: '/app/exam/results',
                state: { result }
            });
        } catch (e) {
            alert('Analysis Failed: ' + e);
            history.goBack();
        } finally {
            clearInterval(interval);
        }
    };

    run();
  }, [history]);

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Analysis in Progress</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding ion-text-center">

        <div style={{ marginTop: '30vh' }}>
            <h2 style={{ color: 'var(--ion-color-primary)' }}>{Math.round(progress * 100)}%</h2>
            <IonProgressBar value={progress} color="primary" style={{ height: '10px', borderRadius: '5px' }} />

            <p style={{ marginTop: '20px', fontSize: '1.2rem', color: '#666' }}>
                {statusText}
            </p>

            <p style={{ marginTop: '50px', fontSize: '0.9rem', color: '#999' }}>
                Do not remove the cartridge or turn off the device.
            </p>
        </div>

      </IonContent>
    </IonPage>
  );
};

export default Analysis;
