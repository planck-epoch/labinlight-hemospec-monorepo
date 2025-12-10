import { BleClient, BleDevice, ScanResult as BleScanResult } from '@capacitor-community/bluetooth-le';
import { Capacitor } from '@capacitor/core';
import { ANALYSIS_PAYLOAD_TEMPLATE } from './payloadTemplate';
import { apiService } from './ApiService';

export type { BleDevice };

// UUID Definition
// Base UUID Pattern: 534552xx-444C-5020-4E49-52204E616E6F (derived from user info)
// Services:
// Scan Data Info Service (GSDIS): 53455206-444C-5020-4E49-52204E616E6F
const SCAN_DATA_SERVICE_UUID = '53455206-444c-5020-4e49-52204e616e6f';

// Characteristics:
// Start Scan: 0x4348411D -> 4348411d-444c-5020-4e49-52204e616e6f
const START_SCAN_CHAR_UUID = '4348411d-444c-5020-4e49-52204e616e6f';

// Return Serialized Scan Data: 0x43484128 -> 43484128-444c-5020-4e49-52204e616e6f
const READ_SCAN_DATA_CHAR_UUID = '43484128-444c-5020-4e49-52204e616e6f';

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

// Export for backward compatibility
export interface DeviceResult {
    [key: string]: any;
}

class DeviceService {
    private status: DeviceStatus = {
        connected: false,
        batteryLevel: 85, // Mock default
        isScanning: false,
        cartridgeInserted: true, // Mock default
        temperature: 25.0, // Mock default
        serialNumber: '6100023'
    };
    private subscribers: ((status: DeviceStatus) => void)[] = [];
    private connectedDeviceId: string | null = null;
    private discoveredDevices: BleDevice[] = [];
    private bleInitialized = false;

    constructor() {
        this.init();
    }

    private async init() {
        try {
            await BleClient.initialize();
            this.bleInitialized = true;
        } catch (error) {
            console.error('BLE Initialization failed:', error);
        }
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

    public async connect(deviceId: string): Promise<boolean> {
        if (!this.bleInitialized) await this.init();

        try {
            await BleClient.connect(deviceId, (deviceId) => {
                console.log('Device disconnected:', deviceId);
                this.updateStatus({ connected: false });
                this.connectedDeviceId = null;
            });

            this.connectedDeviceId = deviceId;
            this.updateStatus({ connected: true });
            console.log('Connected to device:', deviceId);

            return true;
        } catch (error) {
            console.error('Connection failed:', error);
            this.updateStatus({ connected: false });
            return false;
        }
    }

    public async scanAndConnect(): Promise<void> {
        if (!this.bleInitialized) await this.init();
        if (this.status.connected) return;

        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];

        try {
            await BleClient.requestLEScan(
                {
                    // namePrefix: 'NIR' // Optional
                },
                (result) => {
                    if (result.device.name && result.device.name.includes('NIR')) {
                        console.log('Found NIR device:', result.device);
                         BleClient.stopLEScan().then(() => {
                             this.connect(result.device.deviceId);
                         });
                    }
                }
            );

            // Timeout to stop scanning
            setTimeout(async () => {
                if (!this.status.connected) {
                    await BleClient.stopLEScan();
                    this.updateStatus({ isScanning: false });
                }
            }, 10000);

        } catch (error) {
            console.error('Scanning failed:', error);
            this.updateStatus({ isScanning: false });
            throw error;
        }
    }

    public async startScanForDevices(): Promise<void> {
        if (!this.bleInitialized) await this.init();
        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];

