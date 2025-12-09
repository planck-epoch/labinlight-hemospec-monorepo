import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon } from '@ionic/react';
import { optionsOutline } from 'ionicons/icons';
import { authService } from '../services/AuthService';
import { useHistory } from 'react-router-dom';

const Home: React.FC = () => {
    const history = useHistory();
    const user = authService.getUser();

    // Initials for avatar
    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map(part => part[0])
            .join('')
            .toUpperCase()
            .substring(0, 2);
    };

  return (
    <IonPage>
      <IonHeader className="ion-no-border">
        <IonToolbar style={{ '--background': '#F0F4F5' }}>
            <IonButtons slot="start">
                <IonButton>
                    <IonIcon icon={optionsOutline} slot="icon-only" color="dark" />
                </IonButton>
            </IonButtons>

            <IonTitle className="ion-text-center">
                <span style={{ fontWeight: 'bold', color: 'black' }}>Hemo</span>
                <span style={{ fontWeight: 'bold', color: '#00B8D4' }}>spec</span>
            </IonTitle>

            <IonButtons slot="end">
                <div
                    onClick={() => history.push('/app/user-account')}
                    style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '50%',
                        backgroundColor: '#FF3D00', // Red color from mock
                        color: 'white',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        marginRight: '10px',
                        cursor: 'pointer'
                    }}
                >
                    {user ? getInitials(user.name) : 'U'}
                </div>
            </IonButtons>
        </IonToolbar>
      </IonHeader>

      <IonContent fullscreen style={{ '--background': '#F0F4F5' }}>
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: '#F0F4F5'
        }}>

            <div style={{ marginBottom: '50px' }}>
                <img src="/assets/device_image.png" alt="Hemospec Device" style={{ maxWidth: '80%', height: 'auto' }} />
            </div>

            <div
                onClick={() => history.push('/app/exam/patient')}
                style={{
                    backgroundColor: '#00838F',
                    color: 'white',
                    borderRadius: '25px',
                    padding: '10px 25px',
                    display: 'flex',
                    alignItems: 'center',
                    cursor: 'pointer',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                }}
            >
                {/* Custom icon or ionic icon? Using standard for now unless image is preferred */}
                {/* <IonIcon icon={addCircleOutline} style={{ marginRight: '8px' }} /> */}
                <img src="/assets/icon_action_button_new_test.png" style={{ height: '20px', marginRight: '10px' }} alt="" />
                <span style={{ fontWeight: 'bold' }}>New test</span>
            </div>

        </div>
      </IonContent>
    </IonPage>
  );
};

export default Home;
