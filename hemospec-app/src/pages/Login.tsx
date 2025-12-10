import { IonContent, IonPage, IonInput, IonButton, IonItem, IonIcon, IonText, IonAlert, IonFooter, IonSpinner } from '@ionic/react';
import { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { authService } from '../services/AuthService';
import { mailOutline, lockClosedOutline, eyeOutline, eyeOffOutline, globeOutline } from 'ionicons/icons';

const Login: React.FC = () => {
  const history = useHistory();
  const [viewState, setViewState] = useState<'landing' | 'login'>('landing');

  // Login State
  // Hardcoded defaults as requested
  const [email, setEmail] = useState('daniel.sousa@labinlight.com');
  const [password, setPassword] = useState('secret');
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
    if (!email || !password) {
        setError('Please enter email and password');
        return;
    }
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

  const renderFooter = () => (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 20px 40px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', color: 'var(--ion-color-dark)' }}>
              <IonIcon icon={globeOutline} style={{ marginRight: '5px' }} />
              <span>English</span>
          </div>
          <div>
              <img src="/assets/logo_asset.png" alt="labinlight" style={{ height: '20px' }} />
          </div>
      </div>
  );

  const renderLanding = () => (
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '20px', textAlign: 'center' }}>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '10px' }}>
                <span style={{ color: 'var(--ion-color-dark)' }}>Hemo</span>
                <span style={{ color: 'var(--ion-color-primary)' }}>spec</span>
            </h1>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--ion-color-dark)' }}>Manage your health.</h2>
          </div>

          <div style={{ width: '100%', padding: '0 20px 50px 20px' }}>
              <IonButton expand="block" fill="outline" shape="round" color="dark" onClick={() => setViewState('login')} style={{ marginBottom: '15px', height: '50px' }}>
                  Login
              </IonButton>
              <IonButton expand="block" shape="round" color="primary" onClick={() => handleNotImplemented('Quick start')} style={{ height: '50px' }}>
                  Quick start
              </IonButton>
          </div>

          {renderFooter()}
      </div>
  );

  const renderLogin = () => (
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
          <div style={{ textAlign: 'center', marginBottom: '40px', marginTop: '60px', padding: '0 20px' }}>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '30px' }}>
                <span style={{ color: 'var(--ion-color-dark)' }}>Hemo</span>
                <span style={{ color: 'var(--ion-color-primary)' }}>spec</span>
            </h1>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--ion-color-dark)' }}>Login</h2>
          </div>

          <div style={{ padding: '0 20px' }}>
              <div style={{ marginBottom: '20px' }}>
                  <div style={{ color: 'var(--ion-color-medium)', marginBottom: '5px', fontSize: '0.9rem' }}>Email</div>
                  <IonItem lines="none" style={{ border: '1px solid var(--c-neutral-50)', borderRadius: '5px', '--padding-start': '10px', '--background': 'var(--c-neutral-100)' }}>
                      <IonIcon icon={mailOutline} slot="start" color="medium" />
                      <IonInput
                        value={email}
                        onIonChange={e => setEmail(e.detail.value!)}
                        type="email"
                        placeholder="user@mail.example"
                        style={{ '--background': 'transparent', '--color': 'var(--ion-color-dark)', '--placeholder-color': 'var(--ion-color-medium)' }}
                      />
                  </IonItem>
              </div>

              <div style={{ marginBottom: '30px' }}>
                  <div style={{ color: 'var(--ion-color-medium)', marginBottom: '5px', fontSize: '0.9rem' }}>Password</div>
                  <IonItem lines="none" style={{ border: '1px solid var(--c-neutral-50)', borderRadius: '5px', '--padding-start': '10px', '--background': 'var(--c-neutral-100)' }}>
                      <IonIcon icon={lockClosedOutline} slot="start" color="medium" />
                      <IonInput
                        value={password}
                        onIonChange={e => setPassword(e.detail.value!)}
                        type={showPassword ? 'text' : 'password'}
                        placeholder="password"
                        style={{ '--background': 'transparent', '--color': 'var(--ion-color-dark)', '--placeholder-color': 'var(--ion-color-medium)' }}
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

              <IonButton
                  expand="block"
                  shape="round"
                  color="primary"
                  onClick={handleLogin}
                  disabled={loading}
                  style={{ height: '50px', marginBottom: '30px', width: '150px', margin: '0 auto 30px auto', textTransform: 'none' }}
              >
                  {loading ? <IonSpinner name="crescent" color="light" /> : 'Login'}
              </IonButton>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <div onClick={() => handleNotImplemented('Reset password')} style={{ color: 'var(--ion-color-primary)', fontWeight: 'bold', cursor: 'pointer' }}>
                      Reset password
                  </div>
                  <div onClick={() => handleNotImplemented('Quick start')} style={{ color: 'var(--ion-color-primary)', fontWeight: 'bold', cursor: 'pointer' }}>
                      Quick start
                  </div>
              </div>
          </div>

          <div style={{ marginTop: 'auto' }}>
            {renderFooter()}
          </div>
      </div>
  );

  return (
    <IonPage>
      <IonContent fullscreen style={{ '--background': 'var(--ion-color-light)' }}>
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