        try {
            await BleClient.requestLEScan({}, (result) => {
                if (result.device.name && result.device.name.includes('NIR')) {
                     const exists = this.discoveredDevices.find(d => d.deviceId === result.device.deviceId);
                     if (!exists) {
                         this.discoveredDevices.push(result.device);
                         // Notify 'deviceFound' listeners if we had any.
                     }
                }
            });

             setTimeout(async () => {
                await BleClient.stopLEScan();
                this.updateStatus({ isScanning: false });
            }, 10000);
        } catch (e) {
            console.error('Scan error', e);
            this.updateStatus({ isScanning: false });
        }
    }

    public getDiscoveredDevices() {
        return this.discoveredDevices;
    }

    // This method is used by DeviceConnectionModal to get list of devices
    public async scan(): Promise<BleDevice[]> {
        if (!this.bleInitialized) await this.init();
        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];

        return new Promise(async (resolve) => {
            const found: BleDevice[] = [];
            try {
                await BleClient.requestLEScan({}, (result) => {
                    if (result.device.name && result.device.name.includes('NIR')) {
                         if (!found.find(d => d.deviceId === result.device.deviceId)) {
                             found.push(result.device);
                         }
                    }
                });
            } catch(e) { console.error(e); }

            // Scan for 3 seconds then return
            setTimeout(async () => {
                await BleClient.stopLEScan();
                this.updateStatus({ isScanning: false });
                this.discoveredDevices = found;
                resolve(found);
            }, 3000);
        });
    }

    public async stopScanning() {
        try {
            await BleClient.stopLEScan();
        } catch(e) {
            // ignore
        }
        this.updateStatus({ isScanning: false });
    }

    public async disconnect() {
        if (this.connectedDeviceId) {
            await BleClient.disconnect(this.connectedDeviceId);
            this.connectedDeviceId = null;
            this.updateStatus({ connected: false });
        }
    }

    public isConnected(): boolean {
        return this.status.connected;
    }

    public async getTemperature(): Promise<number> {
        return this.status.temperature || 25.0;
    }

    public async insertCartridge(): Promise<void> {
        this.updateStatus({ cartridgeInserted: true });
    }

    // Main Scan Logic
    public async performSpectralScan(): Promise<ScanResult> {
        if (!this.connectedDeviceId || !this.status.connected) throw new Error("Device not connected");

        console.log('Starting Spectral Scan...');

        // 1. Trigger Scan
        // Write 0x01 to Start Scan Char
        const data = new DataView(new ArrayBuffer(1));
        data.setUint8(0, 1);

        await BleClient.write(
            this.connectedDeviceId,
            SCAN_DATA_SERVICE_UUID,
            START_SCAN_CHAR_UUID,
            data
        );
        console.log('Scan command sent.');

        // 2. Wait for completion
        await new Promise(resolve => setTimeout(resolve, 3000));
        console.log('Waited 3s for scan completion.');

        // 3. Read Data
        // Logic: Read Loop

        let allBytes: number[] = [];
        let reading = true;
        let packetIndex = 0;
        let expectedSize = 0;

        let maxLoops = 100;

        while (reading && maxLoops > 0) {
            maxLoops--;
            try {
                const value = await BleClient.read(
                    this.connectedDeviceId,
                    SCAN_DATA_SERVICE_UUID,
                    READ_SCAN_DATA_CHAR_UUID
                );

                const rawBytes = new Uint8Array(value.buffer);
                console.log(`Read packet ${packetIndex}, len: ${rawBytes.length}`);

                if (rawBytes.length === 0) {
                    if (allBytes.length > 0 && allBytes.length >= expectedSize) {
                         reading = false;
                    } else {
                         await new Promise(r => setTimeout(r, 100));
                         continue;
                    }
                }

                const index = rawBytes[0];
                if (index === 0 && packetIndex === 0) {
                     expectedSize = value.getUint32(1, true); // Try little endian
                     if (expectedSize > 10000 || expectedSize === 0) {
                         expectedSize = value.getUint32(1, false);
                     }
                     console.log('Expected Data Size:', expectedSize);
                } else {
                     for (let i = 1; i < rawBytes.length; i++) {
                         allBytes.push(rawBytes[i]);
                     }
                }

                if (expectedSize > 0 && allBytes.length >= expectedSize) {
                    console.log('Read complete. Total bytes:', allBytes.length);
                    reading = false;
                }

                packetIndex++;

            } catch (e) {
                console.error('Error reading packet', e);
                reading = false;
            }
        }

        // 4. Reconstruct Intensities
        let intensities: number[] = [];
        if (allBytes.length >= 4) {
            const buffer = new Uint8Array(allBytes).buffer;
            const dataView = new DataView(buffer);
            const count = Math.floor(allBytes.length / 4);
            for (let i = 0; i < count; i++) {
                intensities.push(dataView.getInt32(i * 4, true));
            }
        }

        console.log('Parsed Intensities:', intensities.slice(0, 10), '...');

        const result: ScanResult = {
            intensities: intensities,
            wavelengths: [] // Will be filled by payload template mock
        };

        return result;
    }

    public formatScanData(data: any): any {
        return data;
    }

    public constructPayload(scanData: ScanResult, patientInfo: { patientId: string, age: number, gender: string }) {
        const payload = JSON.parse(JSON.stringify(ANALYSIS_PAYLOAD_TEMPLATE));

        payload.PatientId = patientInfo.patientId;
        payload.Age = patientInfo.age;
        payload.Sex = patientInfo.gender;
        payload.DateReading = new Date().toISOString();

        if (scanData.intensities && scanData.intensities.length > 0) {
            payload.Samples[0].Intensity = scanData.intensities;
        }

        payload.SerialNumber = this.status.serialNumber || "6100023";
        return payload;
    }
}

export const deviceService = new DeviceService();
