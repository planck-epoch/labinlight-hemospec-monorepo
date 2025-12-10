import { describe, it, expect, vi, beforeEach } from 'vitest';

const mocks = vi.hoisted(() => ({
    addListener: vi.fn(),
    startScan: vi.fn().mockResolvedValue(undefined),
    stopScan: vi.fn().mockResolvedValue(undefined),
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn().mockResolvedValue(undefined),
    getDeviceStatus: vi.fn().mockResolvedValue({ battery: 100, temp: 25, serial: "TEST" }),
    performScan: vi.fn().mockResolvedValue(undefined)
}));

vi.mock('@capacitor/core', () => ({
    registerPlugin: () => mocks
}));

import { deviceService } from './DeviceService';

describe('DeviceService', () => {
    beforeEach(async () => {
        vi.clearAllMocks();
        await deviceService.disconnect();
    });

    it('should start scanning', async () => {
        const spy = vi.fn();
        deviceService.subscribe(spy);

        const connectPromise = deviceService.scanAndConnect();
        expect(mocks.startScan).toHaveBeenCalled();

        try {
             await connectPromise;
        } catch(e) {
            // expected timeout
        }
    }, 15000); // Increased timeout

    it('should run analysis only when connected', async () => {
        await deviceService.disconnect();
        await expect(deviceService.runAnalysis()).rejects.toThrow('Device not connected');
    });
});
