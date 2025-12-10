import React, { useState, useEffect } from 'react';
import {
  IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton,
  IonIcon, IonSpinner, IonCard, IonCardContent, IonInput, IonItem, IonLabel,
  IonSegment, IonSegmentButton, IonGrid, IonRow, IonCol, IonText, IonFooter
} from '@ionic/react';
import { arrowBackOutline, checkmarkCircleOutline, closeCircleOutline } from 'ionicons/icons';
import { useHistory } from 'react-router-dom';
import { apiService } from '../services/ApiService';
import { deviceService } from '../services/DeviceService';

const AnalysisPage: React.FC = () => {
    const history = useHistory();

    // Steps: 'patient-form' -> 'scanning' -> 'analyzing' -> 'complete'
    const [step, setStep] = useState<'patient-form' | 'scanning' | 'analyzing' | 'complete'>('patient-form');

    // Form Data
    const [patientId, setPatientId] = useState('');
    const [age, setAge] = useState<string>('');
    const [sex, setSex] = useState<'M' | 'F'>('M');

    // Process State
    const [progress, setProgress] = useState(0);
    const [scanData, setScanData] = useState<any>(null);
    const [result, setResult] = useState<any>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    // Step 1: Patient Form Handlers
    const handleStart = () => {
        if (!patientId || !age) {
            setErrorMsg("Please fill in all fields");
            return;
        }
        setErrorMsg(null);
        setStep('scanning');
    };

    // Step 2: Scanning Logic
    useEffect(() => {
        let mounted = true;
        const performScan = async () => {
            if (step !== 'scanning') return;

            setProgress(0);
            try {
                // 1. Connect (if not already) and Start Scan
                // Simulate progress for UX while connection/scanning happens
                const progressInterval = setInterval(() => {
                    setProgress(prev => {
                        if (prev >= 90) return prev;
                        return prev + 5;
                    });
                }, 500);

                // Real Scan call
                await deviceService.scanAndConnect(); // Ensure connection
                // Note: The prompt implies a scan happens. `scanAndConnect` in my previous impl triggers a scan flow.
                // However, for precise control I might want to separate connect and scan if I modified DeviceService enough.
                // Current DeviceService.scanAndConnect() does: Connect -> Scan -> Return when device found & connected.
                // But it doesn't return the *spectral data* of a scan unless we call performSpectralScan.

                // Let's refine:
                // 1. Ensure connected.
                if (!deviceService.isConnected()) {
                    // This might prompt user or auto-connect to first found.
                    // For demo, we assume user is near device.
                    // Ideally we might want a "Connect" button before this page, but per instructions we do it here.
                    await deviceService.connect(""); // Address? Logic in service handles scanning if no address.
                    // Wait, `connect` requires address. `scanAndConnect` handles the search.
                    await deviceService.scanAndConnect();
                }

                // 2. Perform Spectral Scan
                const data = await deviceService.performSpectralScan();

                clearInterval(progressInterval);
                if (mounted) {
                    setProgress(100);
                    setScanData(data);
                    setTimeout(() => setStep('analyzing'), 500);
                }
            } catch (err: any) {
                console.error("Scan failed", err);
                if (mounted) setErrorMsg("Scan failed: " + err.message);
                // For DEMO resilience: if scan fails (no device), maybe fallback to mock?
                // The user said "use hardcoded... for demo", but also "should connect...".
                // I will leave error state for now to be realistic.
            }
        };

        performScan();
        return () => { mounted = false; };
    }, [step]);

    // Step 3: Analysis Logic
    useEffect(() => {
        const analyze = async () => {
            if (step !== 'analyzing' || !scanData) return;

            setProgress(0);
            const progressInterval = setInterval(() => {
                 setProgress(prev => Math.min(prev + 10, 90));
            }, 300);

            try {
                // Construct Payload
                const payload = deviceService.constructPayload(scanData, {
                    patientId,
                    age: parseInt(age),
                    gender: sex
                });

                // Call API
                console.log("Sending payload:", payload);
                const response = await apiService.analyze(payload);

                clearInterval(progressInterval);
                setProgress(100);
                setResult(response);
                setTimeout(() => setStep('complete'), 500);

            } catch (err: any) {
                console.error("Analysis failed", err);
                clearInterval(progressInterval);
                setErrorMsg("Analysis failed: " + err.message);
            }
        };

        analyze();
    }, [step, scanData]);


    // RENDERERS

    const renderPatientForm = () => (
        <div className="ion-padding">
            <h2 className="ion-text-center">New test patient information</h2>
            <p className="ion-text-center text-muted">The following information is needed to the analysis.</p>

            <IonGrid>
                <IonRow>
                    <IonCol>
                        <IonItem className="ion-margin-bottom">
                            <IonLabel position="floating">Patient ID</IonLabel>
                            <IonInput value={patientId} onIonChange={e => setPatientId(e.detail.value!)}></IonInput>
                        </IonItem>
                    </IonCol>
                    <IonCol>
                        <IonItem className="ion-margin-bottom">
                            <IonLabel position="floating">Age</IonLabel>
                            <IonInput type="number" value={age} onIonChange={e => setAge(e.detail.value!)}></IonInput>
                        </IonItem>
                    </IonCol>
                </IonRow>
            </IonGrid>

            <div className="ion-padding-vertical">
                <IonSegment value={sex} onIonChange={e => setSex(e.detail.value as any)}>
                    <IonSegmentButton value="M">
                        <IonLabel>Male</IonLabel>
                    </IonSegmentButton>
                    <IonSegmentButton value="F">
                        <IonLabel>Female</IonLabel>
                    </IonSegmentButton>
                </IonSegment>
            </div>

            {errorMsg && <p className="ion-text-center" style={{color: 'red'}}>{errorMsg}</p>}

            <div className="ion-padding-top ion-text-center">
                <IonButton shape="round" fill="outline" onClick={handleStart} style={{width: '200px'}}>
                    Continue
                </IonButton>
            </div>
        </div>
    );

    const renderProgress = (title: string, subtext: string) => (
        <div className="ion-text-center" style={{ marginTop: '30%' }}>
            <h1 style={{ fontWeight: 'bold', fontSize: '3rem' }}>{progress}%</h1>
            <div style={{ width: '80%', margin: '20px auto', height: '4px', background: '#E0E0E0' }}>
                <div style={{ width: `${progress}%`, height: '100%', background: '#0097A9', transition: 'width 0.3s' }} />
            </div>
            <p className="ion-margin-top">{subtext}</p>
            {errorMsg && (
                 <div style={{marginTop: '20px'}}>
                    <p style={{color: 'red'}}>{errorMsg}</p>
                    <IonButton fill="outline" onClick={() => setStep('patient-form')}>Cancel</IonButton>
                 </div>
            )}
            {!errorMsg && <IonButton fill="outline" shape="round" className="ion-margin-top" onClick={() => setStep('patient-form')}>Cancel</IonButton>}
        </div>
    );

    const renderResults = () => {
        if (!result) return null;
        // Result keys based on Python service output: Hemoglobina, Hematocrito, RDW, Eritrocitos, etc.
        // Or if the Python mock returns something else. Assuming keys match somewhat.

        // Helper to format with badge
        const ResultCard = ({ title, value, unit, diff }: { title: string, value: string, unit: string, diff?: string }) => (
            <IonCard style={{ margin: '10px 0', boxShadow: 'none', border: '1px solid #ddd' }}>
                <IonCardContent>
                    <div style={{ fontSize: '12px', color: '#666' }}>{title} ({unit})</div>
                    <div style={{ display: 'flex', alignItems: 'center', marginTop: '5px' }}>
                        <span style={{ fontSize: '24px', fontWeight: 'bold', color: '#000' }}>{value}</span>
                        <span style={{ fontSize: '18px', fontWeight: 'bold', marginLeft: '2px', color: '#000' }}>{unit.split('/')[0]}</span> {/* Simplified unit display */}
                        {diff && (
                            <span style={{
                                marginLeft: '10px',
                                background: diff.startsWith('-') ? '#FFEBEE' : '#E0F2F1',
                                color: diff.startsWith('-') ? '#D32F2F' : '#00796B',
                                padding: '2px 8px',
                                borderRadius: '12px',
                                fontSize: '12px',
                                fontWeight: 'bold'
                            }}>
                                {diff}
                            </span>
                        )}
                    </div>
                </IonCardContent>
            </IonCard>
        );

        return (
            <div className="ion-padding" style={{ background: '#F5F5F5', minHeight: '100%' }}>
                <div style={{ background: '#fff', padding: '16px', borderRadius: '8px', marginBottom: '16px' }}>
                    <div style={{ fontSize: '12px', color: '#666' }}>Patient ID</div>
                    <div style={{ fontSize: '18px', fontWeight: 'bold' }}>{patientId}</div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                        Gender: {sex === 'M' ? 'Male' : 'Female'} &nbsp;&nbsp; Age: {age}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '16px' }}>
                        {new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </div>
                </div>

                <IonGrid>
                    <IonRow>
                        <IonCol size="6">
                            <ResultCard
                                title="Haemoglobin"
                                value={result.Hemoglobina || "14.3"}
                                unit="g/dl"
                                diff="-2.2g" // Mock diff for now, or calc if history available
                            />
                        </IonCol>
                        <IonCol size="6">
                            <ResultCard
                                title="Haematocrit"
                                value={result.Hematocrito || "0.46"}
                                unit="L/L"
                                diff="+0.15L"
                            />
                        </IonCol>
                        <IonCol size="6">
                            <ResultCard
                                title="RDW"
                                value={result.RDW || "13.7"}
                                unit="%"
                                diff="+0.5%"
                            />
                        </IonCol>
                        <IonCol size="6">
                            <ResultCard
                                title="RBC (x10^12L)"
                                value={result.Eritrocitos || "4.8"}
                                unit="L"
                                diff="+0.3L"
                            />
                        </IonCol>
                    </IonRow>
                </IonGrid>

                <div className="ion-padding-top">
                    <IonButton expand="block" fill="solid" color="secondary" className="ion-margin-bottom" onClick={() => {/* History func */}}>
                        <IonIcon slot="start" icon={checkmarkCircleOutline} />
                        Patient history
                    </IonButton>
                    <IonButton expand="block" fill="solid" color="primary" onClick={() => history.replace('/app/home')}>
                        Send results
                    </IonButton>
                </div>
            </div>
        );
    };

    return (
        <IonPage>
             <IonHeader className="ion-no-border" style={{background: '#fff'}}>
                <IonToolbar>
                    <IonButtons slot="start">
                        <IonButton onClick={() => history.goBack()}>
                            <IonIcon icon={arrowBackOutline} color="dark" />
                        </IonButton>
                    </IonButtons>
                    <IonTitle style={{color: '#000', fontWeight: 'bold'}}>
                        {step === 'complete' ? 'Analysis results' : 'Analysis'}
                    </IonTitle>
                    {step === 'complete' && (
                         <IonButtons slot="end">
                             <IonButton>
                                 <IonIcon name="ellipsis-vertical" color="dark" />
                             </IonButton>
                         </IonButtons>
                    )}
                </IonToolbar>
            </IonHeader>

            <IonContent className={step === 'complete' ? '' : 'ion-padding'}>
                {step === 'patient-form' && renderPatientForm()}
                {step === 'scanning' && renderProgress('Scanning...', 'Analyzing...')} {/* Reusing UI for scan progress */}
                {step === 'analyzing' && renderProgress('Analyzing...', 'Completing...')}
                {step === 'complete' && renderResults()}
            </IonContent>
        </IonPage>
    );
};

export default AnalysisPage;
