import { registerPlugin, Capacitor } from '@capacitor/core';
import { AndroidPermissions } from '@awesome-cordova-plugins/android-permissions';
import { ANALYSIS_PAYLOAD_TEMPLATE } from './payloadTemplate';

export interface MetaScanPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  connect(options: { address: string }): Promise<void>;
  disconnect(options: { address: string }): Promise<void>;
  performScan(): Promise<void>;
  getScanData(): Promise<any>;
  getDeviceStatus(): Promise<any>;
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
    temperature?: number;
    serialNumber?: string;
}

export interface ScanResult {
    wavelengths?: number[];
    intensities?: number[];
    length?: number;
    [key: string]: any;
}

class DeviceService {
    private status: DeviceStatus = {
        connected: false,
        batteryLevel: 0,
        isScanning: false,
        cartridgeInserted: false,
        temperature: 0,
        serialNumber: ''
    };
    private subscribers: ((status: DeviceStatus) => void)[] = [];
    private connectedDeviceAddress: string | null = null;
    private discoveredDevices: BleDevice[] = [];
    private pendingScanResolve: ((value: any) => void) | null = null;

    constructor() {
        this.initListeners();
    }

    private async requestAndroidPermissions(): Promise<boolean> {
        if (Capacitor.getPlatform() !== 'android') {
            return true; 
        }

        try {
            let androidPermissions: any = AndroidPermissions;
            if (typeof AndroidPermissions === 'function') {
                 androidPermissions = new (AndroidPermissions as any)();
            }

            // Determine Android version-specific permissions
            // Note: requesting legacy permissions on Android 12 usually auto-grants, 
            // but let's be specific to avoid errors.
            const permissionsToRequest = [
                'android.permission.ACCESS_FINE_LOCATION',
                'android.permission.ACCESS_COARSE_LOCATION',
                'android.permission.BLUETOOTH_SCAN',
                'android.permission.BLUETOOTH_CONNECT'
            ];

            const response = await androidPermissions.requestPermissions(permissionsToRequest);
            
            // Check if specifically BLUETOOTH_SCAN is granted if on newer device
            // or just return the general hasPermission flag
            return response.hasPermission;
        } catch (error) {
            console.error('Error requesting permissions:', error);
            return false;
        }
    }

