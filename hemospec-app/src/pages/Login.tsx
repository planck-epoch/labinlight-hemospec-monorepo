import { IonContent, IonPage, IonInput, IonButton, IonItem, IonLabel, IonText, IonIcon } from '@ionic/react';
import { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { authService } from '../services/AuthService';
import { lockClosedOutline, personOutline } from 'ionicons/icons';

const Login: React.FC = () => {
  const history = useHistory();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async () => {
    setLoading(true);
    setError('');
    try {
      await authService.login(email, password);
      history.replace('/app/home');
    } catch (e) {
      setError('Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <IonPage>
      <IonContent className="ion-padding" fullscreen>
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%', justifyContent: 'center' }}>

            <div className="ion-text-center" style={{ marginBottom: '40px' }}>
                <h1 style={{ color: 'var(--ion-color-primary)', fontWeight: 'bold' }}>Welcome Back</h1>
                <p style={{ color: 'var(--ion-color-medium)' }}>Sign in to access your dashboard</p>
            </div>

            <IonItem className="ion-margin-bottom" lines="inset">
                <IonIcon icon={personOutline} slot="start" color="medium" />
                <IonLabel position="floating">Email / User ID</IonLabel>
                <IonInput value={email} onIonChange={e => setEmail(e.detail.value!)} type="email" />
            </IonItem>

            <IonItem className="ion-margin-bottom" lines="inset">
                <IonIcon icon={lockClosedOutline} slot="start" color="medium" />
                <IonLabel position="floating">Password</IonLabel>
                <IonInput value={password} onIonChange={e => setPassword(e.detail.value!)} type="password" />
            </IonItem>

            {error && (
                <IonText color="danger" className="ion-text-center">
                    <p>{error}</p>
                </IonText>
            )}

            <div className="ion-margin-top">
                <IonButton expand="block" onClick={handleLogin} disabled={loading} shape="round">
                    {loading ? 'Signing In...' : 'Sign In'}
                </IonButton>
            </div>

            <div className="ion-text-center ion-margin-top">
                <IonButton fill="clear" size="small" color="medium">
                    Forgot Password?
                </IonButton>
            </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Login;
