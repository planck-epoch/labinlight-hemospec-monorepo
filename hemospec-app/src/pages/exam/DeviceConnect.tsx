import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButton, IonIcon, IonText, IonSpinner, IonButtons, IonBackButton } from '@ionic/react';
import { bluetoothOutline, checkmarkCircleOutline, alertCircleOutline, flaskOutline } from 'ionicons/icons';
import { useState, useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { deviceService, DeviceStatus } from '../../services/DeviceService';
import { apiService } from '../../services/ApiService';
import { ANALYSIS_PAYLOAD_TEMPLATE } from '../../services/payloadTemplate';

const DeviceConnect: React.FC = () => {
  const history = useHistory();
  const location = useLocation<{ patientId?: string; age?: string }>();
  const [status, setStatus] = useState<DeviceStatus>({ connected: false, batteryLevel: 0, isScanning: false, cartridgeInserted: false });
  const [error, setError] = useState('');
  const [isSimulating, setIsSimulating] = useState(false);

  useEffect(() => {
    const unsub = deviceService.subscribe(setStatus);
    return unsub;
  }, []);

  const handleScan = async () => {
    setError('');
    try {
        await deviceService.scanAndConnect();
    } catch (e) {
        setError('Could not find Hemospec device. Ensure it is turned on.');
    }
  };

  const handleCartridge = () => {
      deviceService.insertCartridge(true);
  };

  const startAnalysis = () => {
      history.push('/app/exam/analysis', location.state);
  };

  const handleSimulate = async () => {
      setIsSimulating(true);
      setError('');
      try {
          // Use template payload
          let result;
          try {
             // Attempt API call first as requested
             result = await apiService.analyze(ANALYSIS_PAYLOAD_TEMPLATE);
          } catch (e) {
             console.warn("API Simulation failed, falling back to dummy data", e);
             // Fallback to dummy data
             result = {
                  "Eritrocitos": 3.58,
                  "Hemoglobina": 10.8,
                  "Hematocrito": 32.8,
                  "RDW": 14.8,
                  "Creatinina": 1.5,
                  "PCR": 35.5
             };
          }

          history.push('/app/exam/results', { result });
      } catch (e) {
          setError('Simulation failed.');
          setIsSimulating(false);
      }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
            <IonButtons slot="start">
                <IonBackButton defaultHref="/app/exam/patient" />
            </IonButtons>
          <IonTitle>Connect Device</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding ion-text-center">

        <div style={{ marginTop: '20px', marginBottom: '40px' }}>
            <div style={{
                width: '120px', height: '120px', borderRadius: '50%',
                background: status.connected ? 'var(--ion-color-success-tint)' : '#eee',
                margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
                <IonIcon
                    icon={status.connected ? checkmarkCircleOutline : bluetoothOutline}
                    style={{ fontSize: '60px', color: status.connected ? 'var(--ion-color-success)' : '#999' }}
                />
            </div>
            <h2 style={{ marginTop: '20px' }}>
                {status.connected ? 'Device Connected' : 'Connect Hemospec'}
            </h2>
            <p>{status.connected ? `Battery: ${status.batteryLevel}%` : 'Turn on Bluetooth and the device.'}</p>
        </div>

        {error && (
            <div className="ion-margin-bottom" style={{ color: 'var(--ion-color-danger)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px' }}>
                <IonIcon icon={alertCircleOutline} />
                <span>{error}</span>
            </div>
        )}

        {!status.connected ? (
            <>
                <IonButton expand="block" onClick={handleScan} disabled={status.isScanning || isSimulating}>
                    {status.isScanning ? (
                        <>
                            <IonSpinner name="dots" style={{ marginRight: '10px' }} /> Scanning...
                        </>
                    ) : 'Scan for Device'}
                </IonButton>

                <div className="ion-margin-top">
                    <IonText color="medium"><p>or</p></IonText>
                    <IonButton expand="block" fill="outline" onClick={handleSimulate} disabled={status.isScanning || isSimulating}>
                         {isSimulating ? <IonSpinner name="dots" /> : (
                             <>
                                <IonIcon icon={flaskOutline} slot="start" />
                                Simulate Scan
                             </>
                         )}
                    </IonButton>
                </div>
            </>
        ) : (
            <div className="ion-text-left fade-in">
                <div style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '10px', marginBottom: '20px' }}>
                     <h4 style={{ margin: 0, marginBottom: '10px' }}>Pre-Check</h4>

                     <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                        <span>Bluetooth Connection</span>
                        <IonText color="success">OK</IonText>
                     </div>
                     <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                        <span>Battery Level</span>
                        <IonText color={status.batteryLevel > 20 ? "success" : "warning"}>{status.batteryLevel}%</IonText>
                     </div>
                     <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span>Cartridge Status</span>
                        {status.cartridgeInserted ? (
                             <IonText color="success">Ready</IonText>
                        ) : (
                            <IonButton size="small" fill="outline" onClick={handleCartridge}>Insert Mock Cartridge</IonButton>
                        )}
                     </div>
                </div>

                <IonButton expand="block" disabled={!status.cartridgeInserted} onClick={startAnalysis}>
                    Start Analysis
                </IonButton>
            </div>
        )}

      </IonContent>
    </IonPage>
  );
};

export default DeviceConnect;
