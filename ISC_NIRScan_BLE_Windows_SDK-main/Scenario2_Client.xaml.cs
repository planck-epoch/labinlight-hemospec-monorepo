using ISC_UUID_DEFINITION;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection.PortableExecutable;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.ServiceModel.Channels;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.ApplicationModel.Core;
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
using static ISC_BLE_SDK.ScanData;
using static System.Net.Mime.MediaTypeNames;
using ISC_UUID = ISC_UUID_DEFINITION;
using DBG = System.Diagnostics.Debug;
using Windows.Media.Protection.PlayReady;

namespace ISC_BLE_SDK
{
    public sealed partial class Scenario2_Client : Page
    {
        public enum LampState
        {
            AUTO, ON, OFF
        }
        public enum PhysicalButton
        {
            Unlock, Lock
        }

        private static MainPage rootPage = MainPage.Current;

        public static BluetoothLEDevice bluetoothLeDevice = null;
        public static GattDeviceService selectedService;
        public static GattCharacteristic selectedCharacteristic;

        public static List<GattCharacteristic> registeredCharacteristicList = new List<GattCharacteristic>();
        public static GattPresentationFormat presentationFormat;
        public static List<String> ListShouldWriteReleaseUUID = new List<string>();

        private byte[] tmpScanConfigData = new byte[256];
        private int tmpScanConfigDataRcvLength = 0;
        private int tmpScanConfigDataTotalLength = 0;
        private Config.SlewScanConfig tmpScanCfg = new Config.SlewScanConfig();
        private static SemaphoreSlim _semaphoreWriteNotify = new SemaphoreSlim(0);
        private static SemaphoreSlim _semaphoreWrite = new SemaphoreSlim(0);
        private static SemaphoreSlim _semaphoreRead = new SemaphoreSlim(0);
        private static string CharacteristicFoundListBackup = "";
        private int BufReadDataCount = 0;

        private static int CurrentScanConfigurationDataSize;
        private static byte[] CurrentScanConfigurationDataByte;
        private static int CurrentScanConfigurationDataIndex = 0;
        private static Config.SlewScanConfig CurrentScanConfigurationData;

        private static byte[] ScanData;
        private static int DataSize;
        private static ScanResults scanResults = new ScanResults();
        private static ScanResults refResults = new ScanResults();
        private static Byte[] RefCalCoefficients;
        private static Byte[] RefCalMatrix;
        private static Byte[] SpecCalCoefficients;
        private static Byte[] DeviceStatusByte;
        private static Byte[] ErrorStatusByte;

        #region Error Codes
        readonly int E_BLUETOOTH_ATT_WRITE_NOT_PERMITTED = unchecked((int)0x80650003);
        readonly int E_BLUETOOTH_ATT_INVALID_PDU = unchecked((int)0x80650004);
        readonly int E_ACCESSDENIED = unchecked((int)0x80070005);
        readonly int E_DEVICE_NOT_AVAILABLE = unchecked((int)0x800710df); // HRESULT_FROM_WIN32(ERROR_DEVICE_NOT_AVAILABLE)
        #endregion

