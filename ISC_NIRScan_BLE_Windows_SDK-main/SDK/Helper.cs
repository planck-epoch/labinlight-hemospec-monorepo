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
    public class Helper
    {
        private static MainPage rootPage = MainPage.Current;
        // Special Controls
        private readonly List<string> Con_OneNM_PixWidth = new List<string> { "R13", "T13", "F13" };

        public static string TypeIdxToName(int Idx)
        {
            switch (Idx)
            {
                case 0:
                    return "Column";
                case 1:
                    return "Hadamard";
                case 2:
                    return "Slew";
                default:
                    return "Unknown";
            }
        }
        public static int TypeNameToIdx(string typeName)
        {
            switch (typeName)
            {
                case "Column":
                    return 0;
                case "Hadamard":
                    return 1;
                case "Slew":
                    return 2;
                default:
                    return -1;
            }
        }
        public static string PixelWidth2NM(int PW)
        {
            bool isExtendedPlusModel = Device.Serial_Number.IndexOf('P') > 0;
            double pixelWidthRatio = isExtendedPlusModel ? 1 : 1.17;
            return (PW * pixelWidthRatio).ToString(isExtendedPlusModel ? "0" : "0.00");
        }
        public static int NM2PixelWidth(string NM)
        {
            bool isExtendedPlusModel = Device.Serial_Number.IndexOf('P') > 0;
            double pixelWidthRatio = isExtendedPlusModel ? 1 : 1.17;
            return (int)Math.Round(float.Parse(NM) / pixelWidthRatio, MidpointRounding.AwayFromZero);
        }
        public static string ExpIdxToTime(int Idx)
        {
            switch (Idx)
            {
                case 0:
                    return "0.635";
                case 1:
                    return "1.27";
                case 2:
                    return "2.54";
                case 3:
                    return "15.24";
                case 4:
                    return "30.48";
                case 5:
                    return "60.96";
                default:
                    return "Unknown";
            }
        }
        public static int ExpTimeToIdx(string ExpTimeString)
        {
            string expTime = ExpTimeString.Split('.').ElementAt(0);

            switch (expTime)
            {
                case "0":
                    return 0;
                case "1":
                    return 1;
                case "2":
                    return 2;
                case "15":
                    return 3;
                case "30":
                    return 4;
                case "60":
                    return 5;
                default:
                    return -1;
            }
        }
        public static byte[] StringToByteArray(string hexStr)
        {
            var newHex = hexStr.Replace(":",string.Empty);
            return Enumerable.Range(0, newHex.Length)
                             .Where(x => x % 2 == 0)
                             .Select(x => Convert.ToByte(newHex.Substring(x, 2), 16))
                             .ToArray();
        }
        public static Int16 ByteStringToInt16(string str)
        {
            return BitConverter.ToInt16(Helper.StringToByteArray(str), 0);
        }

        /*!
        * 將 configuration pattern 寬度索引值轉換成 nm。
        * 
        * \fn          CfgWidthIndexToNM
        * \param[in]   Index   要轉換的索引值
        * \return      nm
        */
        //[DllImport(DLL_FILE_NAME, EntryPoint = "DLPSDK_API_ScanConfig_WidthIndexToNM", CallingConvention = CallingConvention.Cdecl)]
        public static Double CfgWidthIndexToNM(Int32 Index, Boolean IsFromSavedFile = false)
        {
            return CfgWidthPixelToNM(Index + 2, IsFromSavedFile);
        }
        /*!
        * 將 configuration pattern 寬度像素值轉換成 nm。
        * 
        * \fn          CfgWidthPixelToNM
        * \param[in]   Pixel   要轉換的像素值
        * \return      nm
        */
        //[DllImport(DLL_FILE_NAME, EntryPoint = "DLPSDK_API_ScanConfig_WidthPixelToNM", CallingConvention = CallingConvention.Cdecl)]
        public static Double CfgWidthPixelToNM(Int32 Pixel, Boolean IsFromSavedFile = false)
        {
            double pixelWidth = 800 / (854 * 0.8);
            var h = new Helper();
            if (h.Con_OneNM_PixWidth.FirstOrDefault(stringToCheck => stringToCheck.Contains(Device.Get_Model_Identifier(IsFromSavedFile))) == Device.Get_Model_Identifier(IsFromSavedFile))
                return (Double)(Pixel);
            else
                return (Double)(Pixel * pixelWidth);
        }
    }
}