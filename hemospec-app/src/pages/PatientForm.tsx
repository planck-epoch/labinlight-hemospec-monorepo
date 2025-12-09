import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, IonInput, IonItem, IonLabel, IonSelect, IonSelectOption } from '@ionic/react';
import { arrowBackOutline } from 'ionicons/icons';
import { useState } from 'react';
import { useHistory } from 'react-router-dom';

const PatientForm: React.FC = () => {
    const history = useHistory();
    const [patientId, setPatientId] = useState('');
    const [age, setAge] = useState('');
    const [sex, setSex] = useState<string>('Male');

    const handleContinue = () => {
        if (patientId && age && sex) {
            history.push({
                pathname: '/app/exam/analysis',
                state: { patientId, age: parseInt(age), sex }
            });
        }
    };

    return (
        <IonPage>
            <IonHeader className="ion-no-border">
                <IonToolbar>
                    <IonButtons slot="start">
                        <IonButton onClick={() => history.goBack()}>
                            <IonIcon icon={arrowBackOutline} color="dark" />
                        </IonButton>
                    </IonButtons>
                    <IonTitle>Patient Information</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                <div style={{ marginTop: '20px' }}>
                    <IonItem lines="none" style={{ border: '1px solid #ccc', borderRadius: '5px', marginBottom: '20px' }}>
                        <IonLabel position="stacked">Patient ID</IonLabel>
                        <IonInput value={patientId} onIonChange={e => setPatientId(e.detail.value!)} placeholder="12345" />
                    </IonItem>

                    <IonItem lines="none" style={{ border: '1px solid #ccc', borderRadius: '5px', marginBottom: '20px' }}>
                        <IonLabel position="stacked">Age</IonLabel>
                        <IonInput type="number" value={age} onIonChange={e => setAge(e.detail.value!)} placeholder="30" />
                    </IonItem>

                    <IonItem lines="none" style={{ border: '1px solid #ccc', borderRadius: '5px', marginBottom: '40px' }}>
                        <IonLabel position="stacked">Sex</IonLabel>
                        <IonSelect value={sex} onIonChange={e => setSex(e.detail.value)}>
                            <IonSelectOption value="Male">Male</IonSelectOption>
                            <IonSelectOption value="Female">Female</IonSelectOption>
                        </IonSelect>
                    </IonItem>

                    <IonButton expand="block" shape="round" color="secondary" onClick={handleContinue} style={{ '--background': '#00838F' }}>
                        Continue
                    </IonButton>
                </div>
            </IonContent>
        </IonPage>
    );
};

export default PatientForm;
