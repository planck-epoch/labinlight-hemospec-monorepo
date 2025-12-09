using ISC_UUID_DEFINITION;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.Devices.Bluetooth.GenericAttributeProfile;

namespace ISC_BLE_SDK
{
    public static class Config
    {
        private const Int32 SLEW_SCAN_MAX_SECTIONS = 5;

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct SlewScanSection
        {
            public Byte section_scan_type;  
            public Byte width_px;  
            public UInt16 wavelength_start_nm;  
            public UInt16 wavelength_end_nm;  
            public UInt16 num_patterns;  
            public UInt16 exposure_time;  
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct SlewScanConfigHead
        {
            public Byte scan_type;  
            public UInt16 scanConfigIndex;  
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 8)]
            public String ScanConfig_serial_number;  
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 40)]
            public String config_name;  
            public UInt16 num_repeats;  
            public Byte num_sections;  
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Ansi)]
        public struct SlewScanConfig
        {
            public SlewScanConfigHead head;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = SLEW_SCAN_MAX_SECTIONS)]
            public SlewScanSection[] section; 
        }

        public enum SCAN_TYPE
        {
            COLUMN,
            HADAMARD,
            SLEW
        }

        public static int NumStoredConfig { get; set; }
        public static List<Byte[]> ScanConfigIndexList { get; set; }
        public static List<SlewScanConfig> ScanConfigList { get; set; }
        public static Byte[] ActiveScanIndex { get; set; }

        public static bool SetValue(string uuid, string value)
        {
            if (uuid != null)
            {
                string charUUID = uuid.ToUpper();
                if (charUUID == Configuration_Service.ActiveScanConfiguration_CharUUID)
                {
                    ActiveScanIndex = Helper.StringToByteArray(value);
                }
                else if (charUUID == Configuration_Service.NumberOfStoredConfigurations_CharUUID)
                {
                    NumStoredConfig = Helper.ByteStringToInt16(value);
                }
            }
            return true;
        }
    }
}