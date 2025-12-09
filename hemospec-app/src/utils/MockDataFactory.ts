export const createMockAnalyzeData = (patientId: string, age: number, sex: string) => {
    return {
        Age: age,
        Sex: sex,
        Samples: [
            {
                UUID: "mock-uuid-" + Date.now(),
                LampPD: 100,
                Method: "mock",
                NumRep: 1,
                PGAgain: 1,
                RefTemp: 30,
                Exposure: 100,
                Humidity: 50,
                Intensity: Array(10).fill(100),
                RefLampPD: 100,
                Absorbance: Array(10).fill(0.5),
                NumSection: 1,
                SystemTemp: 35,
                WaveLength: Array(10).fill(400),
                RefDateTime: new Date().toISOString(),
                RefHumidity: 50,
                DetectorTemp: 30,
                HostDateTime: new Date().toISOString(),
                ReferencePGA: 1,
                EndWavelength: 800,
                HeaderVersion: 1,
                TotalTimeScan: 10,
                ScanConfigName: "config",
                ScanConfigType: 1,
                RefDetectorTemp: 30,
                RefSerialNumber: "ref-123",
                StartWavelength: 400,
                DigitalResolution: 1,
                PatternPixelWidth: 1,
                RefScanConfigType: 1,
                ReferenceIntensity: Array(10).fill(100),
                ReferenceScanConfigData: {
                    Head: {
                        scan_type: 1,
                        config_name: "config",
                        num_repeats: 1,
                        num_sections: 1,
                        scanConfigIndex: 1,
                        ScanConfig_serial_number: "123"
                    },
                    Section: [
                         {
                            width_px: 100,
                            num_patterns: 1,
                            exposure_time: 100,
                            section_scan_type: 1,
                            wavelength_end_nm: 800,
                            wavelength_start_nm: 400
                         }
                    ]
                },
                ShiftVectorCoefficients: [1, 0],
                ReferenceScanDataVersion: 1,
                PixelWavelengtCoefficients: [1, 0],
                Wavelength: Array(10).fill(400)
            }
        ],
        ModelName: "MockModel",
        PatientId: patientId,
        DateReading: new Date().toISOString(),
        BirthdayYear: new Date().getFullYear() - age,
        SerialNumber: "DEV-123",
        ProcessNumber: "PROC-1",
        AnalysisBundle: "bundle",
        PatientIdCountry: "US",
        PatientContactNumber: "555-0123"
    };
};
