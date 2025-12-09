using ISC_UUID_DEFINITION;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection.PortableExecutable;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Devices.Enumeration;
using Windows.Security.Cryptography;
using Windows.Storage.Streams;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Text;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Documents;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Client = ISC_BLE_SDK.Scenario2_Client;

namespace ISC_BLE_SDK
{
    public sealed partial class Scenario3_DeviceInfo : Page
    {
        private MainPage rootPage = MainPage.Current;
        private List<string> ThisCharacteristicCollection = new List<string>();

        #region UI Code
        public Scenario3_DeviceInfo()
        {
            InitializeComponent();
            ThisCharacteristicCollection.Add(GenericAccess_Service.DeviceName_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.ManufacturerNameString_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.ModelNumberString_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.SerialNumberString_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.SystemID_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.HardwareRevisionString_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.FirmwareRevisionString_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.SoftwareRevisionString_CharUUID);
            ThisCharacteristicCollection.Add(General_Information_Service.TemperatureMeasurement_CharUUID);
            ThisCharacteristicCollection.Add(General_Information_Service.HumidityMeasurement_CharUUID);
            ThisCharacteristicCollection.Add(GenericAccess_Service.BatteryLevel_CharUUID);
            ThisCharacteristicCollection.Add(General_Information_Service.TotalLampHours_CharUUID);
            ThisCharacteristicCollection.Add(Command_Service.ActivateState_CharUUID);
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            SelectedDeviceRun.Text = rootPage.SelectedBleDeviceName;
            RefreshUiData();
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {

        }
        private async void RefreshButton_Click()
        {
            rootPage.IsEnabled = false;
            RefreshButton.IsEnabled = false;
            rootPage.NotifyUser("Refreshing data ...", NotifyType.StatusProcessing);

            Device.Advertising_Name = string.Empty;
            Device.MFG_Name = string.Empty;
            Device.Model_Name = string.Empty;
            Device.Serial_Number = string.Empty;
            Device.UUID = string.Empty;
            Device.Hardware_Rev = string.Empty;
            Device.Tiva_Rev = string.Empty;
            Device.SpecLib_Rev = string.Empty;
            Device.Temp = string.Empty;
            Device.Humi = string.Empty;
            Device.Batt_Lv = string.Empty;
            Device.Lamp_Usage = string.Empty;
            Device.ActivateState = string.Empty;
            RefreshUiData();

            if (Client.bluetoothLeDevice == null)
            {
                rootPage.NotifyUser("Failed to connect to device.", NotifyType.ErrorMessage);
                return;
            }

            foreach(string uuid in ThisCharacteristicCollection)
            {
                string ret =  await Client.GattReadData(uuid); 
                Device.SetValue(uuid, ret);
                RefreshUiData();
            }

            rootPage.NotifyUser("Refreshing data finished!", NotifyType.StatusMessage);
            RefreshButton.IsEnabled = true;
            rootPage.IsEnabled = true;
        }
        #endregion

        private void RefreshUiData()
        {
            if (rootPage.SelectedDeviceConnected)
            {
                Advertising_Name.Text = Device.Advertising_Name;
                MFG_Name.Text = Device.MFG_Name;
                Model_Name.Text = Device.Model_Name;
                Serial_Number.Text = Device.Serial_Number;
                Device_UUID.Text = Device.UUID;
                Hardware_Revision.Text = Device.Hardware_Rev;
                Tiva_Firmware_Revision.Text = Device.Tiva_Rev;
                Spectrum_Library_Revision.Text = Device.SpecLib_Rev;
                Temp.Text = Device.Temp;
                Humi.Text = Device.Humi;
                Batt_Lv.Text = Device.Batt_Lv;
                Lamp_Usage.Text = Device.Lamp_Usage;
                ActivateState.Text = Device.ActivateState;
                DeviceStatus.Text = BitConverter.ToString(Device.DeviceStatusBytes).Replace("-", ":");
                ErrorStatus.Text = BitConverter.ToString(Device.ErrorStatusBytes).Replace("-", ":");
                if (Device.ErrorStatusBytes[0] > 0)
                    ErrorStatus.Foreground = new SolidColorBrush(Colors.Red);
                else
                    ErrorStatus.Foreground = DeviceStatus.Foreground;

                RefreshButton.Visibility = Visibility.Visible;
            }
            else
            {
                Advertising_Name.Text = "N/A";
                MFG_Name.Text = "N/A";
                Model_Name.Text = "N/A";
                Serial_Number.Text = "N/A";
                Device_UUID.Text = "N/A";
                Hardware_Revision.Text = "N/A";
                Tiva_Firmware_Revision.Text = "N/A";
                Spectrum_Library_Revision.Text = "N/A";
                Temp.Text = "N/A";
                Humi.Text = "N/A";
                Batt_Lv.Text = "N/A";
                Lamp_Usage.Text = "N/A";
                ActivateState.Text = "N/A";
                DeviceStatus.Text = "N/A";
                ErrorStatus.Text = "N/A";

                RefreshButton.Visibility = Visibility.Collapsed;
            }
        }
    }
}
