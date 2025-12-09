import { IonContent, IonPage } from '@ionic/react';
import { useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';
import './Splash.css';

const Splash: React.FC = () => {
  const history = useHistory();
  const [verificationText, setVerificationText] = useState('Verifying parameter Bluetooth...');
  const [verificationState, setVerificationState] = useState<'verifying' | 'verified'>('verifying');
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const sequence = async () => {
        // Start
        setProgress(10);

        // Step 1: Bluetooth
        setVerificationText('Verifying parameter Bluetooth...');
        setVerificationState('verifying');
        await new Promise(resolve => setTimeout(resolve, 1000));
        setVerificationText('Bluetooth • Verified');
        setVerificationState('verified');
        setProgress(33);
        await new Promise(resolve => setTimeout(resolve, 500));

        // Step 2: Cloud
        setVerificationText('Verifying parameter Cloud...');
        setVerificationState('verifying');
        await new Promise(resolve => setTimeout(resolve, 1000));
        setVerificationText('Cloud • Verified');
        setVerificationState('verified');
        setProgress(66);
        await new Promise(resolve => setTimeout(resolve, 500));

        // Step 3: Sensors
        setVerificationText('Verifying parameter Sensors...');
        setVerificationState('verifying');
        await new Promise(resolve => setTimeout(resolve, 1000));
        setVerificationText('Sensors • Verified');
        setVerificationState('verified');
        setProgress(100);
        await new Promise(resolve => setTimeout(resolve, 500));

        // Done
        history.replace('/login');
    };
    sequence();
  }, [history]);

  return (
    <IonPage>
      <IonContent fullscreen style={{ '--background': 'black' }}>
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: 'black',
            color: 'white'
        }}>
            <h1 style={{
                fontSize: '2rem',
                fontWeight: 'bold',
                marginBottom: '5px'
            }}>
                <span style={{ color: 'white' }}>Hemo</span>
                <span style={{ color: '#00B8D4' }}>spec</span>
            </h1>

            <div style={{
                marginTop: '10px',
                width: '80%',
                height: '4px', // Thicker bar
                backgroundColor: '#333',
                position: 'relative',
                borderRadius: '2px'
            }}>
                <div style={{
                    width: `${progress}%`,
                    height: '100%',
                    backgroundColor: '#00B8D4', // Brighter cyan
                    transition: 'width 0.5s ease-in-out',
                    borderRadius: '2px'
                }}></div>
            </div>

            <p style={{
                marginTop: '15px',
                color: verificationState === 'verified' ? '#00B8D4' : '#888',
                fontSize: '0.9rem'
            }}>
                {verificationText}
            </p>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Splash;
