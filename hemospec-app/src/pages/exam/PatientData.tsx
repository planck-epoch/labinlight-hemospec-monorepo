import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonInput, IonItem, IonLabel, IonButton, IonSelect, IonSelectOption, IonButtons, IonBackButton } from '@ionic/react';
import { useState } from 'react';
import { useHistory } from 'react-router-dom';

const PatientData: React.FC = () => {
  const history = useHistory();
  const [patientId, setPatientId] = useState('');
  const [age, setAge] = useState('');

  const isValid = patientId.length > 0 && age.length > 0;

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
           <IonButtons slot="start">
                <IonBackButton defaultHref="/app/home" />
            </IonButtons>
          <IonTitle>New Exam</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        <h2>Patient Data</h2>
        <p className="ion-margin-bottom" style={{ color: '#888' }}>Enter the patient details to proceed.</p>

        <IonItem className="ion-margin-bottom">
            <IonLabel position="stacked">Patient ID *</IonLabel>
            <IonInput value={patientId} onIonChange={e => setPatientId(e.detail.value!)} placeholder="e.g. 12345" />
        </IonItem>

        <IonItem className="ion-margin-bottom">
            <IonLabel position="stacked">Age *</IonLabel>
            <IonInput value={age} onIonChange={e => setAge(e.detail.value!)} type="number" placeholder="Years" />
        </IonItem>

        <IonItem className="ion-margin-bottom">
            <IonLabel position="stacked">Sex</IonLabel>
            <IonSelect placeholder="Select One">
                <IonSelectOption value="m">Male</IonSelectOption>
                <IonSelectOption value="f">Female</IonSelectOption>
                <IonSelectOption value="o">Other</IonSelectOption>
            </IonSelect>
        </IonItem>

        <div className="ion-margin-top">
            <IonButton expand="block" disabled={!isValid} onClick={() => history.push('/app/exam/connect', { patientId, age })}>
                Continue
            </IonButton>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default PatientData;
