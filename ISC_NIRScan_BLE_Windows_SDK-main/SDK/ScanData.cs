using ISC_UUID_DEFINITION;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using static ISC_BLE_SDK.Config;

namespace ISC_BLE_SDK
{
    public static class ScanData
    {
        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct ScanResults
        {
            public UInt32 Header_Version;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 20)]
            public String Scan_Name;
            public ScanDateTime Datetime;
            public UInt16 system_temp_hundredths;
            public UInt16 detector_temp_hundredths;
            public UInt16 humidity_hundredths;
            public UInt16 lamp_pd;
            public UInt32 scanDataIndex;
            public CalibCoeffs calibCoeffs;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 8)]
            public String serial_number;
            public UInt16 adc_data_length;
            public byte black_pattern_first;
            public byte black_pattern_period;
            public byte pga;
            public SlewScanConfig slewScanConfig;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 864)]
            public double[] Wavelength;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 864)]
            public int[] intensity;
            public int length;
        }
        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct ScanDateTime
        {
            public byte Year;
            public byte Month;
            public byte Day;
            public byte Day_of_Week;
            public byte Hour;
            public byte Minute;
            public byte Second;
        }
        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct CalibCoeffs
        {
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
            public double[] ShiftVectorCoeffs;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
            public double[] PixelToWavelengthCoeffs;
        }

        public static string ScanResultFileName = "";

        public static List<double> WaveLength = new List<double>();
        public static List<double> Absorbance = new List<double>();
        public static List<double> Intensity = new List<double>();
        public static List<double> Reflectance = new List<double>();
        public static List<double> Reference = new List<double>();
    }
}