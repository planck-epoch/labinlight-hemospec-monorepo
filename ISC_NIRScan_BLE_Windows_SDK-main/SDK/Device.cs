using ISC_UUID_DEFINITION;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth.GenericAttributeProfile;

namespace ISC_BLE_SDK
{
    public static class Device
    {
        #region General Information
        public static string Advertising_Name { get; set; }

        public static string MFG_Name { get; set; }
        public static string Model_Name { get; set; }
        public static string Serial_Number { get; set; }
        public static string UUID { get; set; }
        public static string Hardware_Rev { get; set; }
        public static string Tiva_Rev { get; set; }
        public static string SpecLib_Rev { get; set; }

        public static string Batt_Lv { get; set; }
        public static string Lamp_Usage { get; set; }

        public static string Temp { get; set; }
        public static string Humi { get; set; }

        public static string ActivateState { get; set; }

        public static Byte[] DeviceStatusBytes { get; set; }
        public static Byte[] ErrorStatusBytes { get; set; }

        public static byte[] RefCalCoefficients { get; set; }
        public static byte[] RefCalMatrix { get; set; }
        public static byte[] SpecCalCoefficients { get; set; }

        public static bool SetValue(string uuid, string value)
        {
            if (uuid != null)
            {
                string charUUID = uuid.ToUpper();
                Guid uuid_guid = new Guid(charUUID);
                if (DisplayHelpers.IsSigDefinedUuid(uuid_guid))
                {
                    GattNativeCharacteristicUuid characteristicName;
                    if (Enum.TryParse(Utilities.ConvertUuidToShortId(uuid_guid).ToString(), out characteristicName))
                    {
                        string uuidLongStr = uuid_guid.ToString().ToUpper();
                        string uuidShortStr = Utilities.ConvertUuidToShortId(uuid_guid).ToString();
                        string charName = characteristicName.ToString();
                        string debugStr = charName + ", " + uuidLongStr + ", " + uuidShortStr;
                        switch (charName)
                        {
                            case "DeviceName":
                                Advertising_Name = value;
                                break;
                            case "ManufacturerNameString":
                                MFG_Name = value;
                                break;
                            case "ModelNumberString":
                                Model_Name = value;
                                break;
                            case "SerialNumberString":
                                Serial_Number = value;
                                break;
                            case "HardwareRevisionString":
                                Hardware_Rev = value;
                                break;
                            case "FirmwareRevisionString":
                                Tiva_Rev = value;
                                break;
                            case "SoftwareRevisionString":
                                SpecLib_Rev = value;
                                break;
                            case "SystemID":
                                UUID = value;
                                break;
                            case "BatteryLevel":
                                Batt_Lv = value;
                                break;
                        }
                    }
                }
                else if (charUUID == Command_Service.ActivateState_CharUUID)
                {
                    ActivateState = value;
                }
                else if (charUUID == General_Information_Service.TemperatureMeasurement_CharUUID)
                {
                    Temp = value;
                }
                else if (charUUID == General_Information_Service.HumidityMeasurement_CharUUID)
                {
                    Humi = value;
                }
                else if (charUUID == General_Information_Service.TotalLampHours_CharUUID)
                {
                    Lamp_Usage = value;
                }
            }
            return true;
        }
        #endregion

        #region Error Status
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct NNO_error_codes_struct
        {
            public sbyte scan;
            public sbyte adc;
            public sbyte sd;
            public sbyte eeprom;
            public short ble;
            public sbyte spec_lib;
            public sbyte hw;
            public sbyte tmp;
            public sbyte hdc;
            public sbyte battery;
            public sbyte memory;
            public sbyte uart;
            public short system;
            [MarshalAs(UnmanagedType.ByValArray, SizeConst = 1)]
            public sbyte[] reserved;
        }

        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct NNO_error_status_struct
        {
            public uint status;
            public NNO_error_codes_struct errorCodes;
        }
        public static NNO_error_status_struct Error = new NNO_error_status_struct();
        #endregion
        private static readonly String[,] ModelMapper =
       {
            { "NIR-M-R1",   "NIR-M-R2"  },
            { "NIR-M-R21",  "NIR-M-R2"  },
            { "NIR-M-R22",  "NIR-M-R2"  },
            { "NIR-S-G1",   "NIR-M-R2"  },
            { "NIR-S-G2",   "NIR-M-R2"  },
            { "NIR-S-RX1",  "NIR-M-R2"  },
            { "NIR-S-RT1",  "NIR-M-R2"  },
            { "NIR-S-T2",   "NIR-M-T1"  },
            { "NIR-S-F2",   "NIR-M-F1"  },
            { "NIR-S-R12",  "NIR-M-R11" },
            { "NIR-S-T12",  "NIR-M-T11" },
            { "NIR-S-F12",  "NIR-M-F11" },
            { "NIR-S-R14",  "NIR-M-R13" },
            { "NIR-S-T14",  "NIR-M-T13" },
            { "NIR-S-F14",  "NIR-M-F13" },
        };
        public static String SubstituteModel(String input)
        {
            String output = String.Empty;

            for (int i = 0; i < ModelMapper.GetLength(0); i++)
            {
                if (ModelMapper[i, 0] == input)
                {
                    output = ModelMapper[i, 1];
                    break;
                }
            }

            if (output == String.Empty)
                output = input;

            return output;
        }
        public static String Get_Model_Identifier(Boolean IsFromSavedFile = false)
        {
            String currentModel = String.Empty;
            String idString = String.Empty;

            try
            {
                currentModel = SubstituteModel(Device.MFG_Name);
                if (currentModel != String.Empty && !IsFromSavedFile)
                    idString = currentModel.Substring(currentModel.LastIndexOf("-") + 1, currentModel.Length - currentModel.LastIndexOf("-") - 1);
                else
                    idString = Device.Model_Name.Substring(Device.Model_Name.LastIndexOf("-") + 1, Device.Model_Name.Length -Device.Model_Name.LastIndexOf("-") - 1);
            }
            catch { }
            return idString;
        }
    }
}