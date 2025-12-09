import { registerPlugin } from '@capacitor/core';

export interface MetaScanPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  connect(options: { address: string }): Promise<void>;
  disconnect(options: { address: string }): Promise<void>;
  performScan(): Promise<void>;
  getScanData(): Promise<{ length: number; wavelengths?: number[]; intensities?: number[] }>;
  addListener(eventName: 'deviceFound', listenerFunc: (device: { name: string; address: string; rssi: number }) => void): Promise<any>;
  addListener(eventName: 'metaScanEvent', listenerFunc: (event: { event: string; data?: any }) => void): Promise<any>;
}

const MetaScan = registerPlugin<MetaScanPlugin>('MetaScan');

export interface BleDevice {
    deviceId: string;
    name: string;
    rssi: number;
}

export interface DeviceStatus {
    connected: boolean;
    batteryLevel: number;
    isScanning: boolean;
    cartridgeInserted: boolean;
}

export interface DeviceResult {
    timestamp: string;
    value: string;
    units: string;
    status: 'normal' | 'abnormal' | 'critical';
    hematocrit?: number;
    hemoglobin?: number;
    rbc?: number;
    rdw?: number;
}

class DeviceService {
    private status: DeviceStatus = {
        connected: false,
        batteryLevel: 0,
        isScanning: false,
        cartridgeInserted: false
    };
    private subscribers: ((status: DeviceStatus) => void)[] = [];
    private connectedDeviceAddress: string | null = null;
    private discoveredDevices: BleDevice[] = [];
    private pendingAnalysisResolve: ((value: DeviceResult | PromiseLike<DeviceResult>) => void) | null = null;

    constructor() {
        this.initListeners();
    }

    private async initListeners() {
        await MetaScan.addListener('deviceFound', async (device) => {
             const bleDevice: BleDevice = {
                 deviceId: device.address,
                 name: device.name,
                 rssi: device.rssi
             };

             if (!this.discoveredDevices.find(d => d.deviceId === bleDevice.deviceId)) {
                 this.discoveredDevices.push(bleDevice);
             }

             console.log('Found device:', device);
        });

        await MetaScan.addListener('metaScanEvent', async (data) => {
             console.log('SDK Event:', data.event);
             if (data.event === 'NOTIFY_COMPLETE') {
                 this.updateStatus({ connected: true, batteryLevel: 100 });
             } else if (data.event === 'DISCONNECTED') {
                 this.updateStatus({ connected: false, cartridgeInserted: false });
                 this.connectedDeviceAddress = null;
             } else if (data.event === 'SCAN_DATA_READY') {
                 // Try to get data from event or fetch it
                 let scanResult = data.data;
                 if (!scanResult) {
                     try {
                         scanResult = await MetaScan.getScanData();
                     } catch(e) {
                         console.error("Failed to fetch scan data", e);
                     }
                 }

                 if (this.pendingAnalysisResolve && scanResult) {
                     // Process data logic here.
                     // For now, we simulate the result calculation based on valid scan data
                     // In a real app, we would send this data to a cloud API or local algorithm
                     const calculatedResult: DeviceResult = {
                        timestamp: new Date().toISOString(),
                        value: "13.5", // This would be calculated from wavelengths/intensities
                        units: "g/dL",
                        status: "normal",
                        hematocrit: 40,
                        hemoglobin: 13.5,
                        rbc: 4.8,
                        rdw: 12.0
                    };
                    this.pendingAnalysisResolve(calculatedResult);
                    this.pendingAnalysisResolve = null;
                 }
             }
        });
    }

    private updateStatus(newStatus: Partial<DeviceStatus>) {
        this.status = { ...this.status, ...newStatus };
        this.notifySubscribers();
    }

    private notifySubscribers() {
        this.subscribers.forEach(cb => cb(this.status));
    }

    public subscribe(callback: (status: DeviceStatus) => void): () => void {
        this.subscribers.push(callback);
        callback(this.status);
        return () => {
            this.subscribers = this.subscribers.filter(cb => cb !== callback);
        };
    }

    // Combined flow
    public async scanAndConnect(): Promise<void> {
        if (this.status.connected) return;
        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];
        try {
            await MetaScan.startScan();
            // Wait for a device (simplified logic: connect to first found)
            return new Promise((resolve, reject) => {
                 const checkInterval = setInterval(async () => {
                      if (this.discoveredDevices.length > 0) {
                          clearInterval(checkInterval);
                          this.stopScanning();
                          await this.connect(this.discoveredDevices[0].deviceId);
                          resolve();
                      }
                 }, 500);

                 setTimeout(() => {
                     if (this.status.isScanning) {
                         clearInterval(checkInterval);
                         this.stopScanning();
                         // reject(new Error("No device found")); // Don't reject for UI smoothness, just stop
                         this.updateStatus({ isScanning: false });
                     }
                 }, 10000);
            });
        } catch (e) {
            this.updateStatus({ isScanning: false });
            throw e;
        }
    }

    // Explicit scan for list
    public async scan(): Promise<BleDevice[]> {
        this.discoveredDevices = [];
        this.updateStatus({ isScanning: true });
        await MetaScan.startScan();
        return new Promise(resolve => {
            setTimeout(async () => {
                await this.stopScanning();
                resolve([...this.discoveredDevices]);
            }, 5000);
        });
    }

    private async stopScanning() {
        await MetaScan.stopScan();
        this.updateStatus({ isScanning: false });
    }

    public async connect(address: string): Promise<boolean> {
        try {
            await MetaScan.connect({ address });
            this.connectedDeviceAddress = address;
            return true;
        } catch (e) {
            console.error(e);
            this.updateStatus({ connected: false });
            return false;
        }
    }

    public insertCartridge() {
        this.updateStatus({ cartridgeInserted: true });
    }

    public async runAnalysis(): Promise<DeviceResult> {
        if (!this.status.connected) throw new Error("Device not connected");

        await MetaScan.performScan();

        return new Promise((resolve, reject) => {
            // Set timeout for analysis
            const timeout = setTimeout(() => {
                this.pendingAnalysisResolve = null;
                reject(new Error("Analysis timed out"));
            }, 10000);

            this.pendingAnalysisResolve = (result) => {
                clearTimeout(timeout);
                resolve(result);
            };
        });
    }

    public async disconnect() {
        if (this.connectedDeviceAddress) {
            await MetaScan.disconnect({ address: this.connectedDeviceAddress });
        }
    }

    public isConnected(): boolean {
        return this.status.connected;
    }

    public async getTemperature(): Promise<number> {
        // Mock or retrieve if SDK supports it
        return 37.0;
    }
}

export const deviceService = new DeviceService();
