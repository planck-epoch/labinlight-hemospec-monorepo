import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, IonCard, IonCardContent } from '@ionic/react';
import { arrowBackOutline, logOutOutline, personCircleOutline } from 'ionicons/icons';
import { useHistory } from 'react-router-dom';
import { authService } from '../services/AuthService';

const UserAccount: React.FC = () => {
  const history = useHistory();
  const user = authService.getUser();

  const handleLogout = async () => {
      await authService.logout();
      history.replace('/login');
  };

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
        <IonToolbar style={{ '--background': '#fff' }}>
          <IonButtons slot="start">
            <IonButton onClick={() => history.goBack()}>
              <IonIcon icon={arrowBackOutline} color="dark" />
            </IonButton>
          </IonButtons>
          <IonTitle style={{ color: 'black', fontWeight: 'bold' }}>User account</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent fullscreen style={{ '--background': '#fff' }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px 20px' }}>

            <div style={{
                width: '80px',
                height: '80px',
                borderRadius: '50%',
                backgroundColor: '#FF3D00',
                color: 'white',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                fontSize: '32px',
                fontWeight: 'bold',
                marginBottom: '10px'
            }}>
                {user ? getInitials(user.name) : <IonIcon icon={personCircleOutline} />}
            </div>

            <h2 style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '5px', color: 'black' }}>
                {user?.name || 'Unknown User'}
            </h2>

            <div style={{
                backgroundColor: '#E0F7FA',
                color: '#006064',
                padding: '5px 15px',
                borderRadius: '15px',
                fontSize: '0.8rem',
                marginBottom: '40px',
                display: 'flex',
                alignItems: 'center'
            }}>
                {/* Simple doctor icon simulation */}
                <span style={{ marginRight: '5px' }}>🩺</span>
                {user?.role === 'doctor' ? 'Doctor' : user?.role}
            </div>

            <div style={{ display: 'flex', width: '100%', justifyContent: 'space-between', marginBottom: '40px' }}>
                <div style={{
                    border: '1px solid #ddd',
                    borderRadius: '10px',
                    padding: '15px',
                    width: '48%'
                }}>
                    <div style={{ fontSize: '0.8rem', color: '#666', marginBottom: '5px' }}>Logged in at</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'black' }}>8:31</div>
                </div>

                <div style={{
                    border: '1px solid #ddd',
                    borderRadius: '10px',
                    padding: '15px',
                    width: '48%'
                }}>
                    <div style={{ fontSize: '0.8rem', color: '#666', marginBottom: '5px' }}>Time logged in</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'black' }}>45min</div>
                </div>
            </div>

            <IonButton fill="outline" shape="round" color="medium" onClick={handleLogout} style={{ width: '150px' }}>
                <IonIcon icon={logOutOutline} slot="start" />
                Logout
            </IonButton>

        </div>
      </IonContent>
    </IonPage>
  );
};

export default UserAccount;
