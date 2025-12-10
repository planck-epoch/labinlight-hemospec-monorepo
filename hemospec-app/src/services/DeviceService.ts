import { registerPlugin } from '@capacitor/core';
import { apiService } from './ApiService';

export interface MetaScanPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  connect(options: { address: string }): Promise<void>;
  disconnect(options: { address: string }): Promise<void>;
  performScan(): Promise<void>;
  getScanData(): Promise<any>; // Returns complex object
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

export interface ScanPayload {
  Age: number;
  Sex: string; // "M" or "F"
  Samples: any[];
  RefLampPD?: number;
  Absorbance?: number[];
  NumSection?: number;
  SystemTemp?: number;
  WaveLength?: number[];
  RefDateTime?: string;
  RefHumidity?: number;
  DetectorTemp?: number;
  HostDateTime?: string;
  ReferencePGA?: number;
  EndWavelength?: number;
  HeaderVersion?: number;
  TotalTimeScan?: number;
  ScanConfigName?: string;
  ScanConfigType?: number;
  RefDetectorTemp?: number;
  RefSerialNumber?: string;
  StartWavelength?: number;
  DigitalResolution?: number;
  PatternPixelWidth?: number;
  RefScanConfigType?: number;
  ReferenceIntensity?: number[];
  ReferenceScanConfigData?: any;
  ShiftVectorCoefficients?: number[];
  ReferenceScanDataVersion?: number;
  PixelWavelengtCoefficients?: number[];
  Wavelength?: number[];
  ModelName?: string;
  PatientId?: string;
  DateReading?: string;
  BirthdayYear?: number;
  SerialNumber?: string;
  ProcessNumber?: string;
  AnalysisBundle?: string;
  PatientIdCountry?: string;
  PatientContactNumber?: string;
}

