using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking.Connectivity;

namespace ISC_UUID_DEFINITION
{
    public static class Services
    {
        public static readonly string GenericAccess_UUID = "00001800-0000-1000-8000-00805F9B34FB";
        public static readonly string GenericAttribute_UUID = "00001801-0000-1000-8000-00805F9B34FB";
        public static readonly string Device_Information_Service_UUID = "0000180A-0000-1000-8000-00805F9B34FB";
        public static readonly string Battery_Service_UUID = "0000180F-0000-1000-8000-00805F9B34FB";
        public static readonly string General_Information_UUID = "53455201-444C-5020-4E49-52204E616E6F";
        public static readonly string Command_UUID = "53455202-444C-5020-4E49-52204E616E6F";
        public static readonly string Current_Time_UUID = "53455203-444C-5020-4E49-52204E616E6F";
        public static readonly string Calibration_UUID = "53455204-444C-5020-4E49-52204E616E6F";
        public static readonly string Configuration_UUID = "53455205-444C-5020-4E49-52204E616E6F";
        public static readonly string Scan_Data_UUID = "53455206-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Services_UUID_Data_Set = new Dictionary<string, string>
        {
            { "GenericAccess", "00001800-0000-1000-8000-00805F9B34FB" },
            { "GenericAttribute", "00001801-0000-1000-8000-00805F9B34FB" },
            { "Device_Information_Service", "0000180A-0000-1000-8000-00805F9B34FB" },
            { "Battery_Service", "0000180F-0000-1000-8000-00805F9B34FB" },
            { "General_Information", "53455201-444C-5020-4E49-52204E616E6F" },
            { "Command", "53455202-444C-5020-4E49-52204E616E6F" },
            { "Current_Time", "53455203-444C-5020-4E49-52204E616E6F" },
            { "Calibration", "53455204-444C-5020-4E49-52204E616E6F" },
            { "Configuration", "53455205-444C-5020-4E49-52204E616E6F" },
            { "Scan_Data", "53455206-444C-5020-4E49-52204E616E6F" }
        };

        public static readonly Dictionary<string, Dictionary<string, string>> AllIscServicesAndCharacteristicsSet = new Dictionary<string, Dictionary<string, string>>
        {
            { "General_Information", General_Information_Service.Characteristics_UUID_Data_Set },
            { "Command", Command_Service.Characteristics_UUID_Data_Set },
            { "Current_Time", Current_Time_Service.Characteristics_UUID_Data_Set },
            { "Calibration", Calibration_Service.Characteristics_UUID_Data_Set },
            { "Configuration", Configuration_Service.Characteristics_UUID_Data_Set },
            { "Scan_Data", Scan_Data_Service.Characteristics_UUID_Data_Set }
        };
    }

    public static class GenericAccess_Service
    {
        public static readonly string UUID = "00001801-0000-1000-8000-00805F9B34FB";

