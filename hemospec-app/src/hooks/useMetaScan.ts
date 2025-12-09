import { registerPlugin } from '@capacitor/core';
import { useState, useCallback, useEffect } from 'react';

export interface MetaScanPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  connect(options: { address: string }): Promise<void>;
  disconnect(options: { address: string }): Promise<void>;
  performScan(): Promise<void>;
  getScanData(): Promise<{ length: number; wavelengths?: number[]; intensities?: number[] }>;
  addListener(eventName: 'deviceFound', listenerFunc: (device: { name: string; address: string; rssi: number }) => void): Promise<any>;
  addListener(eventName: 'metaScanEvent', listenerFunc: (event: { event: string }) => void): Promise<any>;
}

const MetaScan = registerPlugin<MetaScanPlugin>('MetaScan');

export const useMetaScan = () => {
  const [discoveredDevices, setDiscoveredDevices] = useState<any[]>([]);
  const [isScanning, setIsScanning] = useState(false);
  const [connectedDevice, setConnectedDevice] = useState<any | null>(null);
  const [scanData, setScanData] = useState<any>(null);
  const [isConnecting, setIsConnecting] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  const log = useCallback((message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs(prev => [`[${timestamp}] ${message}`, ...prev]);
  }, []);

  useEffect(() => {
    const setupListeners = async () => {
      await MetaScan.addListener('deviceFound', (device) => {
        setDiscoveredDevices(prev => {
          if (!prev.find(d => d.address === device.address)) {
            log(`Found device: ${device.name} (${device.address})`);
            return [...prev, device];
          }
          return prev;
        });
      });

      await MetaScan.addListener('metaScanEvent', (data) => {
         log(`SDK Event: ${data.event}`);
         if (data.event === 'NOTIFY_COMPLETE') {
             // Connected fully?
             setIsConnecting(false);
         } else if (data.event === 'SCAN_DATA_READY') {
             // Fetch data
             MetaScan.getScanData().then(data => {
                 setScanData(data);
                 log('Scan data received and stored.');
             }).catch(err => log(`Error fetching scan data: ${err}`));
         } else if (data.event === 'DISCONNECTED') {
             setConnectedDevice(null);
             setIsConnecting(false);
         }
      });
    };
    setupListeners();
  }, [log]);

  const startScan = async () => {
    if (isScanning) return;
    log("Starting BLE scan...");
    setDiscoveredDevices([]);
    setIsScanning(true);
    try {
      await MetaScan.startScan();
      // Auto stop scan is handled in native, but we can update state after a timeout to match UI expectation
      setTimeout(() => {
          setIsScanning(false);
          log("Scan stopped (timeout).");
      }, 10000);
    } catch (error: any) {
      log(`Scan Error: ${error.message}`);
      setIsScanning(false);
    }
  };

  const stopScan = async () => {
    if (!isScanning) return;
    try {
      await MetaScan.stopScan();
      log("Scan stopped.");
      setIsScanning(false);
    } catch (error: any) {
      log(`Stop Scan Error: ${error.message}`);
    }
  };

  const connect = async (device: any) => {
    if (connectedDevice || isConnecting) return;
    log(`Connecting to ${device.name}...`);
    setIsConnecting(true);
    try {
      await MetaScan.connect({ address: device.address });
      setConnectedDevice(device);
      // Wait for NOTIFY_COMPLETE event to confirm full readiness
    } catch (error: any) {
      log(`Connection Error: ${error.message}`);
      setIsConnecting(false);
      setConnectedDevice(null);
    }
  };

  const disconnect = async () => {
     if (!connectedDevice) return;
     try {
         await MetaScan.disconnect({ address: connectedDevice.address });
         setConnectedDevice(null);
     } catch (error: any) {
         log(`Disconnect Error: ${error.message}`);
     }
  };

  const performDeviceScan = async () => {
      if (!connectedDevice) {
          log("Cannot perform scan: No device connected.");
          return;
      }
      log("Starting on-device scan...");
      try {
          await MetaScan.performScan();
      } catch (error: any) {
          log(`On-device scan error: ${error.message}`);
      }
  };

  return { discoveredDevices, isScanning, connectedDevice, scanData, isConnecting, logs, startScan, stopScan, connect, disconnect, performDeviceScan };
};
