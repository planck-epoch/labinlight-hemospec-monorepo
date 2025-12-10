import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonList, IonItem, IonLabel, IonNote, IonButton, IonIcon, IonButtons, IonBackButton, IonGrid, IonRow, IonCol, IonBadge, IonText } from '@ionic/react';
import { useLocation, useHistory } from 'react-router-dom';
import { newspaperOutline, ellipsisVertical, sendOutline } from 'ionicons/icons';
import './Results.css';

interface ResultState {
    result: any;
}

// Previous results for Diff calculation
const PREVIOUS_RESULTS = {
    hematocrit: 0.31, // L/L (31%)
    hemoglobin: 13.0, // g/dL
    rbc: 4.5, // M/uL
    rdw: 13.2 // %
};

const Results: React.FC = () => {
  const location = useLocation<ResultState>();
  const history = useHistory();
  const rawResult = location.state?.result;

  // Map API response keys to standardized keys
  // API: { "Eritrocitos": 3.58, "Hemoglobina": 10.8, "Hematocrito": 32.8, "RDW": 14.8, ... }
  const result = rawResult ? {
      hematocrit: rawResult.Hematocrito ? (rawResult.Hematocrito / 100) : (rawResult.hematocrit || 0), // API sends 32.8 (%), we want 0.328 for display if unit is L/L
      hemoglobin: rawResult.Hemoglobina || rawResult.hemoglobin || 0,
      rbc: rawResult.Eritrocitos || rawResult.rbc || 0,
      rdw: rawResult.RDW || rawResult.rdw || 0,
      timestamp: rawResult.timestamp || Date.now()
  } : {
      hematocrit: 0, hemoglobin: 0, rbc: 0, rdw: 0, timestamp: Date.now()
  };

  const parameters = [
      {
          name: 'Haemoglobin (g/dl)',
          key: 'hemoglobin',
          value: result.hemoglobin,
          displayValue: result.hemoglobin.toFixed(1).replace('.', ',') + 'g',
          prev: PREVIOUS_RESULTS.hemoglobin,
          range: '12 - 16 g/dL',
          unit: 'g'
      },
      {
          name: 'Haematocrit (L/L)',
          key: 'hematocrit',
          value: result.hematocrit,
          // If value > 1, assume it's %, convert to L/L
          displayValue: (result.hematocrit > 1 ? result.hematocrit / 100 : result.hematocrit).toFixed(2).replace('.', ',') + 'L',
          prev: PREVIOUS_RESULTS.hematocrit,
          range: '0.37 - 0.47 L/L', // converted 37-47%
          unit: 'L'
      },
      {
          name: 'RDW',
          key: 'rdw',
          value: result.rdw,
          displayValue: result.rdw.toFixed(1).replace('.', ',') + '%',
          prev: PREVIOUS_RESULTS.rdw,
          range: '11.5 - 14.5 %',
          unit: '%'
      },
      {
          name: 'RBC (x10^12L)',
          key: 'rbc',
          value: result.rbc,
          displayValue: result.rbc.toFixed(1).replace('.', ',') + 'L',
          prev: PREVIOUS_RESULTS.rbc,
          range: '4.2 - 5.4 M/µL',
          unit: 'L'
      },
  ];

  const calculateDiff = (current: number, prev: number, unit: string) => {
      // Handle Hematocrit % vs fraction issue for calculation
      let c = current;
      let p = prev;
      // Heuristic: if one is > 1 and other is < 1, normalize to % (larger numbers safer for diff)
      if (c > 1 && p < 1) p = p * 100;
      if (c < 1 && p > 1) c = c * 100;

      const diff = c - p;
      const sign = diff > 0 ? '+' : '';

      // Heuristic for precision: if unit is L (L/L), use 2 decimals for diff
      // Otherwise use 1 decimal (g/dL, %)
      // Note: If we normalized to % above for calculation, we might want to convert back for 'L' unit diff?
      // Design shows "+0,15L" which is 0.15 diff in L/L? No, 0.15 is huge for L/L (15%).
      // 0.46 - 0.31 = 0.15. So yes.
      // My calculation: 0.328 (current) - 0.31 (prev) = 0.018.
      // Display: +0,02L.
      // If the dummy data 32.8% was meant to be 46% to match design, diff would be 0.15.
      // With current dummy data, diff is smaller.

      // Reset logic: calculate simple diff on normalized values.
      // But we display in 'unit'.
      // If unit is L, we want diff in L.
      // If c, p were converted to %, diff is in %. /100 for L.

      let finalDiff = diff;
      if (unit === 'L' && (c > 1 || p > 1)) {
           // We compared in %, but want to show L/L diff?
           // Actually, if unit is L, we should probably stick to L/L.
           // Let's assume input 'current' for Hematocrit is already normalized to L/L in the `result` object construction if it came as %.
           // My code: result.hematocrit = raw / 100. So it is L/L.
           // PREVIOUS_RESULTS.hematocrit = 0.31. L/L.
           // So diff = 0.328 - 0.31 = 0.018.
           // c and p should NOT be multiplied by 100 in the first place if consistent.
      } else {
           // c and p logic above was to fix mismatched scales.
      }

      // Let's use the 'result' object values which I already normalized in the const result = ... block
      // So use original arguments passed to this function which come from `result` object.
      // Re-eval:

      const safeDiff = current - prev;

      // Precision
      const decimals = unit === 'L' ? 2 : 1;

      return {
          value: safeDiff,
          label: `${sign}${safeDiff.toFixed(decimals).replace('.', ',')}`,
          color: safeDiff < 0 ? 'danger' : 'secondary'
      };
  };

  return (
    <IonPage>
      <IonHeader className="ion-no-border">
        <IonToolbar>
            <IonButtons slot="start">
                <IonBackButton defaultHref="/app/home" />
            </IonButtons>
          <IonTitle style={{ fontWeight: 'bold' }}>Analysis results</IonTitle>
           <IonButtons slot="end">
                <IonButton>
                    <IonIcon icon={ellipsisVertical} />
                </IonButton>
           </IonButtons>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding" style={{ '--background': '#F0F7F7' }}>

        <div className="patient-info ion-margin-bottom">
            <IonText color="medium" style={{ fontSize: '0.9rem' }}>Patient ID</IonText>
            <h2 style={{ marginTop: '5px', fontWeight: 'bold', fontSize: '1.5rem' }}>24058P</h2>
            <div style={{ marginTop: '5px', color: 'var(--ion-color-dark)' }}>
                <span>Gender: <span style={{ fontWeight: '500' }}>Female</span></span>
                <span style={{ marginLeft: '15px' }}>Age: <span style={{ fontWeight: '500' }}>34</span></span>
            </div>
            <div style={{ marginTop: '15px', color: 'var(--ion-color-medium)', fontSize: '0.9rem' }}>
                {new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
            </div>
        </div>

        <IonGrid className="ion-no-padding">
            <IonRow>
                {parameters.map((param, index) => {
                    const diff = calculateDiff(param.value, param.prev, param.unit);
                    const diffLabel = `${diff.label}${param.unit}`;

                    const badgeStyle = {
                        background: diff.color === 'danger' ? 'rgba(var(--ion-color-danger-rgb), 0.1)' : 'rgba(var(--ion-color-secondary-rgb), 0.1)',
                        color: diff.color === 'danger' ? 'var(--ion-color-danger)' : 'var(--ion-color-secondary)',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '0.8rem',
                        fontWeight: 'bold',
                        marginLeft: '10px'
                    };

                    return (
                        <IonCol size="6" key={index} style={{ padding: '5px' }}>
                            <div style={{
                                background: 'white',
                                borderRadius: '15px',
                                padding: '15px',
                                height: '100%',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                                border: '1px solid rgba(0,0,0,0.05)'
                            }}>
                                <div style={{ color: 'var(--ion-color-medium)', fontSize: '0.85rem', marginBottom: '5px' }}>
                                    {param.name}
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '5px' }}>
                                    <span style={{ fontSize: '1.4rem', fontWeight: 'bold', color: 'var(--ion-color-dark)' }}>
                                        {param.displayValue}
                                    </span>
                                    <span style={badgeStyle}>
                                        {diffLabel}
                                    </span>
                                </div>
                            </div>
                        </IonCol>
                    );
                })}
            </IonRow>
        </IonGrid>

        <div className="ion-margin-top" style={{ display: 'flex', gap: '15px', marginTop: '40px' }}>
            <IonButton expand="block" style={{ flex: 1, '--border-radius': '25px', fontWeight: 'bold' }} color="primary">
                <IonIcon icon={newspaperOutline} slot="start" />
                Patient history
            </IonButton>
            <IonButton expand="block" style={{ flex: 1, '--border-radius': '25px', fontWeight: 'bold' }} color="primary">
                 <IonIcon icon={sendOutline} slot="start" />
                 Send results
            </IonButton>
        </div>

      </IonContent>
    </IonPage>
  );
};

export default Results;
