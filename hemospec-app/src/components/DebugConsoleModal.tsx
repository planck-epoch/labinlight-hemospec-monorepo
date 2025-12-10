import React, { useEffect, useState } from 'react';
import { IonModal, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonButton, IonList, IonItem, IonLabel, IonText, IonFooter } from '@ionic/react';
import { logger, LogEntry } from '../services/LoggerService';

interface DebugConsoleModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const DebugConsoleModal: React.FC<DebugConsoleModalProps> = ({ isOpen, onClose }) => {
    const [logs, setLogs] = useState<LogEntry[]>([]);

    useEffect(() => {
        if (!isOpen) return;

        // Initial load
        setLogs([...logger.getLogs()]);

        // Subscribe to updates
        const unsubscribe = logger.subscribe(() => {
            setLogs([...logger.getLogs()]);
        });

        return unsubscribe;
    }, [isOpen]);

    const getLevelColor = (level: string) => {
        switch (level) {
            case 'error': return 'danger';
            case 'warn': return 'warning';
            case 'debug': return 'medium';
            default: return 'dark';
        }
    };

    const formatData = (data: any) => {
        if (!data) return '';
        try {
            const str = JSON.stringify(data);
            return str.length > 100 ? str.substring(0, 100) + '...' : str;
        } catch (e) {
            return '[Circular/Invalid Data]';
        }
    };

    return (
        <IonModal isOpen={isOpen} onDidDismiss={onClose}>
            <IonHeader>
                <IonToolbar>
                    <IonTitle>Debug Console</IonTitle>
                    <IonButtons slot="end">
                        <IonButton onClick={onClose}>Close</IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonHeader>
            <IonContent className="ion-padding">
                <IonList>
                    {logs.length === 0 && (
                        <IonItem lines="none">
                            <IonLabel className="ion-text-center" color="medium">No logs yet.</IonLabel>
                        </IonItem>
                    )}
                    {logs.map((log, index) => (
                        <IonItem key={index}>
                            <IonLabel className="ion-text-wrap">
                                <IonText color={getLevelColor(log.level)} style={{ fontSize: '0.8rem', fontWeight: 'bold' }}>
                                    [{log.level.toUpperCase()}] {new Date(log.timestamp).toLocaleTimeString()}
                                </IonText>
                                <p style={{ fontWeight: 'bold' }}>{log.message}</p>
                                {log.data && (
                                    <p style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--ion-color-medium)' }}>
                                        {formatData(log.data)}
                                    </p>
                                )}
                            </IonLabel>
                        </IonItem>
                    ))}
                </IonList>
            </IonContent>
            <IonFooter>
                <IonToolbar>
                    <IonButtons slot="start">
                         <IonButton color="danger" onClick={() => logger.clear()}>Clear Logs</IonButton>
                    </IonButtons>
                    <IonButtons slot="end">
                         <IonButton onClick={() => setLogs([...logger.getLogs()])}>Refresh</IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonFooter>
        </IonModal>
    );
};

export default DebugConsoleModal;
