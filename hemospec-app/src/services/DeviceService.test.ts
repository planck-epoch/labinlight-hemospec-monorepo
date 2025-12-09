import { describe, it, expect, vi } from 'vitest';
import { deviceService } from './DeviceService';

describe('DeviceService', () => {
    it('should start scanning and update status', async () => {
        const spy = vi.fn();
        deviceService.subscribe(spy);

        // Initial state
        expect(spy).toHaveBeenCalledWith(expect.objectContaining({ isScanning: false, connected: false }));

        // Start scan (mocked)
        const connectPromise = deviceService.scanAndConnect();
        expect(spy).toHaveBeenCalledWith(expect.objectContaining({ isScanning: true }));

        try {
            await connectPromise;
        } catch (e) {
            // It might fail randomly due to the 90% success rate mock
        }

        expect(spy).toHaveBeenCalledWith(expect.objectContaining({ isScanning: false }));
    });

    it('should run analysis only when connected and cartridge inserted', async () => {
        // Reset state
        await deviceService.disconnect();

        // Expect failure
        await expect(deviceService.runAnalysis()).rejects.toThrow('Device not connected');
    });
});
