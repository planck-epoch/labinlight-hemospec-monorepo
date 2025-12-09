import { IonContent, IonPage, IonInput, IonButton, IonItem, IonIcon, IonText, IonAlert } from '@ionic/react';
import { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { authService } from '../services/AuthService';
import { mailOutline, lockClosedOutline, eyeOutline, eyeOffOutline, globeOutline, arrowForwardOutline } from 'ionicons/icons';

const Login: React.FC = () => {
  const history = useHistory();
  const [viewState, setViewState] = useState<'landing' | 'login'>('landing');

  // Login State
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Alert State
  const [showAlert, setShowAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState('');

  const handleNotImplemented = (feature: string) => {
      setAlertMessage(`${feature} is not implemented yet. Coming in the future.`);
      setShowAlert(true);
  };

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

  const renderLanding = () => (
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%', justifyContent: 'space-between', padding: '40px 20px' }}>
          <div style={{ marginTop: 'auto', marginBottom: 'auto', textAlign: 'center' }}>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '10px' }}>
                <span style={{ color: 'black' }}>Hemo</span>
                <span style={{ color: '#00B8D4' }}>spec</span>
            </h1>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'black' }}>Manage your health.</h2>
          </div>

          <div style={{ width: '100%', marginBottom: '50px' }}>
              <IonButton expand="block" fill="outline" shape="round" color="dark" onClick={() => setViewState('login')} style={{ marginBottom: '15px', height: '50px' }}>
                  Login
              </IonButton>
              <IonButton expand="block" shape="round" color="secondary" onClick={() => handleNotImplemented('Quick start')} style={{ height: '50px', '--background': '#00838F' }}>
                  Quick start
              </IonButton>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', color: '#333' }}>
                  <IonIcon icon={globeOutline} style={{ marginRight: '5px' }} />
                  <span>English</span>
              </div>
              <div>
                  <img src="/assets/logo_asset.png" alt="labinlight" style={{ height: '20px' }} />
              </div>
          </div>
      </div>
  );

  const renderLogin = () => (
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '40px 20px' }}>
          <div style={{ textAlign: 'center', marginBottom: '40px', marginTop: '40px' }}>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '30px' }}>
                <span style={{ color: 'black' }}>Hemo</span>
                <span style={{ color: '#00B8D4' }}>spec</span>
            </h1>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'black' }}>Login</h2>
          </div>

          <div style={{ marginBottom: '20px' }}>
              <div style={{ color: '#666', marginBottom: '5px', fontSize: '0.9rem' }}>Email</div>
              <IonItem lines="none" style={{ border: '1px solid #ccc', borderRadius: '5px', '--padding-start': '10px' }}>
                  <IonIcon icon={mailOutline} slot="start" color="medium" />
                  <IonInput
                    value={email}
                    onIonChange={e => setEmail(e.detail.value!)}
                    type="email"
                    placeholder="user@mail.example"
                  />
              </IonItem>
          </div>

          <div style={{ marginBottom: '30px' }}>
              <div style={{ color: '#666', marginBottom: '5px', fontSize: '0.9rem' }}>Password</div>
              <IonItem lines="none" style={{ border: '1px solid #ccc', borderRadius: '5px', '--padding-start': '10px' }}>
                  <IonIcon icon={lockClosedOutline} slot="start" color="medium" />
                  <IonInput
                    value={password}
                    onIonChange={e => setPassword(e.detail.value!)}
                    type={showPassword ? 'text' : 'password'}
                    placeholder="password"
                  />
                  <IonIcon
                    icon={showPassword ? eyeOffOutline : eyeOutline}
                    slot="end"
                    onClick={() => setShowPassword(!showPassword)}
                    color="medium"
                    style={{ cursor: 'pointer' }}
                  />
              </IonItem>
          </div>

          {error && (
            <IonText color="danger" className="ion-text-center" style={{ marginBottom: '20px', display: 'block' }}>
                <p>{error}</p>
            </IonText>
          )}

          <IonButton expand="block" shape="round" color="secondary" onClick={handleLogin} style={{ height: '50px', '--background': '#00838F', marginBottom: '30px', width: '150px', margin: '0 auto 30px auto' }}>
              {loading ? 'Logging in...' : 'Login'}
          </IonButton>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', marginBottom: '40px' }}>
              <div onClick={() => handleNotImplemented('Reset password')} style={{ color: '#00838F', fontWeight: 'bold', cursor: 'pointer' }}>
                  Reset password
              </div>
              <div onClick={() => handleNotImplemented('Quick start')} style={{ color: '#00838F', fontWeight: 'bold', cursor: 'pointer' }}>
                  Quick start
              </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', color: '#333' }}>
                  <IonIcon icon={globeOutline} style={{ marginRight: '5px' }} />
                  <span>English</span>
              </div>
              <div>
                   <img src="/assets/logo_asset.png" alt="labinlight" style={{ height: '20px' }} />
              </div>
          </div>
      </div>
  );

  return (
    <IonPage>
      <IonContent fullscreen className="ion-padding" style={{ '--background': '#F0F4F5' }}>
        {viewState === 'landing' ? renderLanding() : renderLogin()}

        <IonAlert
          isOpen={showAlert}
          onDidDismiss={() => setShowAlert(false)}
          header={'Feature Unavailable'}
          message={alertMessage}
          buttons={['OK']}
        />
      </IonContent>
    </IonPage>
  );
};

export default Login;
