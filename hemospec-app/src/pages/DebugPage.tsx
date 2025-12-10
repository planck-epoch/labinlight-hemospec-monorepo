import React, { useState, useEffect } from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, IonCard, IonCardContent, IonList, IonItem, IonLabel, IonTextarea } from '@ionic/react';
import { arrowBackOutline } from 'ionicons/icons';
import { useHistory } from 'react-router-dom';
import { deviceService, DeviceStatus } from '../services/DeviceService';

const DebugPage: React.FC = () => {
    const history = useHistory();
    const [logs, setLogs] = useState<string[]>([]);
    const [status, setStatus] = useState<DeviceStatus>({
        connected: false,
        batteryLevel: 0,
        isScanning: false,
        cartridgeInserted: false,
        temperature: 0,
        serialNumber: ''
    });

    useEffect(() => {
        const unsub = deviceService.subscribe((s) => setStatus(s));
        return () => { unsub(); };
    }, []);

    const log = (msg: string | object) => {
        const text = typeof msg === 'string' ? msg : JSON.stringify(msg, null, 2);
        setLogs(prev => [text, ...prev]);
    };

    const handleConnect = async () => {
        log("Starting scan & connect...");
        try {
            await deviceService.scanAndConnect();
            log("Connected!");
        } catch (e: any) {
            log("Connect Error: " + e.message);
        }
    };

    const handleDisconnect = async () => {
        try {
            await deviceService.disconnect();
            log("Disconnected.");
        } catch (e: any) {
            log("Disconnect Error: " + e.message);
        }
    };

    const handleTestScan = async () => {
        log("Starting Spectral Scan...");
        try {
            const data = await deviceService.performSpectralScan();
            log("Scan Complete!");
            log(data); // Will dump the huge object
        } catch (e: any) {
            log("Scan Error: " + e.message);
        }
    };

    const handleGetPayload = async () => {
         // Create dummy payload with random data to verify structure
         const dummyScan = {
             intensities: Array(228).fill(100000),
             wavelengths: Array(228).fill(900),
             ScanConfig_serial_number: "TEST-123",
             DetectorTemp: 30,
             Humidity: 50,
             SystemTemp: 25,
             LampPD: 3000
         };
         const payload = deviceService.constructPayload(dummyScan, { patientId: "TEST", age: 99, gender: 'M' });
         log("Constructed Payload:");
         log(payload);
    };

    const handleCheckPermissions = async () => {
        log("Checking permissions...");
        try {
            const result = await deviceService.checkPermissionsDebug();
            log("Permission Check Result:");
            log(result);
        } catch (e: any) {
            log("Permission Check Failed: " + e.message);
        }
    };

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    <IonButtons slot="start">
                        <IonButton onClick={() => history.goBack()}>
                            <IonIcon icon={arrowBackOutline} />
                        </IonButton>
                    </IonButtons>
                    <IonTitle>Device Debugger</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                <IonCard>
                    <IonCardContent>
                        <p><strong>Status:</strong> {status.connected ? 'Connected' : 'Disconnected'}</p>
                        <p><strong>Battery:</strong> {status.batteryLevel}%</p>
                        <p><strong>Temp:</strong> {status.temperature}°C</p>
                        <p><strong>Serial:</strong> {status.serialNumber}</p>
                        <p><strong>Scanning:</strong> {status.isScanning ? 'Yes' : 'No'}</p>
                    </IonCardContent>
                </IonCard>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    <IonButton expand="block" onClick={handleConnect} disabled={status.connected || status.isScanning}>
                        {status.isScanning ? 'Scanning...' : 'Connect (Auto Scan)'}
                    </IonButton>

                    <IonButton expand="block" color="danger" onClick={handleDisconnect} disabled={!status.connected}>
                        Disconnect
                    </IonButton>

                    <IonButton expand="block" color="tertiary" onClick={handleTestScan} disabled={!status.connected}>
                        Test Spectral Scan
                    </IonButton>

                     <IonButton expand="block" color="light" onClick={handleGetPayload}>
                        Test Payload Construction
                    </IonButton>

                    <IonButton expand="block" color="warning" onClick={handleCheckPermissions}>
                        Check Permissions
                    </IonButton>
                </div>

                <h3>Logs</h3>
                <div style={{ background: '#333', color: '#0f0', padding: '10px', height: '300px', overflowY: 'scroll', fontFamily: 'monospace', fontSize: '10px' }}>
                    {logs.map((l, i) => (
                        <div key={i} style={{ borderBottom: '1px solid #444', marginBottom: '5px' }}>{l}</div>
                    ))}
                </div>
            </IonContent>
        </IonPage>
    );
};

export default DebugPage;
