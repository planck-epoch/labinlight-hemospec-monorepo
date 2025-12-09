import { BleClient } from '@capacitor-community/bluetooth-le';

// This service will wrap the community BLE plugin or your custom plugin.
// It simplifies the interaction for the rest of the app.

class BLEService {
  async initialize() {
    await BleClient.initialize();
  }

  async scanForHemospecDevice(callback: (device: any) => void) {
    await BleClient.requestLEScan({}, (result) => {
      // Filter for the Hemospec device by name, similar to your Swift code.
      if (result.device.name?.includes("NIR-")) {
        callback(result.device);
      }
    });

    // Stop scanning after a timeout.
    setTimeout(async () => {
      await BleClient.stopLEScan();
    }, 10000);
  }

  async connect(deviceId: string) {
    await BleClient.connect(deviceId);
  }

  async disconnect(deviceId: string) {
    await BleClient.disconnect(deviceId);
  }
  
  // You would add methods for reading/writing characteristics here.
}

export const bleService = new BLEService();