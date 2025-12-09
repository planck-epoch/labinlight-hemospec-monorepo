export interface BleDevice {
    deviceId: string;
    name: string;
    rssi: number;
}

class DeviceService {
    private connectedDevice: BleDevice | null = null;
    private isConnectedState = false;

    // Simulate scanning for devices
    public async scan(): Promise<BleDevice[]> {
        return new Promise(resolve => {
            setTimeout(() => {
                resolve([
                    { deviceId: 'dev_1', name: 'Hemospec Device A1', rssi: -60 },
                    { deviceId: 'dev_2', name: 'Hemospec Device B2', rssi: -75 },
                ]);
            }, 2000);
        });
    }

    // Simulate connection
    public async connect(deviceId: string): Promise<boolean> {
        return new Promise(resolve => {
            setTimeout(() => {
                this.connectedDevice = { deviceId, name: 'Hemospec Device', rssi: -60 };
                this.isConnectedState = true;
                resolve(true);
            }, 1500);
        });
    }

    public async disconnect(): Promise<void> {
        return new Promise(resolve => {
            setTimeout(() => {
                this.connectedDevice = null;
                this.isConnectedState = false;
                resolve();
            }, 500);
        });
    }

    // Simulate getting temperature from device
    public async getTemperature(): Promise<number> {
        return new Promise(resolve => {
            setTimeout(() => {
                // Return a temperature > 30 for success, or random for testing?
                // For this requirement, we assume it works if connected.
                resolve(32.5);
            }, 1000);
        });
    }

    public isConnected(): boolean {
        return this.isConnectedState;
    }

    public getConnectedDevice(): BleDevice | null {
        return this.connectedDevice;
    }
}

export const deviceService = new DeviceService();
