import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonSpinner, IonImg } from '@ionic/react';
import { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import './Splash.css';

const Splash: React.FC = () => {
  const history = useHistory();

  useEffect(() => {
    const init = async () => {
      // Simulate system checks (Cloud, Language, etc.)
      await new Promise(resolve => setTimeout(resolve, 3000));
      history.replace('/login');
    };
    init();
  }, [history]);

  return (
    <IonPage>
      <IonContent className="ion-padding ion-text-center" fullscreen>
        <div className="splash-container">
            {/* Placeholder for Logo - In real app use IonImg src="assets/logo.png" */}
            <h1 style={{ color: 'var(--ion-color-primary)', fontWeight: 'bold', fontSize: '2.5rem', marginTop: '30vh' }}>HEMOSPEC</h1>
            <p style={{ color: 'var(--ion-color-medium)' }}>Advanced Hematology Analysis</p>

            <div style={{ marginTop: '50px' }}>
                <IonSpinner name="crescent" color="primary" />
                <p style={{ fontSize: '0.9rem', color: '#666' }}>Checking system status...</p>
            </div>

            <div style={{ position: 'absolute', bottom: '30px', width: '100%', left: 0 }}>
                <p style={{ fontSize: '0.8rem', color: '#aaa' }}>Powered by labinlight</p>
            </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Splash;