        #region UI Code
        public Scenario2_Client()
        {
            InitializeComponent();
            DisconnectButton.Visibility = Visibility.Collapsed;
            CharacteristicFoundListTitle.Visibility = Visibility.Collapsed;
            ListShouldWriteReleaseUUID.Clear();
            ListShouldWriteReleaseUUID.Add(Configuration_Service.SetPGADat_CharUUID);
            ListShouldWriteReleaseUUID.Add(Configuration_Service.SetLampDelayTime_CharUUID);
            ListShouldWriteReleaseUUID.Add(Configuration_Service.SetScanLampModeData_CharUUID);
            ListShouldWriteReleaseUUID.Add(Configuration_Service.ResetConfigurationData_CharUUID);
            ListShouldWriteReleaseUUID.Add(Current_Time_Service.CurrentTime_CharUUID);
            ListShouldWriteReleaseUUID.Add(General_Information_Service.ErrorStatus_CharUUID);
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            SelectedDeviceRun.Text = rootPage.SelectedBleDeviceName;
            if (string.IsNullOrEmpty(rootPage.SelectedBleDeviceId))
            {
                ConnectButton.IsEnabled = false;
            }
            else
            {
                if (rootPage.SelectedDeviceConnected)
                {
                    ConnectButton.Visibility = Visibility.Collapsed;
                    DisconnectButton.Visibility = Visibility.Visible;
                    CharacteristicFoundList.Text = CharacteristicFoundListBackup;
                }
                else
                {
                    ConnectButton.Visibility = Visibility.Visible;
                    DisconnectButton.Visibility = Visibility.Collapsed;
                    CharacteristicFoundListBackup = "";
                }
            }
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {

        }
        #endregion

        private bool ClearBluetoothLEDeviceAsync()
        {
            for (int i = 0; i < registeredCharacteristicList.Count; i++)
            {
                registeredCharacteristicList[i].Service?.Session?.Dispose();
                registeredCharacteristicList[i] = null;
            }

            selectedCharacteristic?.Service?.Dispose();
            selectedCharacteristic = null;
            selectedService?.Dispose();
            selectedService = null;

            bluetoothLeDevice?.Dispose();
            bluetoothLeDevice = null;

            GC.Collect();

            return true;
        }
        private async void ConnectButton_Click()
        {
            rootPage.IsEnabled = false;
            ConnectButton.IsEnabled = false;
            rootPage.NotifyUser("Device connecting ...", NotifyType.StatusProcessing);
            CharacteristicFoundListBackup = "";

            if (!ClearBluetoothLEDeviceAsync())
            {
                rootPage.NotifyUser("Error: Unable to reset state, try again.", NotifyType.ErrorMessage);
                ConnectButton.IsEnabled = true;
                return;
            }

            try
            {
                // BT_Code: BluetoothLEDevice.FromIdAsync must be called from a UI thread because it may prompt for consent.
                bluetoothLeDevice = await BluetoothLEDevice.FromIdAsync(rootPage.SelectedBleDeviceId);
               
                if (bluetoothLeDevice == null)
                {
                    rootPage.NotifyUser("Failed to connect to device.", NotifyType.ErrorMessage);
                }
            }
            catch (Exception ex) when (ex.HResult == E_DEVICE_NOT_AVAILABLE)
            {
                rootPage.NotifyUser("Bluetooth radio is not on.", NotifyType.ErrorMessage);
            }

            if (bluetoothLeDevice != null)
            {
                // Note: BluetoothLEDevice.GattServices property will return an empty list for unpaired devices. For all uses we recommend using the GetGattServicesAsync method.
                // BT_Code: GetGattServicesAsync returns a list of all the supported services of the device (even if it's not paired to the system).
                // If the services supported by the device are expected to change during BT usage, subscribe to the GattServicesChanged event.
                GattDeviceServicesResult result = await bluetoothLeDevice.GetGattServicesAsync(BluetoothCacheMode.Cached);

                if (result.Status == GattCommunicationStatus.Success)
                {
                    rootPage.NotifyUser("Retrieving services/characteristics ...", NotifyType.StatusProcessing);
                    CharacteristicFoundListTitle.Visibility = Visibility.Visible;

                    var services = result.Services;
                    var characteristicsCounts = 0;
                    foreach (var service in services)
                    {
                        CharacteristicFoundList.Text += DisplayHelpers.GetServiceName(service) + "\r\n";

                        IReadOnlyList<GattCharacteristic> characteristics = null;
                        try
                        {
                            // Ensure we have access to the device.
                            var accessStatus = await service.RequestAccessAsync();
                            if (accessStatus == DeviceAccessStatus.Allowed)
                            {
                                // BT_Code: Get all the child characteristics of a service. Use the cache mode to specify uncached characterstics only 
                                // and the new Async functions to get the characteristics of unpaired devices as well. 
                                var res = await service.GetCharacteristicsAsync(BluetoothCacheMode.Cached);
                                if (res.Status == GattCommunicationStatus.Success)
                                {
                                    characteristics = res.Characteristics;
                                }
                                else
                                {
                                    rootPage.NotifyUser("Error accessing service.", NotifyType.ErrorMessage);
                                    // On error, act as if there are no characteristics.
                                    characteristics = new List<GattCharacteristic>();
                                }
                            }
                            else
                            {
                                // Not granted access
                                rootPage.NotifyUser("Error accessing service.", NotifyType.ErrorMessage);
                                // On error, act as if there are no characteristics.
                                characteristics = new List<GattCharacteristic>();
                            }
                        }
                        catch (Exception ex)
                        {
                            rootPage.NotifyUser("Restricted service. Can't read characteristics: " + ex.Message,
                                NotifyType.ErrorMessage);
                            // On error, act as if there are no characteristics.
                            characteristics = new List<GattCharacteristic>();
                        }

                        characteristicsCounts += characteristics.Count;
                        foreach (GattCharacteristic c in characteristics)
                        {
                            bool deprecated;
                            string charName = DisplayHelpers.GetCharacteristicName(service, c, out deprecated);

                            if (deprecated)
                            {
                                var inlines = CharacteristicFoundList.Inlines;
                                inlines.Add(new Run() { Text = "    (Deprecated) ", Foreground = new SolidColorBrush(Color.FromArgb(255, 255, 0, 0)) });
                            }
                            else
                                CharacteristicFoundList.Text += "    ";

                            CharacteristicFoundList.Text += charName;

                            if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read) &&
                                c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Write) &&
                                c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
                                CharacteristicFoundList.Text += " (Read/Write/Notify) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read) &&
                                c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Write))
                                CharacteristicFoundList.Text += " (Read/Write) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read) &&
                                c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
                                CharacteristicFoundList.Text += " (Read/Notify) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Write) &&
                                c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
                                CharacteristicFoundList.Text += " (Write/Notify) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read))
                                CharacteristicFoundList.Text += " (Read) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Write))
                                CharacteristicFoundList.Text += " (Write) ";
                            else if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
                                CharacteristicFoundList.Text += " (Notify) ";

                            if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read))
                            {
                                GattReadResult readResult = await c.ReadValueAsync(BluetoothCacheMode.Uncached);
                                if (readResult.Status == GattCommunicationStatus.Success)
                                {
                                    string formattedResult = FormatValueByPresentation(readResult.Value, c);
                                    CharacteristicFoundList.Text += " --> " + formattedResult;

                                    var servUID = service.Uuid.ToString().ToUpper();
                                    var charUID = c.Uuid.ToString().ToUpper();
                                    if (servUID == Services.GenericAccess_UUID ||
                                        servUID == Services.GenericAttribute_UUID ||
                                        servUID == Services.Device_Information_Service_UUID ||
                                        servUID == Services.Battery_Service_UUID ||
                                        servUID == Services.General_Information_UUID ||
                                        servUID == Services.Command_UUID)
                                    {
                                        Device.SetValue(charUID, formattedResult);
                                    }
                                    else if (servUID == Services.Configuration_UUID)
                                    {
                                        Config.SetValue(charUID, formattedResult);
                                    }
                                }
                                else
                                {
                                    CharacteristicFoundList.Text += " --> Read Failed";
                                }
                            }
                            CharacteristicFoundList.Text += "\r\n";

