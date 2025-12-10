import React, { useState, useEffect } from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonProgressBar, IonText, IonButton, IonIcon } from '@ionic/react';
import { useHistory } from 'react-router-dom';
import { bluetooth } from 'ionicons/icons';
import { deviceService } from '../../services/DeviceService';
import { apiService } from '../../services/ApiService';
import './Exam.css';

const Analysis: React.FC = () => {
  const history = useHistory();
  const [status, setStatus] = useState('Initializing...');
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    runAnalysis();
  }, []);

  const runAnalysis = async () => {
    try {
      setStatus('Connecting to device...');
      setProgress(0.1);

      // Ensure connected (might be redundant if already connected, but safe)
      await deviceService.scanAndConnect();

      setStatus('Scanning sample...');
      setProgress(0.3);

      // Get raw scan data
      const scanResult = await deviceService.scan();

      setStatus('Processing data...');
      setProgress(0.6);

      // Format the data using the template to match backend DTO
      const payload = deviceService.formatScanData(scanResult);

      setStatus('Analyzing Sample...');

      // Send to API
      const response = await apiService.analyze(payload);

      setStatus('Analysis complete');
      setProgress(1.0);

      // Navigate to results
      history.push('/exam/results', { result: response });

    } catch (err: any) {
      console.error(err);
      setError(err.message || 'Analysis failed');
      setStatus('Error occurred');
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Analysis</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        <div className="analysis-container">
          <IonIcon icon={bluetooth} size="large" className="bluetooth-icon" />

          <IonText>
            <h2>{status}</h2>
          </IonText>

          <IonProgressBar value={progress} buffer={progress + 0.1}></IonProgressBar>

          {error && (
            <div className="error-message">
              <IonText color="danger">
                <p>{error}</p>
              </IonText>
              <IonButton expand="block" onClick={() => history.push('/exam/device')}>
                Try Again
              </IonButton>
            </div>
          )}
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Analysis;