        public static readonly string DeviceName_CharUUID = "00002A00-0000-1000-8000-00805F9B34FB";
        public static readonly string Appearance_CharUUID = "00002A01-0000-1000-8000-00805F9B34FB";
        public static readonly string PeripheralPreferredConnectionParameters_CharUUID = "00002A04-0000-1000-8000-00805F9B34FB";
        public static readonly string BatteryLevel_CharUUID = "00002A19-0000-1000-8000-00805F9B34FB";
        public static readonly string SystemID_CharUUID = "00002A23-0000-1000-8000-00805F9B34FB";
        public static readonly string ModelNumberString_CharUUID = "00002A24-0000-1000-8000-00805F9B34FB";
        public static readonly string SerialNumberString_CharUUID = "00002A25-0000-1000-8000-00805F9B34FB";
        public static readonly string FirmwareRevisionString_CharUUID = "00002A26-0000-1000-8000-00805F9B34FB";
        public static readonly string HardwareRevisionString_CharUUID = "00002A27-0000-1000-8000-00805F9B34FB";
        public static readonly string SoftwareRevisionString_CharUUID = "00002A28-0000-1000-8000-00805F9B34FB";
        public static readonly string ManufacturerNameString_CharUUID = "00002A29-0000-1000-8000-00805F9B34FB";
        public static readonly string IEEE11073_CharUUID = "00002A2A-0000-1000-8000-00805F9B34FB";
        public static readonly string PnPID_CharUUID = "00002A50-0000-1000-8000-00805F9B34FB";


        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "DeviceName_CharUUID", "00002A00-0000-1000-8000-00805F9B34FB" },
            { "Appearance_CharUUID", "00002A01-0000-1000-8000-00805F9B34FB" },
            { "PeripheralPreferredConnectionParameters_CharUUID", "00002A04-0000-1000-8000-00805F9B34FB" },
            { "BatteryLevel_CharUUID", "00002A19-0000-1000-8000-00805F9B34FB" },
            { "SystemID_CharUUID", "00002A23-0000-1000-8000-00805F9B34FB" },
            { "ModelNumberString_CharUUID", "00002A24-0000-1000-8000-00805F9B34FB" },
            { "SerialNumberString_CharUUID", "00002A25-0000-1000-8000-00805F9B34FB" },
            { "FirmwareRevisionString_CharUUID", "00002A26-0000-1000-8000-00805F9B34FB" },
            { "HardwareRevisionString_CharUUID", "00002A27-0000-1000-8000-00805F9B34FB" },
            { "SoftwareRevisionString_CharUUID", "00002A28-0000-1000-8000-00805F9B34FB" },
            { "ManufacturerNameString_CharUUID", "00002A29-0000-1000-8000-00805F9B34FB" },
            { "IEEE11073_CharUUID", "00002A2A-0000-1000-8000-00805F9B34FB" },
            { "PnPID_CharUUID", "00002A50-0000-1000-8000-00805F9B34FB" }
        };
    }

    public static class General_Information_Service
    {
        public static readonly string UUID = "53455201-444C-5020-4E49-52204E616E6F";

        public static readonly string TemperatureMeasurement_CharUUID = "43484101-444C-5020-4E49-52204E616E6F";
        public static readonly string HumidityMeasurement_CharUUID = "43484102-444C-5020-4E49-52204E616E6F";
        public static readonly string DeviceStatus_CharUUID = "43484103-444C-5020-4E49-52204E616E6F";
        public static readonly string ErrorStatus_CharUUID = "43484104-444C-5020-4E49-52204E616E6F";
        public static readonly string TemperatureThreshold_CharUUID = "43484105-444C-5020-4E49-52204E616E6F";
        public static readonly string HumidityThreshold_CharUUID = "43484106-444C-5020-4E49-52204E616E6F";
        public static readonly string NumberOfUsageHours_CharUUID = "43484107-444C-5020-4E49-52204E616E6F";
        public static readonly string NumberOfBatteryRechargeCycles_CharUUID = "43484108-444C-5020-4E49-52204E616E6F";
        public static readonly string TotalLampHours_CharUUID = "43484109-444C-5020-4E49-52204E616E6F";
        public static readonly string ErrorLog_CharUUID = "4348410A-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "TemperatureMeasurement", "43484101-444C-5020-4E49-52204E616E6F" },
            { "HumidityMeasurement", "43484102-444C-5020-4E49-52204E616E6F" },
            { "DeviceStatus" ,"43484103-444C-5020-4E49-52204E616E6F" },
            { "ErrorStatus" ,"43484104-444C-5020-4E49-52204E616E6F" },
            { "TemperatureThreshold" ,"43484105-444C-5020-4E49-52204E616E6F" },
            { "HumidityThreshold" ,"43484106-444C-5020-4E49-52204E616E6F" },
            { "NumberOfUsageHours" ,"43484107-444C-5020-4E49-52204E616E6F" },
            { "NumberOfBatteryRechargeCycles" ,"43484108-444C-5020-4E49-52204E616E6F" },
            { "TotalLampHours" ,"43484109-444C-5020-4E49-52204E616E6F" },
            { "ErrorLog" ,"4348410A-444C-5020-4E49-52204E616E6F" }
        };
    }

    public static class Command_Service
    {
        public static readonly string UUID = "53455202-444C-5020-4E49-52204E616E6F";

        public static readonly string InternalCommand_CharUUID = "4348410B-444C-5020-4E49-52204E616E6F";
        public static readonly string ActivateState_CharUUID = "43484130-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnActivateState_CharUUID = "43484131-444C-5020-4E49-52204E616E6F";
        public static readonly string ReplaceBuiltInReference_CharUUID = "43484132-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "InternalCommand", "4348410B-444C-5020-4E49-52204E616E6F" },
            { "ActivateState", "43484130-444C-5020-4E49-52204E616E6F" },
            { "ReturnActivateState" ,"43484131-444C-5020-4E49-52204E616E6F" },
            { "ReplaceBuiltInReference" ,"43484132-444C-5020-4E49-52204E616E6F" }
        };
    }

    public static class Current_Time_Service
    {
        public static readonly string UUID = "53455203-444C-5020-4E49-52204E616E6F";

        public static readonly string CurrentTime_CharUUID = "4348410C-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "CurrentTime", "4348410C-444C-5020-4E49-52204E616E6F" }
        };
    }

    public static class Calibration_Service
    {
        public static readonly string UUID = "53455204-444C-5020-4E49-52204E616E6F";

        public static readonly string ReadSpectrumCalCoefficients_CharUUID = "4348410D-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnSpectrumCalCoefficients_CharUUID = "4348410E-444C-5020-4E49-52204E616E6F";
        public static readonly string ReadReferenceCalCoefficients_CharUUID = "4348410F-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnReferenceCalCoefficients_CharUUID = "43484110-444C-5020-4E49-52204E616E6F";
        public static readonly string ReadReferenceCalMatrix_CharUUID = "43484111-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnReferenceCalMatrix_CharUUID = "43484112-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "ReadSpectrumCalCoefficients", "4348410D-444C-5020-4E49-52204E616E6F" },
            { "ReturnSpectrumCalCoefficients", "4348410E-444C-5020-4E49-52204E616E6F" },
            { "ReadReferenceCalCoefficients" ,"4348410F-444C-5020-4E49-52204E616E6F" },
            { "ReturnReferenceCalCoefficients" ,"43484110-444C-5020-4E49-52204E616E6F" },
            { "ReadReferenceCalMatrix" ,"43484111-444C-5020-4E49-52204E616E6F" },
            { "ReturnReferenceCalMatrix" ,"43484112-444C-5020-4E49-52204E616E6F" }
        };
    }

    public static class Configuration_Service
    {
        public static readonly string UUID = "53455205-444C-5020-4E49-52204E616E6F";

        public static readonly string NumberOfStoredConfigurations_CharUUID = "43484113-444C-5020-4E49-52204E616E6F";
        public static readonly string RequestStoredConfigurationsList_CharUUID = "43484114-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnStoredConfigurationsList_CharUUID = "43484115-444C-5020-4E49-52204E616E6F";
        public static readonly string ReadScanConfigurationData_CharUUID = "43484116-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanConfigurationData_CharUUID = "43484117-444C-5020-4E49-52204E616E6F";
        public static readonly string ActiveScanConfiguration_CharUUID = "43484118-444C-5020-4E49-52204E616E6F";
        public static readonly string ReadCurrentScanConfigurationData_CharUUID = "43484140-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnCurrentScanConfigurationData_CharUUID = "43484141-444C-5020-4E49-52204E616E6F";
        public static readonly string WriteScanConfigurationData_CharUUID = "43484142-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnWriteScanConfigurationData_CharUUID = "43484143-444C-5020-4E49-52204E616E6F";
        public static readonly string SetScanLampModeData_CharUUID = "43484144-444C-5020-4E49-52204E616E6F";
        public static readonly string SetLampDelayTime_CharUUID = "43484145-444C-5020-4E49-52204E616E6F";
        public static readonly string SetPGADat_CharUUID = "43484146-444C-5020-4E49-52204E616E6F";
        public static readonly string SetNumberRepeatData_CharUUID = "43484147-444C-5020-4E49-52204E616E6F";
        public static readonly string ResetConfigurationData_CharUUID = "43484148-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "NumberOfStoredConfigurations", "43484113-444C-5020-4E49-52204E616E6F" },
            { "RequestStoredConfigurationsList", "43484114-444C-5020-4E49-52204E616E6F" },
            { "ReturnStoredConfigurationsList", "43484115-444C-5020-4E49-52204E616E6F" },
            { "ReadScanConfigurationData", "43484116-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanConfigurationData", "43484117-444C-5020-4E49-52204E616E6F" },
            { "ActiveScanConfiguration", "43484118-444C-5020-4E49-52204E616E6F" },
            { "ReadCurrentScanConfigurationData", "43484140-444C-5020-4E49-52204E616E6F" },
            { "ReturnCurrentScanConfigurationData", "43484141-444C-5020-4E49-52204E616E6F" },
            { "WriteScanConfigurationData", "43484142-444C-5020-4E49-52204E616E6F" },
            { "ReturnWriteScanConfigurationData", "43484143-444C-5020-4E49-52204E616E6F" },
            { "SetScanLampModeData", "43484144-444C-5020-4E49-52204E616E6F" },
            { "SetLampDelayTime", "43484145-444C-5020-4E49-52204E616E6F" },
            { "SetPGAData", "43484146-444C-5020-4E49-52204E616E6F" },
            { "SetNumberRepeatData", "43484147-444C-5020-4E49-52204E616E6F" },
            { "ResetConfigurationData", "43484148-444C-5020-4E49-52204E616E6F" }
        };
    }

    public static class Scan_Data_Service
    {
        public static readonly string UUID = "53455206-444C-5020-4E49-52204E616E6F";

        public static readonly string NumberOfStoredScans_CharUUID = "43484119-444C-5020-4E49-52204E616E6F";
        public static readonly string RequestStoredScanIndicesList_CharUUID = "4348411A-444C-5020-4E49-52204E616E6F";
        public static readonly string GetStoredScanIndicesList_CharUUID = "4348411B-444C-5020-4E49-52204E616E6F";
        public static readonly string SetScanNameStub_CharUUID = "4348411C-444C-5020-4E49-52204E616E6F";
        public static readonly string StartScan_CharUUID = "4348411D-444C-5020-4E49-52204E616E6F";
        public static readonly string ClearScan_CharUUID = "4348411E-444C-5020-4E49-52204E616E6F";
        public static readonly string GetScanName_CharUUID = "4348411F-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanName_CharUUID = "43484120-444C-5020-4E49-52204E616E6F";
        public static readonly string GetScanType_CharUUID = "43484121-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanType_CharUUID = "43484122-444C-5020-4E49-52204E616E6F";
        public static readonly string GetScanTimestamp_CharUUID = "43484123-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanTimestamp_CharUUID = "43484124-444C-5020-4E49-52204E616E6F";
        public static readonly string GetScanBlobVersion_CharUUID = "43484125-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanBlobVersion_CharUUID = "43484126-444C-5020-4E49-52204E616E6F";
        public static readonly string GetScanData_CharUUID = "43484127-444C-5020-4E49-52204E616E6F";
        public static readonly string ReturnScanData_CharUUID = "43484128-444C-5020-4E49-52204E616E6F";

        public static readonly Dictionary<string, string> Characteristics_UUID_Data_Set = new Dictionary<string, string>
        {
            { "NumberOfStoredScans", "43484119-444C-5020-4E49-52204E616E6F" },
            { "RequestStoredScanIndicesList", "4348411A-444C-5020-4E49-52204E616E6F" },
            { "GetStoredScanIndicesList", "4348411B-444C-5020-4E49-52204E616E6F" },
            { "SetScanNameStub", "4348411C-444C-5020-4E49-52204E616E6F" },
            { "StartScan", "4348411D-444C-5020-4E49-52204E616E6F" },
            { "ClearScan", "4348411E-444C-5020-4E49-52204E616E6F" },
            { "GetScanName", "4348411F-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanName", "43484120-444C-5020-4E49-52204E616E6F" },
            { "GetScanType", "43484121-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanType", "43484122-444C-5020-4E49-52204E616E6F" },
            { "GetScanTimestamp", "43484123-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanTimestamp", "43484124-444C-5020-4E49-52204E616E6F" },
            { "GetScanBlobVersion", "43484125-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanBlobVersion", "43484126-444C-5020-4E49-52204E616E6F" },
            { "GetScanData", "43484127-444C-5020-4E49-52204E616E6F" },
            { "ReturnScanData", "43484128-444C-5020-4E49-52204E616E6F" }
        };
    }
}