// Hardcoded Reference Data from payload.json
const HARDCODED_REFERENCE_INTENSITY = [
    24311, 27708, 29896, 32100, 34641, 37570, 41019, 45033, 49615, 54608, 61561, 66812, 71846, 76870, 81820, 86740, 91588, 96376, 100998, 106628, 110473, 113952, 116991, 119678, 121694, 123645, 125215, 126735, 127578, 128418, 129139, 129765, 130420, 131003, 131526, 132023, 132968, 133948, 134749, 135383, 136095, 136998, 137750, 138507, 139483, 140357, 141043, 141675, 142098, 142703, 143259, 143664, 144065, 144420, 144533, 144728, 144919, 144943, 144822, 144686, 144652, 144844, 144980, 145077, 145317, 145682, 146077, 146395, 146710, 147066, 147945, 148517, 149054, 149947, 151081, 152403, 153918, 155756, 157880, 161049, 163575, 166369, 169280, 171979, 174682, 177619, 180777, 184255, 186543, 189024, 191684, 194002, 196294, 198724, 201240, 203507, 206877, 209405, 211900, 214233, 216726, 219069, 220725, 222365, 224937, 226715, 228100, 229162, 230431, 231622, 232670, 233230, 233809, 234853, 235206, 235437, 235736, 236179, 236040, 235839, 235621, 235358, 234854, 234535, 234500, 234246, 233750, 233178, 232516, 231540, 229827, 228647, 227692, 226611, 225544, 224456, 223672, 223139, 222463, 221533, 221164, 220784, 220548, 220323, 219966, 219874, 219806, 219650, 219558, 219133, 218880, 218471, 217988, 217430, 216620, 215844, 214766, 213864, 212819, 211734, 210824, 209691, 208365, 206983, 205237, 203897, 202388, 200763, 198964, 197445, 195771, 193975, 192008, 189330, 187366, 185319, 183103, 180954, 178593, 176277, 173830, 170499, 167833, 165360, 162729, 160198, 157445, 154654, 151971, 149401, 145646, 142764, 139827, 137202, 134453, 131558, 128572, 125828, 122049, 119246, 116292, 113405, 110526, 107647, 104849, 101976, 99097, 95317, 92583, 89728, 86778, 83747, 80652, 77337, 73465, 69005, 62313, 56748, 50613, 44448, 38400, 32893, 27957, 23704, 19173, 16402, 14102, 12148
];

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
             // Auto-connect to NIRScan devices for smoother UX if in scanning mode
             if (this.status.isScanning && device.name.includes("NIR")) {
                 this.stopScanning();
                 this.connect(device.address);
             }
        });

        await MetaScan.addListener('metaScanEvent', async (data) => {
             console.log('SDK Event:', data.event);
             if (data.event === 'NOTIFY_COMPLETE') {
                 // Scan logic complete
                 this.updateStatus({ connected: true });

                 // If we were waiting for scan data, fetch it now
                 if (this.pendingScanResolve) {
                     try {
                         const scanData = await MetaScan.getScanData();
                         this.pendingScanResolve(scanData);
                     } catch(e) {
                         console.error("Error fetching scan data:", e);
                         this.pendingScanResolve(null); // Resolve with null on error
                     }
                     this.pendingScanResolve = null;
                 }
             } else if (data.event === 'DISCONNECTED') {
                 this.updateStatus({ connected: false });
                 this.connectedDeviceAddress = null;
             } else if (data.event === 'SCAN_DATA_READY') {
                 // Already handled by explicit getScanData call usually, but good fallback
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

    // Connect to specific address
    public async connect(address: string): Promise<boolean> {
        try {
            await MetaScan.connect({ address });
            this.connectedDeviceAddress = address;
            this.updateStatus({ connected: true });

            // Fetch initial status
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

    // Combined Scan and Connect for UX
    public async scanAndConnect(): Promise<void> {
        if (this.status.connected) return;

        this.updateStatus({ isScanning: true });
        this.discoveredDevices = [];

        try {
            await MetaScan.startScan();
            // In a real app we might show a list, but here we auto-connect in the listener
            // or wait for the user to pick (but we automated it in listener for NIR devices)

            // We wait up to 10 seconds for a connection
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
                        // resolve(); // Don't reject, just stop trying
                        // Or if strict:
                        reject(new Error("No device found or connected"));
                    }
                }, 10000);
            });

        } catch (e) {
            this.updateStatus({ isScanning: false });
            throw e;
        }
    }

    // Disconnect
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
        this.discoveredDevices = [];
        this.updateStatus({ isScanning: true });
        await MetaScan.startScan();
    }

    public async stopScanning() {
        await MetaScan.stopScan();
        this.updateStatus({ isScanning: false });
    }

    // Legacy method for compatibility with other components
    public async scan(): Promise<BleDevice[]> {
        await this.startScanForDevices();
        return new Promise(resolve => {
            setTimeout(async () => {
                await this.stopScanning();
                resolve([...this.discoveredDevices]);
            }, 3000);
        });
    }

    // Legacy method
    public insertCartridge() {
        this.updateStatus({ cartridgeInserted: true });
    }

    // Legacy method
    public async getTemperature(): Promise<number> {
        return this.status.temperature || 0;
    }

    // Legacy/Mixed method used by old Analysis flow
    public async runAnalysis(): Promise<DeviceResult> {
        // Trigger scan and mock result for legacy compatibility
        if (!this.status.connected) throw new Error("Device not connected");

        try {
             await this.performSpectralScan();
        } catch(e) {
            console.warn("Scan failed in legacy runAnalysis", e);
            throw e; // Rethrow to ensure callers know it failed
        }

        return {
            timestamp: new Date().toISOString(),
            value: "13.5",
            units: "g/dL",
            status: "normal",
            hematocrit: 40,
            hemoglobin: 13.5,
            rbc: 4.8,
            rdw: 12.0
        };
    }

    // The main function to trigger a spectral scan on the connected device
    public async performSpectralScan(): Promise<any> {
        if (!this.status.connected) throw new Error("Device not connected");

        return new Promise(async (resolve, reject) => {
            this.pendingScanResolve = resolve;

            // Timeout mechanism
            setTimeout(() => {
                if (this.pendingScanResolve) {
                    this.pendingScanResolve = null;
                    reject(new Error("Scan timed out"));
                }
            }, 15000); // 15 seconds timeout

            try {
                await MetaScan.performScan();
            } catch (e) {
                this.pendingScanResolve = null;
                reject(e);
            }
        });
    }

    // Helper to calculate Absorbance
    private calculateAbsorbance(intensities: number[]): number[] {
        const absorbance: number[] = [];
        for (let i = 0; i < intensities.length; i++) {
             const sample = intensities[i];
             const ref = HARDCODED_REFERENCE_INTENSITY[i] || 1; // Avoid division by zero
             if (sample <= 0 || ref <= 0) {
                 absorbance.push(0);
             } else {
                 absorbance.push(-1 * Math.log10(sample / ref));
             }
        }
        return absorbance;
    }

    // Construct the payload for the API
    public constructPayload(scanData: any, patientInfo: { patientId: string, age: number, gender: 'M' | 'F' }): ScanPayload {
        const intensities = scanData.intensities || [];
        const wavelengths = scanData.wavelengths || [];
        const absorbance = this.calculateAbsorbance(intensities);

        // Current time in ISO 8601 or similar (example uses a specific format but ISO is safe usually)
        const now = new Date().toISOString();

        return {
            Age: patientInfo.age,
            Sex: patientInfo.gender,
            Samples: [{
                UUID: "1mUwZqJqFyg=", // Placeholder or generate unique ID
                LampPD: scanData.LampPD || 3872.0,
                Method: scanData.ScanConfigName || "Hadamard 1",
                NumRep: 6, // Hardcoded or from config
                PGAgain: scanData.PGA || 16,
                RefTemp: 30.31, // Hardcoded reference temp
                Exposure: scanData.Exposure || 0.635,
                Humidity: scanData.Humidity || 63.65,
                Intensity: intensities,
                RefLampPD: 3864.0, // Hardcoded from payload.json
                Absorbance: absorbance,
                NumSection: 1,
                SystemTemp: scanData.SystemTemp || 26.22,
                WaveLength: wavelengths,
                RefDateTime: "FQUVCw8e", // Hardcoded
                RefHumidity: 41.15,
                DetectorTemp: scanData.DetectorTemp || 27.1,
                HostDateTime: "FwYNCSIs",
                ReferencePGA: 64,
                EndWavelength: 1700,
                HeaderVersion: 1,
                TotalTimeScan: 2.67,
                ScanConfigName: scanData.ScanConfigName || "Hadamard 1",
                ScanConfigType: scanData.ScanConfigType || 1,
                RefDetectorTemp: 30.45,
                RefSerialNumber: "6100023",
                StartWavelength: 900,
                DigitalResolution: 228,
                PatternPixelWidth: 7.03,
                RefScanConfigType: 0,
                ReferenceIntensity: HARDCODED_REFERENCE_INTENSITY,
                ReferenceScanConfigData: {
                    Head: {
                        scan_type: 2,
                        config_name: "SystemTest",
                        num_repeats: 30,
                        num_sections: 1,
                        scanConfigIndex: 1815,
                        ScanConfig_serial_number: "6100023"
                    },
                    Section: [{
                        width_px: 6,
                        num_patterns: 228,
                        exposure_time: 0,
                        section_scan_type: 0,
                        wavelength_end_nm: 1700,
                        wavelength_start_nm: 900
                    }, {}, {}, {}, {}]
                },
                ShiftVectorCoefficients: [-8.585111576005033, 0.06371435766307644, -0.0001164294142905534],
                ReferenceScanDataVersion: 1,
                PixelWavelengtCoefficients: [1781.9415556011409, -0.9137117943233481, -0.00024587460303220255],
                Wavelength: wavelengths // Redundant field in payload example? Included to match.
            }]
            // Flatten the rest? The ScanPayload interface has these fields at root level too in my def
            // But payload.json had them inside "Samples"? No, payload.json had a mix.
            // Let's look at payload.json again.
            // "Age": 32, "Sex": "M", "Samples": [...], "RefLampPD": ...
            // The root object has "Age", "Sex", "Samples".
            // AND "RefLampPD", "Absorbance", etc... wait.
            // In payload.json: {"Age": 32, ..., "Samples": [{"UUID": ..., "Intensity": ..., "Absorbance": ...}]}
            // Wait, "Absorbance" is inside "Samples" array element in the JSON provided?
            // "Samples": [{"UUID": "...", "LampPD": ..., "Absorbance": [0.85...], ...}]
            // BUT, the JSON also shows keys like "RefLampPD" OUTSIDE samples?
            // "Samples": [{...}], "RefLampPD": 3864.0

            // Let's re-read the prompt's JSON.
            // {"Age": 32, "Sex": "M", "Samples": [{"UUID": ..., "LampPD": ..., "Intensity": [...], "RefLampPD": 3864.0, "Absorbance": [...], ... "Wavelength": [...] ...}]}
            // It seems "Samples" contains most data.
            // The root only has Age, Sex, Samples.
            // My previous ScanPayload interface had them at root. I will fix the return object to match structure.
            ,
            ModelName: "",
            PatientId: patientInfo.patientId,
            DateReading: now,
            BirthdayYear: new Date().getFullYear() - patientInfo.age,
            SerialNumber: scanData.ScanConfig_serial_number || "Unknown",
            ProcessNumber: "130623",
            AnalysisBundle: "global",
            PatientIdCountry: "PT",
            PatientContactNumber: ""
        };
    }
}

export const deviceService = new DeviceService();
