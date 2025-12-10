import { deviceService } from './services/DeviceService';

async function verify() {
    console.log("Starting verification...");

    const mockScanResult = {
        wavelengths: [900, 905, 910],
        intensities: [1000, 2000, 3000],
        length: 3
    };

    console.log("Mock Scan Result:", mockScanResult);

    const payload = deviceService.formatScanData(mockScanResult);

    console.log("Formatted Payload:");
    console.log(JSON.stringify(payload, null, 2));

    // Basic assertion checks
    if (payload.Samples[0].Intensity[0] !== 1000) throw new Error("Intensity mismatch");
    if (payload.Samples[0].WaveLength[0] !== 900) throw new Error("WaveLength mismatch");
    if (payload.SerialNumber !== "0000000") throw new Error("SerialNumber default mismatch");

    // Check deep nesting existence
    if (!payload.Samples[0].ReferenceScanConfigData.Head) throw new Error("Nested Head missing");
    if (payload.Samples[0].ReferenceScanConfigData.Head.ScanConfig_serial_number !== "6100023") throw new Error("Head serial number mismatch");

    console.log("Verification Successful!");
}

verify().catch(e => {
    console.error("Verification Failed:", e);
    process.exit(1);
});