                            if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
                            {
                                selectedService = service;
                                selectedCharacteristic = c;
                                await ValueChangedSubscribe();
                            }
                        }
                    }
                    ConnectButton.Visibility = Visibility.Collapsed;
                    DisconnectButton.Visibility = Visibility.Visible;
                    await ReadSpecCalCoefficients();
                    rootPage.NotifyUser($"Found {services.Count} services, {characteristicsCounts} characteristics", NotifyType.StatusMessage);
                    rootPage.SelectedDeviceConnected = true;
                    CharacteristicFoundListBackup = CharacteristicFoundList.Text;
                    DownloadCoefficient();
                }
                else
                {
                    rootPage.NotifyUser("Device unreachable", NotifyType.ErrorMessage);
                }
            }
            ConnectButton.IsEnabled = true;
            rootPage.IsEnabled = true;
        }
        private async void DownloadCoefficient()
        {
            rootPage.NotifyUser("Download spectrum calibration coefficients, please wait...", NotifyType.StatusProcessing);
            _ = await ReadSpecCalCoefficients();
            rootPage.NotifyUser("Download spectrum calibration coefficients finished!", NotifyType.StatusMessage);
        }
        private void DisconnectButton_Click()
        {
            ClearBluetoothLEDeviceAsync();
            bluetoothLeDevice?.Dispose();
            DisconnectButton.Visibility = Visibility.Collapsed;
            CharacteristicFoundListTitle.Visibility = Visibility.Collapsed;
            rootPage.NotifyUser("Device disconnecting", NotifyType.StatusProcessing);
            CharacteristicFoundList.Text = "";
            rootPage.IsEnabled = false;
            WaitDeviceDisconnect(null,null);

            Device.RefCalCoefficients = null;
            Device.RefCalMatrix = null;
            Device.SpecCalCoefficients = null;
            if (Config.ScanConfigList != null)
                Config.ScanConfigList.Clear();
        }
        private async void WaitDeviceDisconnect(object sender, RoutedEventArgs e)
        {
            await Task.Delay(TimeSpan.FromSeconds(5));
            ConnectButton.Visibility = Visibility.Visible;
            rootPage.NotifyUser("Device disconnected", NotifyType.StatusMessage);
            rootPage.SelectedDeviceConnected = false;
            registeredCharacteristicList.Clear();
            rootPage.IsEnabled = true;
        }

        private static string GetServiceUuidByCharacteristicUuid(string charUUID)
        {
            string serviceName = string.Empty;
            foreach (var service in ISC_UUID.Services.AllIscServicesAndCharacteristicsSet)
            {
                foreach (var characteristic in service.Value)
                {
                    if (charUUID == characteristic.Value)
                    {
                        serviceName = service.Key;
                        break;
                    }
                }
                if (serviceName != string.Empty)
                    break;
            }
            foreach (var sName in ISC_UUID.Services.Services_UUID_Data_Set)
            {
                if (sName.Key == serviceName)
                    return sName.Value;
            }
            return string.Empty;
        }
        private void AddValueChangedHandler()
        {
            if (!registeredCharacteristicList.Any(item => item.Uuid == selectedCharacteristic.Uuid))
            {
                registeredCharacteristicList.Add(selectedCharacteristic);
                registeredCharacteristicList.Last().ValueChanged += Characteristic_ValueChanged;
                DBG.WriteLine($"Successfully adding {selectedCharacteristic.Uuid} notification to list.");
            }
        }
        private async Task<bool> ValueChangedSubscribe()
        {
            // initialize status
            GattCommunicationStatus status = GattCommunicationStatus.Unreachable;
            var cccdValue = GattClientCharacteristicConfigurationDescriptorValue.None;
            if (selectedCharacteristic.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Indicate))
            {
                cccdValue = GattClientCharacteristicConfigurationDescriptorValue.Indicate;
            }
            else if (selectedCharacteristic.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Notify))
            {
                cccdValue = GattClientCharacteristicConfigurationDescriptorValue.Notify;
            }

            try
            {
                // BT_Code: Must write the CCCD in order for server to send indications.
                // We receive them in the ValueChanged event handler.
                status = await selectedCharacteristic.WriteClientCharacteristicConfigurationDescriptorAsync(cccdValue);

                if (status == GattCommunicationStatus.Success)
                {
                    string charName = DisplayHelpers.GetCharacteristicName(selectedService, selectedCharacteristic, out _);
                    AddValueChangedHandler();
                    rootPage.NotifyUser($"Successfully subscribed {charName} for value changes", NotifyType.StatusProcessing);
                }
                else
                {
                    rootPage.NotifyUser($"Error registering for value changes: {status}", NotifyType.ErrorMessage);
                }
            }
            catch (UnauthorizedAccessException ex)
            {
                // This usually happens when a device reports that it support indicate, but it actually doesn't.
                rootPage.NotifyUser(ex.Message, NotifyType.ErrorMessage);
                return false;
            }
            return true;
        }
        private void Characteristic_ValueChanged(GattCharacteristic sender, GattValueChangedEventArgs args)
        {
            // BT_Code: An Indicate or Notify reported that the value has changed.
            // Display the new value with a timestamp.
            var rcvData = FormatValueByPresentation(args.CharacteristicValue, sender);
            var message = $"Value at {DateTime.Now:hh:mm:ss.FFF}: {rcvData}";
            DBG.WriteLine(message);
            var rcvUUID = sender.Uuid.ToString().ToUpper();
            if (rcvUUID == Configuration_Service.ReturnStoredConfigurationsList_CharUUID)
            {
                if (rcvData.Split(':').ElementAt(0) == "01") // We ignore the first package(0) since we already got the length from reading number of configs
                {
                    var rawCfgIndexData = Helper.StringToByteArray(rcvData);
                    if (Config.NumStoredConfig <= 9)
                    {
                        if (rawCfgIndexData.Length == Config.NumStoredConfig * 2 + 1)
                        {
                            for (int i = 0; i < Config.NumStoredConfig; i++)
                            {
                                Config.ScanConfigIndexList.Add(new byte[2]);
                                Config.ScanConfigIndexList[i][0] = rawCfgIndexData[2 * i + 1];
                                Config.ScanConfigIndexList[i][1] = rawCfgIndexData[2 * i + 2];
                            }
                        }
                        _semaphoreWriteNotify.Release();
                    }
                    else
                    {
                        for (int i = 0; i < 9; i++)
                        {
                            Config.ScanConfigIndexList.Add(new byte[2]);
                            Config.ScanConfigIndexList[i][0] = rawCfgIndexData[2 * i + 1];
                            Config.ScanConfigIndexList[i][1] = rawCfgIndexData[2 * i + 2];
                        }
                        Config.ScanConfigIndexList.Add(new byte[2]);
                        Config.ScanConfigIndexList[9][0] = rawCfgIndexData[19];
                    }
                }
                if (rcvData.Split(':').ElementAt(0) == "02")
                {
                    var rawCfgIndexData = Helper.StringToByteArray(rcvData);
                    Config.ScanConfigIndexList[9][1] = rawCfgIndexData[1];
                    int starindex = 2;
                    for (int i = 10; i < Config.NumStoredConfig; i++)
                    {
                        Config.ScanConfigIndexList.Add(new byte[2]);
                        Config.ScanConfigIndexList[i][0] = rawCfgIndexData[starindex];
                        Config.ScanConfigIndexList[i][1] = rawCfgIndexData[starindex + 1];
                        starindex += 2;
                    }
                    _semaphoreWriteNotify.Release();
                }
                else
                {
                    if (Config.ScanConfigIndexList == null)
                        Config.ScanConfigIndexList = new List<byte[]>();
                }
            }
            else if (rcvUUID == Configuration_Service.ReturnScanConfigurationData_CharUUID)
            {
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    var rawCfgIndexData = Helper.StringToByteArray(rcvData);
                    tmpScanConfigDataTotalLength = BitConverter.ToInt16(rawCfgIndexData, 1);
                    tmpScanConfigDataRcvLength = 0;

                    if (Config.ScanConfigList == null)
                        Config.ScanConfigList = new List<Config.SlewScanConfig>();
                }
                else
                {
                    var tmpRcvData = rcvData.Remove(0, 3); // remove the package index
                    var rawCfgIndexData = Helper.StringToByteArray(tmpRcvData);
                    rawCfgIndexData.CopyTo(tmpScanConfigData, tmpScanConfigDataRcvLength);
                    tmpScanConfigDataRcvLength += rawCfgIndexData.Length;
                    if (tmpScanConfigDataRcvLength == tmpScanConfigDataTotalLength)
                    {
                        unsafe
                        {
                            fixed (void* pBuf = tmpScanConfigData)
                            {
                                DeserializeScanConfig((IntPtr)pBuf, tmpScanConfigDataRcvLength);
                            }
                        }

                        tmpScanCfg = new Config.SlewScanConfig();
                        int size = Marshal.SizeOf(tmpScanCfg);
                        IntPtr ptr = Marshal.AllocHGlobal(size);
                        Marshal.Copy(tmpScanConfigData, 0, ptr, size);
                        tmpScanCfg = (Config.SlewScanConfig)Marshal.PtrToStructure(ptr, tmpScanCfg.GetType());
                        Marshal.FreeHGlobal(ptr);
                        Config.ScanConfigList.Add(tmpScanCfg);
                        _semaphoreWriteNotify.Release();
                    }
                }
            }
            else if (rcvUUID == Configuration_Service.ReturnCurrentScanConfigurationData_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    CurrentScanConfigurationDataSize = 0;
                    CurrentScanConfigurationDataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    CurrentScanConfigurationDataByte = new byte[CurrentScanConfigurationDataSize];
                    CurrentScanConfigurationDataIndex = 0;
                }
                else
                {
                    int i;
                    for (i = 1; i < rcvDataByte.Length; i++)
                    {
                        CurrentScanConfigurationDataByte[CurrentScanConfigurationDataIndex] = rcvDataByte[i];
                        CurrentScanConfigurationDataIndex++;
                    }
                }
                if (CurrentScanConfigurationDataIndex == CurrentScanConfigurationDataSize)
                    _semaphoreWriteNotify.Release();
            }
            else if (rcvUUID == Scan_Data_Service.StartScan_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvDataByte[0] == (byte)0xff)
                {
                    byte[] ScanIndex = new byte[4];
                    ScanIndex[0] = rcvDataByte[1];
                    ScanIndex[1] = rcvDataByte[2];
                    ScanIndex[2] = rcvDataByte[3];
                    ScanIndex[3] = rcvDataByte[4];
                    var writeBuffer = CryptographicBuffer.CreateFromByteArray(ScanIndex);
                    _ = GattWriteData(Scan_Data_Service.GetScanData_CharUUID, writeBuffer);
                }
            }
            else if (rcvUUID == Scan_Data_Service.ReturnScanData_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    DataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    ScanData = new byte[DataSize];
                    BufReadDataCount = 0;
                }
                else
                {
                    for (int i = 1; i < rcvDataByte.Length; i++)
                    {
                        ScanData[BufReadDataCount] = rcvDataByte[i];
                        BufReadDataCount++;
                    }
                }
                if (DataSize == BufReadDataCount)
                {
                    unsafe
                    {
                        fixed (void* pBuf = ScanData)
                        {
                            ScanInterpret((IntPtr)pBuf, ScanData.Length, ref scanResults);
                        }
                        fixed (void* pRefCal = RefCalCoefficients)
                        fixed (void* pMatrix = RefCalMatrix)
                        {
                            RefScanInterpret((IntPtr)pRefCal, RefCalCoefficients.Length, (IntPtr)pMatrix, RefCalMatrix.Length, scanResults, ref refResults);
                        }
                    }
                    _semaphoreWriteNotify.Release();
                }
            }
            else if (rcvUUID == Calibration_Service.ReturnReferenceCalCoefficients_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    DataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    RefCalCoefficients = new byte[DataSize];
                    BufReadDataCount = 0;
                }
                else
                {
                    for (int i = 1; i < rcvDataByte.Length; i++)
                    {
                        RefCalCoefficients[BufReadDataCount] = rcvDataByte[i];
                        BufReadDataCount++;
                    }
                }
                if (DataSize == BufReadDataCount)
                {
                    _semaphoreWriteNotify.Release();
                    Device.RefCalCoefficients = RefCalCoefficients;
                }
            }
            else if (rcvUUID == Calibration_Service.ReturnReferenceCalMatrix_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    DataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    RefCalMatrix = new byte[DataSize];
                    BufReadDataCount = 0;
                }
                else
                {
                    for (int i = 1; i < rcvDataByte.Length; i++)
                    {
                        RefCalMatrix[BufReadDataCount] = rcvDataByte[i];
                        BufReadDataCount++;
                    }
                }
                if (DataSize == BufReadDataCount)
                {
                    _semaphoreWriteNotify.Release();
                    Device.RefCalMatrix = RefCalMatrix;
                }
            }
            else if (rcvUUID == Calibration_Service.ReturnSpectrumCalCoefficients_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00")
                {
                    DataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    SpecCalCoefficients = new byte[DataSize];
                    BufReadDataCount = 0;
                }
                else
                {
                    for (int i = 1; i < rcvDataByte.Length; i++)
                    {
                        SpecCalCoefficients[BufReadDataCount] = rcvDataByte[i];
                        BufReadDataCount++;
                    }
                }
                if (DataSize == BufReadDataCount)
                {
                    _semaphoreWriteNotify.Release();
                    Device.SpecCalCoefficients = SpecCalCoefficients;
                }
            }
            else if (rcvUUID == Command_Service.InternalCommand_CharUUID)
            {
                byte[] rcvDataByte = HexStringToByteArray(rcvData);
                if (rcvData.Split(':').ElementAt(0) == "00" && rcvDataByte.Length == 9)//Cmd Package
                {
                    DataSize = (((rcvDataByte[2]) << 8) | (rcvDataByte[1] & 0xFF));
                    BufReadDataCount = 0;
                }
                else
                {
                    for (int i = 1; i < rcvDataByte.Length; i++)
                        BufReadDataCount++;
                }
                if (DataSize == BufReadDataCount)
                    _semaphoreWriteNotify.Release();
            }
        }
        private static async Task<bool> GattWriteData(string uuid, IBuffer buffer)
        {
            try
            {
                GattDeviceServicesResult result = await bluetoothLeDevice.GetGattServicesAsync(BluetoothCacheMode.Cached);
                if (result.Status == GattCommunicationStatus.Success)
                {
                    var services = result.Services;
                    foreach (var service in services)
                    {
                        string iscService = GetServiceUuidByCharacteristicUuid(uuid);

                        if (iscService != string.Empty)
                        {
                            if (service.Uuid.ToString().ToUpper() != iscService.ToUpper())
                                continue;
                        }

                        try
                        {
                            var accessStatus = await service.RequestAccessAsync();
                            if (accessStatus == DeviceAccessStatus.Allowed)
                            {
                                // BT_Code: Get all the child characteristics of a service. Use the cache mode to specify uncached characterstics only 
                                var res = await service.GetCharacteristicsAsync(BluetoothCacheMode.Cached);
                                if (res.Status == GattCommunicationStatus.Success)
                                {
                                    foreach (GattCharacteristic c in res.Characteristics)
                                    {
                                        if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Write) && c.Uuid.ToString().ToUpperInvariant() == uuid)
                                        {
                                            // BT_Code: Writes the value from the buffer to the characteristic.
                                            var result_wr = await c.WriteValueWithResultAsync(buffer, GattWriteOption.WriteWithoutResponse);

                                            if (result_wr.Status == GattCommunicationStatus.Success)
                                            {
                                                DBG.WriteLine("Successfully wrote value to device", NotifyType.StatusMessage);
                                                for (int i = 0; i < ListShouldWriteReleaseUUID.Count; i++)
                                                {
                                                    if (uuid.Equals(ListShouldWriteReleaseUUID[i]))
                                                        break;
                                                }
                                                _semaphoreWrite.Release();
                                                return true;
                                            }
                                            else
                                            {
                                                DBG.WriteLine($"Write failed: {result_wr.Status}", NotifyType.ErrorMessage);
                                                for (int i = 0; i < ListShouldWriteReleaseUUID.Count; i++)
                                                {
                                                    if (uuid.Equals(ListShouldWriteReleaseUUID[i]))
                                                        break;
                                                }
                                                _semaphoreWrite.Release();
                                                return false;
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    DBG.WriteLine("Error accessing service.");
                                }
                            }
                            else
                            {
                                // Not granted access
                                DBG.WriteLine("Error accessing service.");
                            }
                        }
                        catch (Exception ex)
                        {
                            DBG.WriteLine("Restricted service. Can't read characteristics: " + ex.Message,
                                NotifyType.ErrorMessage);
                        }
                    }
                    DBG.WriteLine($"UUID to write not found");
                    for (int i = 0; i < ListShouldWriteReleaseUUID.Count; i++)
                    {
                        if (uuid.Equals(ListShouldWriteReleaseUUID[i]))
                        {
                            _semaphoreWrite.Release();
                            break;
                        }
                    }
                    return false;
                }
                else
                {
                    DBG.WriteLine($"GattCommunication error!");
                    for (int i = 0; i < ListShouldWriteReleaseUUID.Count; i++)
                    {
                        if (uuid.Equals(ListShouldWriteReleaseUUID[i]))
                        {
                            _semaphoreWrite.Release();
                            break;
                        }
                    }
                    return false;
                }
            }
            catch (Exception ex)
            {
                DBG.WriteLine(ex.Message);
                _semaphoreWrite.Release();
                return false;
            }
        }
        public static async Task<string> GattReadData(string uuid)
        {
            GattDeviceServicesResult result = await bluetoothLeDevice.GetGattServicesAsync(BluetoothCacheMode.Cached);
            if (result.Status == GattCommunicationStatus.Success)
            {
                var services = result.Services;
                foreach (var service in services)
                {
                    string iscService = GetServiceUuidByCharacteristicUuid(uuid);

                    if (iscService != string.Empty)
                    {
                        if (service.Uuid.ToString().ToUpper() != iscService.ToUpper())
                            continue;
                    }

                    try
                    {
                        var accessStatus = await service.RequestAccessAsync();
                        if (accessStatus == DeviceAccessStatus.Allowed)
                        {
                            // BT_Code: Get all the child characteristics of a service. Use the cache mode to specify uncached characterstics only 
                            var res = await service.GetCharacteristicsAsync(BluetoothCacheMode.Cached);
                            if (res.Status == GattCommunicationStatus.Success)
                            {
                                foreach (GattCharacteristic c in res.Characteristics)
                                {
                                    if (c.CharacteristicProperties.HasFlag(GattCharacteristicProperties.Read) && c.Uuid.ToString().ToUpperInvariant() == uuid)
                                    {
                                        GattReadResult readResult = await c.ReadValueAsync(BluetoothCacheMode.Uncached);
                                        if (readResult.Status == GattCommunicationStatus.Success)
                                        {
                                            //Thread.SpinWait(100);
                                            return FormatValueByPresentation(readResult.Value, c);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                rootPage.NotifyUser("Error accessing service.", NotifyType.ErrorMessage);
                            }
                        }
                        else
                        {
                            // Not granted access
                            rootPage.NotifyUser("Error accessing service.", NotifyType.ErrorMessage);
                        }
                    }
                    catch (Exception ex)
                    {
                        rootPage.NotifyUser("Restricted service. Can't read characteristics: " + ex.Message,
                            NotifyType.ErrorMessage);
                    }
                }
            }
            return "error";
        }

        #region SDK API
        public static async Task<int> ResetConfig()
        {
            byte[] buildCMD = new byte[] { 0x5A };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Configuration_Service.ResetConfigurationData_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static async Task<bool> WriteActiveScanConfigIndex(byte[] data)
        {
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(data);
            return await GattWriteData(Configuration_Service.ActiveScanConfiguration_CharUUID, writeBuffer);
        }
        public static async Task<byte[]> ReadActiveScanConfigIndex()
        {
            var idx = await GattReadData(Configuration_Service.ActiveScanConfiguration_CharUUID);
            return Helper.StringToByteArray(idx);
        }
        public static async Task<int> ReadDeviceScanConfigList()
        {
            var writeBuffer = CryptographicBuffer.GenerateRandom(1); //.ConvertStringToBinary(null, BinaryStringEncoding.Utf8);
            await GattWriteData(Configuration_Service.RequestStoredConfigurationsList_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            for (int i = 0; i < Config.NumStoredConfig; i++)
            {
                writeBuffer = CryptographicBuffer.CreateFromByteArray(Config.ScanConfigIndexList[i]);
                await GattWriteData(Configuration_Service.ReadScanConfigurationData_CharUUID, writeBuffer);
                _semaphoreWriteNotify.Wait();
            }
            return 0;
        }
        public static async Task<Config.SlewScanConfig> ReadDeviceActiveScanConfig()
        {
            /* TBD */
            Config.SlewScanConfig activeCfg = new Config.SlewScanConfig();
            var writeBuffer = CryptographicBuffer.GenerateRandom(1); //.ConvertStringToBinary(null, BinaryStringEncoding.Utf8);
            await GattWriteData(Configuration_Service.RequestStoredConfigurationsList_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            for (int i = 0; i < Config.NumStoredConfig; i++)
            {
                writeBuffer = CryptographicBuffer.CreateFromByteArray(Config.ScanConfigIndexList[i]);
                await GattWriteData(Configuration_Service.ReadScanConfigurationData_CharUUID, writeBuffer);
                _semaphoreWriteNotify.Wait();
            }
            return activeCfg;
        }
        public static async Task<int> WriteScanConfigToDevice(Config.SlewScanConfig cfg, bool set, bool save)
        //public static int WriteScanConfigToDevice(Config.SlewScanConfig cfg, bool set, bool save)
        {
            // NOT CORRECTLY IMPLEMENTED, only the serializing scan data
            int type, size = 0;
            GetScanConfigDumpSize(cfg, ref size);
            IntPtr ptr = Marshal.AllocHGlobal(size);
            Marshal.StructureToPtr(cfg, ptr, false);

            SerializeScanConfig(cfg, ptr, size);

            byte[] cfgData = new byte[size];
            Marshal.Copy(ptr, cfgData, 0, size);
            Marshal.FreeHGlobal(ptr);

            byte[] buildCMD = new byte[4];

            type = 12;
            buildCMD[0] = (byte)type;
            buildCMD[1] = set ? (byte)1 : (byte)0;
            buildCMD[2] = save ? (byte)1 : (byte)0;
            buildCMD[3] = (byte)size;

            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Configuration_Service.WriteScanConfigurationData_CharUUID, writeBuffer);

            byte chunkSize, totalBytes, byteToSend, byteSent;
            int len;
            chunkSize = 50; // Max. 127bytes could be sent at once, one bytes for package index
            totalBytes = (byte)size;
            byteSent = 0;
            byteToSend = totalBytes;
            
            type = 34;
            for (int i = 0; i < (size / chunkSize + 1); i++)
            {
                if (byteToSend < chunkSize)
                    len = byteToSend;
                else
                    len = chunkSize;

                byte[] buildData = new byte[len + 2];

                buildData[0] = (byte)type;
                buildData[1] = (byte)byteToSend;
                System.Buffer.BlockCopy(cfgData, byteSent, buildData, 2, len);

                writeBuffer = CryptographicBuffer.CreateFromByteArray(buildData);
                await GattWriteData(Configuration_Service.WriteScanConfigurationData_CharUUID, writeBuffer);

                byteToSend -= (byte)len;
                byteSent += (byte)len;

                if (byteSent == totalBytes)
                    break;
            }

            return 0;
        }
        public static async Task<int> ReadCurrentScanConfig()
        {
            byte []cmd = new byte[1];
            cmd[0] = (byte)0x01;
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(cmd); //.ConvertStringToBinary(null, BinaryStringEncoding.Utf8);
            await GattWriteData(Configuration_Service.ReadCurrentScanConfigurationData_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
           
            unsafe
            {
                fixed (void* pBuf = CurrentScanConfigurationDataByte)
                {
                    DeserializeScanConfig((IntPtr)pBuf, CurrentScanConfigurationDataByte.Length);
                }
            }

            CurrentScanConfigurationData = new Config.SlewScanConfig();
            int size = Marshal.SizeOf(CurrentScanConfigurationData);
            IntPtr ptr = Marshal.AllocHGlobal(size);
            Marshal.Copy(CurrentScanConfigurationDataByte, 0, ptr, size);
            CurrentScanConfigurationData = (Config.SlewScanConfig)Marshal.PtrToStructure(ptr, CurrentScanConfigurationData.GetType());
            Marshal.FreeHGlobal(ptr);

            return 0;
        }
        public static Config.SlewScanConfig GetCurrentScanConfig()
        {
            return CurrentScanConfigurationData;
        }
        public static async Task<int> ReadRefCalCoefficients()
        {
            byte[] buildCMD = new byte[] { 0 };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Calibration_Service.ReadReferenceCalCoefficients_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            return 0;
        }
        public static async Task<int> ReadLampUsage()
        {
            String LampUsage = await GattReadData(General_Information_Service.TotalLampHours_CharUUID);
            _semaphoreRead.Wait();
            Device.Lamp_Usage = LampUsage;
            return 0;
        }
        public static async Task<int> ReadDeviceStatus()
        {
            String DevStatus = await GattReadData(General_Information_Service.DeviceStatus_CharUUID);
            _semaphoreRead.Wait();
            return 0;
        }
        public static async Task<int> ReadErrorStatus()
        {
            String ErrorStatus = await GattReadData(General_Information_Service.ErrorStatus_CharUUID);
            _semaphoreRead.Wait();
            return 0;
        }
        public static async Task<int> ReadNumOfConfig()
        {
            _ = await GattReadData(Configuration_Service.NumberOfStoredConfigurations_CharUUID);
            _semaphoreRead.Wait();
            return 0;
        }
        public static async Task<int> SetCurrentTime()
        {
            DateTime currentTime = DateTime.Now;
            byte[] buildCMD = new byte[]{
                (byte)(currentTime.Year - 2000),
                (byte)(currentTime.Month),
                (byte)(currentTime.Day),
                (byte)((int)currentTime.DayOfWeek),
                (byte)(currentTime.Hour),
                (byte)(currentTime.Minute),
                (byte)(currentTime.Second)
            };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Current_Time_Service.CurrentTime_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static async Task<int> SetPGA(int PGAVal)
        {
            byte[] buildCMD = new byte[1];
            buildCMD[0] = (byte)((PGAVal & 0x000000ff));
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Configuration_Service.SetPGADat_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static async Task<int> ControlDeviceBtn(PhysicalButton state)
        {
            byte[] buildCMD = new byte[5];
            buildCMD[0] = (byte)0x02;
            buildCMD[1] = (byte)0x42;
            buildCMD[2] = (byte)0x03;
            buildCMD[3] = (byte)0x01;
            if (state == PhysicalButton.Lock)
                buildCMD[4] = (byte)0x01;
            else
                buildCMD[4] = (byte)0x00;
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Command_Service.InternalCommand_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            return 0;
        }
        public static async Task<int> SetLampMode(LampState state)
        {
            byte[] buildCMD = new byte[1];
            if (state == LampState.ON) 
                buildCMD[0] = (byte)0x01;
            else if (state == LampState.AUTO)
                buildCMD[0] = (byte)0x00;
            else
                buildCMD[0] = (byte)0x02;
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Configuration_Service.SetScanLampModeData_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static async Task<int> SetLampStableTime(int Time)
        {
            byte[] buildCMD = new byte[4];
            buildCMD[0] = (byte)Time;
            buildCMD[1] = (byte)(Time >> 8);
            buildCMD[2] = (byte)(Time >> 16);
            buildCMD[3] = (byte)(Time >> 24);
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Configuration_Service.SetLampDelayTime_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static async Task<int> ClearError()
        {
            byte[] buildCMD = new byte[1];
            buildCMD[0] = (byte)0;
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(General_Information_Service.ErrorStatus_CharUUID, writeBuffer);
            _semaphoreWrite.Wait();
            return 0;
        }
        public static byte[] GetRefCalCoefficients()
        {
            return RefCalCoefficients;
        }
        public static async Task<int> ReadRefCalMatrix()
        {
            byte[] buildCMD = new byte[] { 0 };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Calibration_Service.ReadReferenceCalMatrix_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            return 0;
        }
        public static byte[] GetRefCalMatrix()
        {
            return RefCalMatrix;
        }
        public static async Task<int> ReadSpecCalCoefficients()
        {
            byte[] buildCMD = new byte[] { 0 };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Calibration_Service.ReadSpectrumCalCoefficients_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            return 0;
        }
        public static byte[] GetSpecCalCoefficients()
        {
            return SpecCalCoefficients;
        }
        public static byte[]GetDeviceStatusByte()
        {
            return DeviceStatusByte;
        }
        public static byte[] GetErrorStatusByte()
        {
            return ErrorStatusByte;
        }
        public static async Task<int> StartScan()
        {
            byte[] buildCMD = new byte[] { 0 };
            var writeBuffer = CryptographicBuffer.CreateFromByteArray(buildCMD);
            await GattWriteData(Scan_Data_Service.StartScan_CharUUID, writeBuffer);
            _semaphoreWriteNotify.Wait();
            return 0;
        }
        public static ScanResults GetScanData()
        {
            return scanResults;
        }
        public static ScanResults GetReferenceData()
        {
            return refResults;
        }
        #endregion

        public static byte[] HexStringToByteArray(string hexString)
        {
            hexString = hexString.Replace(":", ""); // Remove the ":" separator if present
            int numberChars = hexString.Length;
            byte[] bytes = new byte[numberChars / 2];
            for (int i = 0; i < numberChars; i += 2)
            {
                bytes[i / 2] = Convert.ToByte(hexString.Substring(i, 2), 16);
            }
            return bytes;
        }
        private static string FormatValueByPresentation(IBuffer buffer, GattCharacteristic c)
        {
            // BT_Code: For the purpose of this sample, this function converts only UInt32 and
            // UTF-8 buffers to readable text. It can be extended to support other formats if your app needs them.
            byte[] data;
            CryptographicBuffer.CopyToByteArray(buffer, out data);
            if (c.Uuid != null && data != null)
            {
                string charUUID = c.Uuid.ToString().ToUpper();
                if (DisplayHelpers.IsSigDefinedUuid(c.Uuid))
                {
                    GattNativeCharacteristicUuid characteristicName;
                    if (Enum.TryParse(Utilities.ConvertUuidToShortId(c.Uuid).ToString(),
                        out characteristicName))
                    {
                        string charName = characteristicName.ToString();
                        if (charName == "DeviceName" ||
                            charName == "ManufacturerNameString" ||
                            charName == "ModelNumberString" ||
                            charName == "HardwareRevisionString" ||
                            charName == "FirmwareRevisionString" ||
                            charName == "SoftwareRevisionString" ||
                            charName == "IEEE11073_20601RegulatoryCertificationDataList"
                            )
                        {
                            return ASCIIEncoding.ASCII.GetString(data, 0, data.Length);
                        }
                        else if (charName == "SerialNumberString")
                        {
                            if (ASCIIEncoding.ASCII.GetString(data, 0, data.Length).Length > 8)
                                return ASCIIEncoding.ASCII.GetString(data, 0, 8);
                            else
                                return ASCIIEncoding.ASCII.GetString(data, 0, data.Length);
                        }
                        else if (charName == "BatteryLevel")
                        {
                            uint bc = data[0];
                            return $"{bc}%";
                        }
                    }

                }
                else if (charUUID == Command_Service.ActivateState_CharUUID)
                {
                    return (data[0] == 1 ? "Activated" : "Not activated");
                }
                else if (charUUID == General_Information_Service.TemperatureMeasurement_CharUUID)
                {
                    if (data.Length != 2)
                    {
                        string strData = "";
                        for (int i = 0; i < data.Length; i++)
                        {
                            strData = strData + string.Format("{0:X}", data[i]);
                            if (i < data.Length - 1)
                                strData = strData + ":";
                        }
                        return "Error (data = " + strData + ")";
                    }
                    var temp = (float)(data[0] + data[1] * 256) / 100;
                    return temp.ToString() + " ¢XC";
                }
                else if (charUUID == General_Information_Service.HumidityMeasurement_CharUUID)
                {
                    var humi = (float)(data[0] + data[1] * 256) / 100;
                    return humi.ToString() + " %RH";
                }
                else if (charUUID == General_Information_Service.TotalLampHours_CharUUID)
                {
                    TimeSpan t = TimeSpan.FromMilliseconds(BitConverter.ToUInt64(data, 0));
                    string lampUsage = string.Format("{0:D2}h:{1:D2}m:{2:D2}s:{3:D3}ms",
                                            t.Hours,
                                            t.Minutes,
                                            t.Seconds,
                                            t.Milliseconds);
                    _semaphoreRead.Release();
                    return lampUsage;
                }
                else if(charUUID == General_Information_Service.DeviceStatus_CharUUID)
                {
                    DeviceStatusByte = (byte[])data.Clone();
                    _semaphoreRead.Release();
                    Device.DeviceStatusBytes = DeviceStatusByte;
                }
                else if (charUUID == General_Information_Service.ErrorStatus_CharUUID)
                {
                    ErrorStatusByte = (byte[])data.Clone();
                    _semaphoreRead.Release();
                    
                    if (data.Length == 20) 
                    { 
                        Device.ErrorStatusBytes = ErrorStatusByte;

                        int size = Marshal.SizeOf(typeof(Device.NNO_error_status_struct));
                        IntPtr ptr = Marshal.AllocHGlobal(size);

                        try
                        {
                            Marshal.Copy(ErrorStatusByte, 0, ptr, size);
                            Device.Error = (Device.NNO_error_status_struct)Marshal.PtrToStructure(ptr, typeof(Device.NNO_error_status_struct));
                        }
                        finally
                        {
                            Marshal.FreeHGlobal(ptr);
                        }                    
                    }
                }
                else if(charUUID == Configuration_Service.NumberOfStoredConfigurations_CharUUID)
                {
                    Config.NumStoredConfig = data[0];
                    _semaphoreRead.Release();
                }
                return BitConverter.ToString(data).Replace("-", ":");
            }
            else if (data != null)
            {
                return BitConverter.ToString(data).Replace("-", ":");
            }
            else
            {
                return "Empty data received";
            }
        }

        [DllImport("DLPSpecLib.dll", 
            CallingConvention = CallingConvention.StdCall, 
            EntryPoint = "DeserializeScanConfig",
            ExactSpelling = false)]
        private static extern int DeserializeScanConfig(IntPtr ptr, int size);

        [DllImport("DLPSpecLib.dll",
            CallingConvention = CallingConvention.StdCall,
            EntryPoint = "SerializeScanConfig",
            ExactSpelling = false)]
        private static extern int SerializeScanConfig(Config.SlewScanConfig cfg, IntPtr ptr, int size);

        [DllImport("DLPSpecLib.dll",
            CallingConvention = CallingConvention.StdCall,
            EntryPoint = "GetScanConfigDumpSize",
            ExactSpelling = false)]
        private static extern int GetScanConfigDumpSize(Config.SlewScanConfig cfg, ref int size);
  
        [DllImport("DLPSpecLib.dll",
            CallingConvention = CallingConvention.StdCall,
            EntryPoint = "ScanInterpret",
            ExactSpelling = false)]
        private static extern int ScanInterpret(IntPtr ptr, int size,ref ScanData.ScanResults scanResults);
        
        [DllImport("DLPSpecLib.dll",
            CallingConvention = CallingConvention.StdCall,
            EntryPoint = "RefScanInterpret",
            ExactSpelling = false)]
        private static extern int RefScanInterpret(IntPtr pRefCal, int calSize, IntPtr pMatrix, int matrixSize, ScanData.ScanResults scanResults,ref ScanData.ScanResults refResults);

        [DllImport("DLPSpecLib.dll",
        CallingConvention = CallingConvention.StdCall,
        EntryPoint = "CalibReadData",
        ExactSpelling = false)]
        private static extern int CalibReadData(IntPtr pCalib, int calSize);
    }
}
