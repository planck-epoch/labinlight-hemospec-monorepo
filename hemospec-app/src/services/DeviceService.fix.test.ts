import { describe, it, expect, vi, beforeEach } from 'vitest';

const mocks = vi.hoisted(() => ({
    addListener: vi.fn(),
    connect: vi.fn().mockResolvedValue(undefined),
    getDeviceStatus: vi.fn().mockResolvedValue({ battery: 80, temp: 25, serial: "12345" }),
    getScanData: vi.fn(),
    startScan: vi.fn(),
    stopScan: vi.fn(),
    disconnect: vi.fn(),
    performScan: vi.fn()
}));

vi.mock('@capacitor/core', () => {
    return {
        registerPlugin: (name: string) => {
            console.log(`[Mock] registerPlugin called for ${name}`);
            if (name === 'MetaScan') {
                return mocks;
            }
            return {};
        },
        Capacitor: {
            getPlatform: () => 'android'
        }
    };
});

// Mock AndroidPermissions
vi.mock('@awesome-cordova-plugins/android-permissions', () => {
    return {
        AndroidPermissions: class {
            requestPermissions() {
                console.log('[Mock] requestPermissions called');
                return Promise.resolve({ hasPermission: true });
            }
            checkPermission() {
                return Promise.resolve({ hasPermission: true });
            }
        }
    };
});

import { deviceService } from './DeviceService';

describe('DeviceService Race Condition Fix', () => {
    let eventCallback: (data: any) => void;

    it('should wait for NOTIFY_COMPLETE before marking as connected', async () => {
        // Wait for addListener to be called if it hasn't been already
        await new Promise(resolve => setTimeout(resolve, 500));

        const calls = mocks.addListener.mock.calls;
        const metaScanEventCall = calls.find((call: any[]) => call[0] === 'metaScanEvent');
        
        expect(metaScanEventCall, 'addListener for metaScanEvent should have been called').toBeDefined();
        eventCallback = metaScanEventCall[1];

        // 2. Call connect and await it
        await deviceService.connect("XX:XX:XX:XX:XX:XX");
        
        // 3. Check that connect called the plugin
        expect(mocks.connect).toHaveBeenCalledWith({ address: "XX:XX:XX:XX:XX:XX" });

        // 4. Verify status is NOT connected yet (because we wait for NOTIFY_COMPLETE)
        expect(deviceService.isConnected()).toBe(false);

        // 5. Emit NOTIFY_COMPLETE
        await eventCallback({ event: 'NOTIFY_COMPLETE' });

        // 6. Verify status IS connected now
        expect(deviceService.isConnected()).toBe(true);
        
        // 7. Verify getDeviceStatus was called
        expect(mocks.getDeviceStatus).toHaveBeenCalled();
    });

    it('should resolve scan when SCAN_DATA_READY is received', async () => {
        // Assume connected
        // deviceService['status'].connected = true; // Hack to set private prop? No, use notify complete.
        // We can reuse the connected state from previous test if we don't reset modules, 
        // but vitest isolates tests usually? No, state is persistent in singleton.
        
        if (!deviceService.isConnected()) {
             // Re-connect flow
             await deviceService.connect("XX:XX:XX:XX:XX:XX");
             const calls = mocks.addListener.mock.calls;
             const metaScanEventCall = calls.find((call: any[]) => call[0] === 'metaScanEvent');
             eventCallback = metaScanEventCall[1];
             await eventCallback({ event: 'NOTIFY_COMPLETE' });
        }

        const scanPromise = deviceService.scan();
        
        // Should call performScan
        expect(mocks.performScan).toHaveBeenCalled();

        // Should NOT resolve yet
        let resolved = false;
        scanPromise.then(() => resolved = true);
        await new Promise(resolve => setTimeout(resolve, 100));
        expect(resolved).toBe(false);

        // Emit SCAN_DATA_READY
        mocks.getScanData.mockResolvedValue({
            wavelengths: [400, 500],
            intensities: [100, 200]
        });
        await eventCallback({ event: 'SCAN_DATA_READY' });

        // Now it should resolve
        const result = await scanPromise;
        expect(result).toBeDefined();
        expect(result.intensities).toEqual([100, 200]);
        expect(mocks.getScanData).toHaveBeenCalled();
    });
});