    private async initListeners() {
        const hasPermission = await this.requestAndroidPermissions();

        if (!hasPermission) {
            console.error("Required Bluetooth permissions were denied.");
            return;
        }
        await MetaScan.addListener('deviceFound', async (device) => {
             const bleDevice: BleDevice = {
                 deviceId: device.address,
                 name: device.name,
                 rssi: device.rssi
             };
             if (!this.discoveredDevices.find(d => d.deviceId === bleDevice.deviceId)) {
                 this.discoveredDevices.push(bleDevice);
             }
             // Auto-connect to NIRScan devices for smoother UX if in scanning mode
             if (this.status.isScanning && device.name.includes("NIR")) {
                 this.stopScanning();
                 this.connect(device.address);
             }
        });

        await MetaScan.addListener('metaScanEvent', async (data) => {
             console.log('SDK Event:', data.event);
             if (data.event === 'NOTIFY_COMPLETE') {
                 this.updateStatus({ connected: true });
                 // If we were waiting for scan data, fetch it now
                 if (this.pendingScanResolve) {
                     try {
                         const scanData = await MetaScan.getScanData();
                         this.pendingScanResolve(scanData);
                     } catch(e) {
                         console.error("Error fetching scan data:", e);
                         this.pendingScanResolve(null);
                     }
                     this.pendingScanResolve = null;
                 }
             } else if (data.event === 'DISCONNECTED') {
                 this.updateStatus({ connected: false });
                 this.connectedDeviceAddress = null;
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

    public async connect(address: string): Promise<boolean> {
        const hasPermission = await this.requestAndroidPermissions();

        if (!hasPermission) {
            console.error("Required Bluetooth permissions were denied.");
            return false;
        }

        try {
            await MetaScan.connect({ address });
            this.connectedDeviceAddress = address;
            this.updateStatus({ connected: true });
            try {
                const status = await MetaScan.getDeviceStatus();
                this.updateStatus({
                    batteryLevel: status.battery,
                    temperature: status.temp,
                    serialNumber: status.serial
                });
            } catch(e) {
                console.warn("Could not fetch device status on connect", e);
            }
            return true;
        } catch (e) {
            console.error("Connection failed", e);
            this.updateStatus({ connected: false });
            return false;
        }
    }

    public async scanAndConnect(): Promise<void> {
        if (this.status.connected) return;
        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];
        try {
            await MetaScan.startScan();
            return new Promise((resolve, reject) => {
                const checkInterval = setInterval(() => {
                    if (this.status.connected) {
                        clearInterval(checkInterval);
                        resolve();
                    }
                }, 500);
                setTimeout(() => {
                    if (!this.status.connected) {
                        clearInterval(checkInterval);
                        this.stopScanning();
                        reject(new Error("No device found or connected"));
                    }
                }, 10000);
            });
        } catch (e) {
            this.updateStatus({ isScanning: false });
            throw e;
        }
    }

    public async disconnect() {
        if (this.connectedDeviceAddress) {
            await MetaScan.disconnect({ address: this.connectedDeviceAddress });
            this.connectedDeviceAddress = null;
            this.updateStatus({ connected: false });
        }
    }

    public isConnected(): boolean {
        return this.status.connected;
    }

    public async startScanForDevices(): Promise<void> {
        const hasPermission = await this.requestAndroidPermissions();

        if (!hasPermission) {
            console.error("Required Bluetooth permissions were denied.");
            return;
        }

        this.discoveredDevices = [];
        this.updateStatus({ isScanning: true });
        await MetaScan.startScan();
    }

    public async stopScanning() {
        await MetaScan.stopScan();
        this.updateStatus({ isScanning: false });
    }

    // Legacy method for compatibility
    public async scan(): Promise<ScanResult> {
        // Run spectral scan
        return this.performSpectralScan();
    }

    public async performSpectralScan(): Promise<ScanResult> {
        if (!this.status.connected) throw new Error("Device not connected");

        return new Promise(async (resolve, reject) => {
            this.pendingScanResolve = resolve;
            // 15s timeout
            setTimeout(() => {
                if (this.pendingScanResolve) {
                    this.pendingScanResolve = null;
                    reject(new Error("Scan timed out"));
                }
            }, 15000);

            try {
                await MetaScan.performScan();
            } catch (e) {
                this.pendingScanResolve = null;
                reject(e);
            }
        });
    }

    public formatScanData(scanResult: ScanResult | null): any {
        // Deep copy the template
        const payload = JSON.parse(JSON.stringify(ANALYSIS_PAYLOAD_TEMPLATE));

        if (scanResult) {
            if (scanResult.intensities && Array.isArray(scanResult.intensities) && scanResult.intensities.length > 0) {
                payload.Samples[0].Intensity = scanResult.intensities;
            }
            if (scanResult.wavelengths && Array.isArray(scanResult.wavelengths) && scanResult.wavelengths.length > 0) {
                payload.Samples[0].WaveLength = scanResult.wavelengths;
                payload.Samples[0].Wavelength = scanResult.wavelengths; // Template has both
            }
        }

        // Ensure SerialNumber is set
        if (!payload.SerialNumber || payload.SerialNumber === "") {
            payload.SerialNumber = "0000000";
        }
        if (payload.Samples && payload.Samples[0] && (!payload.Samples[0].SerialNumber || payload.Samples[0].SerialNumber === "")) {
             // The sample might not have SerialNumber field in template?
             // Template has "SerialNumber" at root.
             // And "RefSerialNumber" in Sample.
        }

        return payload;
    }
}

export const deviceService = new DeviceService();
