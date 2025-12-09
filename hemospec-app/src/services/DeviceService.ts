export interface DeviceResult {
  hematocrit: number;
  hemoglobin: number;
  rdw: number;
  rbc: number;
  timestamp: string;
}

export interface DeviceStatus {
  connected: boolean;
  batteryLevel: number; // 0-100
  isScanning: boolean;
  cartridgeInserted: boolean;
}

class DeviceService {
  private static instance: DeviceService;
  private status: DeviceStatus = {
    connected: false,
    batteryLevel: 85,
    isScanning: false,
    cartridgeInserted: false,
  };

  private listeners: ((status: DeviceStatus) => void)[] = [];

  private constructor() {}

  public static getInstance(): DeviceService {
    if (!DeviceService.instance) {
      DeviceService.instance = new DeviceService();
    }
    return DeviceService.instance;
  }

  public subscribe(callback: (status: DeviceStatus) => void) {
    this.listeners.push(callback);
    callback(this.status);
    return () => {
      this.listeners = this.listeners.filter((l) => l !== callback);
    };
  }

  private updateStatus(newStatus: Partial<DeviceStatus>) {
    this.status = { ...this.status, ...newStatus };
    this.listeners.forEach((l) => l(this.status));
  }

  public async scanAndConnect(): Promise<boolean> {
    this.updateStatus({ isScanning: true });

    // Simulate scanning delay
    await new Promise((resolve) => setTimeout(resolve, 2000));

    // Simulate 90% success rate
    const success = Math.random() > 0.1;

    if (success) {
      this.updateStatus({
        isScanning: false,
        connected: true,
        batteryLevel: Math.floor(Math.random() * 30) + 70 // Random level 70-100
      });
      return true;
    } else {
      this.updateStatus({ isScanning: false, connected: false });
      throw new Error("Device not found");
    }
  }

  public async disconnect() {
    this.updateStatus({ connected: false, cartridgeInserted: false });
  }

  public insertCartridge() {
      // Simulate physical action
      this.updateStatus({ cartridgeInserted: true });
  }

  public async runAnalysis(): Promise<DeviceResult> {
    if (!this.status.connected) throw new Error("Device not connected");
    if (!this.status.cartridgeInserted) throw new Error("No cartridge detected");

    // Simulate analysis time (e.g., 5 seconds)
    await new Promise((resolve) => setTimeout(resolve, 5000));

    // Return mock results
    return {
      hematocrit: 42.5,
      hemoglobin: 14.2,
      rdw: 13.5,
      rbc: 4.8,
      timestamp: new Date().toISOString()
    };
  }
}

export const deviceService = DeviceService.getInstance();
