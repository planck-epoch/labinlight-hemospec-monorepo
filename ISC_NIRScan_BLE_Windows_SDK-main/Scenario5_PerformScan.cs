using ISC_UUID_DEFINITION;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Reflection.PortableExecutable;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using Windows.ApplicationModel;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Devices.Enumeration;
using Windows.Security.Cryptography;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Text;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Documents;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using static ISC_BLE_SDK.Config;
using static ISC_BLE_SDK.ScanData;
using static ISC_BLE_SDK.Scenario5_PerformScan;
//using static ISC_BLE_SDK.DataItem;
using Client = ISC_BLE_SDK.Scenario2_Client;

namespace ISC_BLE_SDK
{
    public sealed partial class Scenario5_PerformScan : Page
    {
        public enum ReferenceType
        {
            BuiltIn,
            New,
            Previous
        }

        private MainPage rootPage = MainPage.Current;

        private String RecordRadioButtonText = "";
        private string [] PGA_Array = { "Auto", "1", "2", "4", "8", "16", "32", "64" };
        private int NewRefPGA;
        private ReferenceType referenceType = ReferenceType.BuiltIn;
        private static List<int> NewReferenceIntensity = new List<int>();
        
        private string FullScanResDirPath = "";

        private DateTime TimeScanStart = new DateTime();
        private DateTime TimeScanEnd = new DateTime();

        private ScanResults ScanResultData;
        private ScanResults BuildInReferenceData;
        private ScanResults LocalNewReferenceData = new ScanResults();

        private Boolean IsFirstCheckDevBtn = true;
        private Boolean IsPageLoaded = false;

        #region UI Code
        public Scenario5_PerformScan()
        {
            InitializeComponent();

            ComboBoxPGA.Items.Clear();
            for (int i = 0; i < PGA_Array.Length; i++)
                ComboBoxPGA.Items.Add(PGA_Array[i]);

            if (Device.RefCalMatrix == null ||
                Device.RefCalCoefficients == null ||
                Device.SpecCalCoefficients == null)
            {
                DownloadCoefficient();
                NewReferenceIntensity.Clear();
            }

            CreateFolder();
        }
        private void Scenario5_PerformScan_Loaded(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            RadioButton_RefNew.Checked += RadioButtonRefType_Checked;
            RadioButton_RefPrevious.Checked += RadioButtonRefType_Checked;
            RadioButton_RefBuiltIn.Checked += RadioButtonRefType_Checked;
            RadioButton_LampAuto.Checked += RadioButtonLampMode_Checked;
            RadioButton_LampOn.Checked += RadioButtonLampMode_Checked;
            RadioButton_LampOff.Checked += RadioButtonLampMode_Checked;
            RadioButton_LockOff.Checked += RadioButtonDevBtn_Checked;
            RadioButton_LockOn.Checked += RadioButtonDevBtn_Checked;
            ComboBoxPGA.SelectionChanged += ComboBoxPGA_SelectionChanged;

            if (Device.Error.status > 0)
            {
                string ErrMsg = "Found device error: \r\n";
                ErrMsg += GetDeviceErrorDetails();
                TextBox_ErrorMsg.Text = ErrMsg;
                TextBox_ErrorMsg.Foreground = new SolidColorBrush(Colors.Red);
                ClearErrorButton.Background = new SolidColorBrush(Colors.Red);
            }
            else
            {
                TextBox_ErrorMsg.Text = "No error";
                TextBox_ErrorMsg.Foreground = (Brush)Application.Current.Resources["SystemControlForegroundBaseHighBrush"];
                ClearErrorButton.Background = new SolidColorBrush(Colors.Transparent); ;
            }

            if (NewReferenceIntensity.Count > 0)
            {
                RadioButton_RefPrevious.IsEnabled = true;
                RadioButton_RefPrevious.IsChecked = true;
            }
            else
            {
                RadioButton_RefPrevious.IsEnabled = false;
                RadioButton_RefNew.IsChecked = true;
                ScanButton.Content = "Reference Scan";
            }
            IsPageLoaded = true;
        }
        private void DisableUI()
        {
            RadioButton_RefNew.Checked -= RadioButtonRefType_Checked;
            RadioButton_RefPrevious.Checked -= RadioButtonRefType_Checked;
            RadioButton_RefBuiltIn.Checked -= RadioButtonRefType_Checked;
            RadioButton_LampAuto.Checked -= RadioButtonLampMode_Checked;
            RadioButton_LampOn.Checked -= RadioButtonLampMode_Checked;
            RadioButton_LampOff.Checked -= RadioButtonLampMode_Checked;
            RadioButton_LockOff.Checked -= RadioButtonDevBtn_Checked;
            RadioButton_LockOn.Checked -= RadioButtonDevBtn_Checked;
            ComboBoxPGA.SelectionChanged -= ComboBoxPGA_SelectionChanged;

            rootPage.IsEnabled = false;
            ScanButton.IsEnabled = false;
            RadioButton_RefNew.IsEnabled = false;
            RadioButton_RefPrevious.IsEnabled = false;
            RadioButton_RefBuiltIn.IsEnabled = false;
            RadioButton_LampAuto.IsEnabled = false;
            RadioButton_LampOn.IsEnabled = false;
            RadioButton_LampOff.IsEnabled = false;
            TextBox_Lamp_Time.IsEnabled = false;
            ComboBoxPGA.IsEnabled = false;
            RadioButton_LockOff.IsEnabled = false;
            RadioButton_LockOn.IsEnabled = false;
            ClearErrorButton.IsEnabled = false;
        }
        private void EnableUI()
        {
            rootPage.IsEnabled = true;
            ScanButton.IsEnabled = true;
            RadioButton_RefNew.IsEnabled = true;
            if(NewReferenceIntensity.Count > 0)
                RadioButton_RefPrevious.IsEnabled = true;
            RadioButton_RefBuiltIn.IsEnabled = true;
            RadioButton_LampAuto.IsEnabled = true;
            RadioButton_LampOn.IsEnabled = true;
            RadioButton_LampOff.IsEnabled = true;
            if(RadioButton_LampAuto.IsChecked == true)
                TextBox_Lamp_Time.IsEnabled = true;
            else
                TextBox_Lamp_Time.IsEnabled = false;
            ComboBoxPGA.IsEnabled = true;
            RadioButton_LockOff.IsEnabled = true;
            RadioButton_LockOn.IsEnabled = true;
            ClearErrorButton.IsEnabled = true;

            RadioButton_RefNew.Checked += RadioButtonRefType_Checked;
            RadioButton_RefPrevious.Checked += RadioButtonRefType_Checked;
            RadioButton_RefBuiltIn.Checked += RadioButtonRefType_Checked;
            RadioButton_LampAuto.Checked += RadioButtonLampMode_Checked;
            RadioButton_LampOn.Checked += RadioButtonLampMode_Checked;
            RadioButton_LampOff.Checked += RadioButtonLampMode_Checked;
            RadioButton_LockOff.Checked += RadioButtonDevBtn_Checked;
            RadioButton_LockOn.Checked += RadioButtonDevBtn_Checked;
            ComboBoxPGA.SelectionChanged += ComboBoxPGA_SelectionChanged;
        }
        private async void DownloadCoefficient()
        {
            DisableUI();
            rootPage.NotifyUser("Download reference calibration coefficients, please wait...", NotifyType.StatusProcessing);
            _ = await Client.ReadRefCalCoefficients();
            rootPage.NotifyUser("Download reference calibration matrix, please wait...", NotifyType.StatusProcessing);
            _ = await Client.ReadRefCalMatrix();
            rootPage.NotifyUser("Download spectrum calibration coefficients, please wait...", NotifyType.StatusProcessing);
            _ = await Client.ReadSpecCalCoefficients();
            rootPage.NotifyUser("Download coefficients finished!", NotifyType.StatusMessage);
            DeviceSetting();
        }
        private async void DeviceSetting()
        {
            rootPage.NotifyUser("Setting device, please wait...", NotifyType.StatusProcessing);
            _ = await Client.SetCurrentTime();
            _ = await Client.SetPGA(0);
            _ = await Client.SetLampStableTime(625);
            _ = await Client.SetLampMode(Client.LampState.AUTO);
            rootPage.NotifyUser("Setting device finished!", NotifyType.StatusMessage);
            EnableUI();
        }
        private async void ControlDeviceButton(String state)
        {
            DisableUI();
            switch (RecordRadioButtonText)
            {
                case "On":
                    _ = await Client.ControlDeviceBtn(Client.PhysicalButton.Lock);
                    EnableUI();
                    break;
                case "Off":
                    _ = await Client.ControlDeviceBtn(Client.PhysicalButton.Unlock);
                    EnableUI();
                    break;
            }  
        }
        private void RadioButtonRefType_Checked(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            if (!IsPageLoaded) return;

            RadioButton radioButton = (RadioButton)sender;
            if (radioButton.IsChecked == true && ScanButton != null)
            {
                RecordRadioButtonText = radioButton.Content.ToString();
                switch (RecordRadioButtonText)
                {
                    case "New":
                        ScanButton.Content = "Reference Scan";
                        ComboBoxPGA.SelectedIndex = 0; // Set to Auto
                        break;
                    case "Previous":
                        ScanButton.Content = "Scan";
                        SetPgaByNewReference();
                        break;
                    case "Built-in":
                        ScanButton.Content = "Scan";
                        ComboBoxPGA.SelectedIndex = 0; // Set to Auto
                        break;
                }
            }
        }
        private void RadioButtonDevBtn_Checked(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            if (!IsPageLoaded) return;

            if (IsFirstCheckDevBtn)
                IsFirstCheckDevBtn = false;
            else
            {
                RadioButton radioButton = (RadioButton)sender;
                if (radioButton.IsChecked == true)
                {
                    RecordRadioButtonText = radioButton.Content.ToString();
                    ControlDeviceButton(RecordRadioButtonText);
                }
            }          
        }
        private void RadioButtonLampMode_Checked(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            if (!IsPageLoaded) return;

            RadioButton radioButton = (RadioButton)sender;
            if (radioButton.IsChecked == true && TextBox_Lamp_Time!=null)
            {
                RecordRadioButtonText = radioButton.Content.ToString();
                SetLampMode(RecordRadioButtonText);               
            }
        }
        private void ComboBoxPGA_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (!IsPageLoaded) return;

            if (sender is ComboBox comboBox)
            {
                if (comboBox.SelectedItem != null)
                {
                    int PGA_Val = 0;
                    if(comboBox.SelectedIndex > 0)
                        PGA_Val = int.Parse(comboBox.SelectedItem.ToString());
                    SetPGA(PGA_Val);
                }
            }
        }
        private void ScanButton_Click()
        {
            StartScan();
        }
        private void ClearErrorButton_Click()
        {
            ClearError();
            TextBox_ErrorMsg.Text = "No error";
            TextBox_ErrorMsg.Foreground = (Brush)Application.Current.Resources["SystemControlForegroundBaseHighBrush"];
            ClearErrorButton.Background = new SolidColorBrush(Colors.Transparent); ;
        }
        private void ViewReportFolder_Click()
        {
            OpenReportFolder();
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            RadioButton_RefNew.IsChecked = true;
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            
        }
        #endregion

        private async void StartScan()
        {
            DisableUI();
            try
            {
                int StableTime;
                if(int.TryParse(TextBox_Lamp_Time.Text.ToString(),out StableTime))
                {
                    rootPage.NotifyUser("Clear previous errors...", NotifyType.StatusProcessing);
                    _ = await Client.ClearError();
                    rootPage.NotifyUser("Start scan, please wait...", NotifyType.StatusProcessing);
                    TimeScanStart = DateTime.Now;
                    SetReferenceType();
                    _ = await Client.SetLampStableTime(StableTime);
                    _ = await Client.StartScan();
                    rootPage.NotifyUser("Scan finished!", NotifyType.StatusMessage);
                    _ = await Client.ReadLampUsage();
                    _ = await Client.ReadDeviceStatus();
                    _ = await Client.ReadErrorStatus();
                    TimeScanEnd = DateTime.Now;
                    GetSpectrumData();
                    if (referenceType != ReferenceType.New)
                        WriteCSV();
                    else
                    {
                        rootPage.NotifyUser("New reference scan finished!", NotifyType.StatusMessage);

                        RadioButton_RefPrevious.IsChecked = true;

                        SetPgaByNewReference();
                    }

                    string ErrMsg = GetDeviceErrorDetails();
                    if (Device.Error.status > 0)
                    {
                        ErrMsg = "Found scan error: \r\n";
                        ErrMsg += GetDeviceErrorDetails();
                        TextBox_ErrorMsg.Text = ErrMsg;
                        TextBox_ErrorMsg.Foreground = new SolidColorBrush(Colors.Red);
                        ClearErrorButton.Background = new SolidColorBrush(Colors.Red);
                    }
                    else
                    {
                        TextBox_ErrorMsg.Text = "Scan completely without error found!";
                        TextBox_ErrorMsg.Foreground = (Brush)Application.Current.Resources["SystemControlForegroundBaseHighBrush"];
                        ClearErrorButton.Background = new SolidColorBrush(Colors.Transparent); ;
                    }
                }   
                else
                    rootPage.NotifyUser("Not valid lamp stable time!", NotifyType.ErrorMessage);
            }
            catch
            {               
            }
            EnableUI();
        }
        private async void SetLampMode(String Lampmode)
        {
            DisableUI();
            switch (RecordRadioButtonText)
            {
                case "Auto":
                    _ = await Client.SetLampMode(Client.LampState.AUTO);
                    EnableUI();
                    break;
                case "On":
                    _ = await Client.SetLampMode(Client.LampState.ON);
                    EnableUI();
                    break;
                case "Off":
                    _ = await Client.SetLampMode(Client.LampState.OFF);
                    EnableUI();              
                    break;
            }
        }
        private async void SetPGA(int PGA_Val)
        {
            DisableUI();
            _ = await Client.SetPGA(PGA_Val);
            EnableUI();
        }
        private async void CreateFolder()
        {
            StorageFolder localFolder = ApplicationData.Current.LocalFolder;
            string folderName = "ScanResult";
            CreationCollisionOption collisionOption = CreationCollisionOption.OpenIfExists;

            try
            {
                StorageFolder newFolder = await localFolder.CreateFolderAsync(folderName, collisionOption);
                FullScanResDirPath = newFolder.Path;
            }
            catch
            {
            }
        }
        private String ErrorByteTransfer(byte[] Errbyte)
        {
            String ErrorMsg = "";
            int ErrorInt = Errbyte[0] & 0xFF | (Errbyte[1] << 8);
            if ((ErrorInt & 0x00000001) > 0)//Scan Error
            {
                ErrorMsg += "Scan Error : ";
                int ErrDetailInt = Errbyte[4] & 0xFF;
                if ((ErrDetailInt & 0x01) > 0)
                    ErrorMsg += "DLPC150 Boot Error Detected.    ";
                if ((ErrDetailInt & 0x02) > 0)
                    ErrorMsg += "DLPC150 Init Error Detected.    ";
                if ((ErrDetailInt & 0x04) > 0)
                    ErrorMsg += "DLPC150 Lamp Driver Error Detected.    ";
                if ((ErrDetailInt & 0x08) > 0)
                    ErrorMsg += "DLPC150 Crop Image Failed.    ";
                if ((ErrDetailInt & 0x10) > 0)
                    ErrorMsg += "ADC Data Error.    ";
                if ((ErrDetailInt & 0x20) > 0)
                    ErrorMsg += "Scan Config Invalid.    ";
                if ((ErrDetailInt & 0x40) > 0)
                    ErrorMsg += "Scan Pattern Streaming Error.    ";
                if ((ErrDetailInt & 0x80) > 0)
                    ErrorMsg += "DLPC150 Read Error.    ";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000002) > 0)  // ADC Error
            {
                ErrorMsg += "ADC Error : ";
                int ErrDetailInt = Errbyte[5] & 0xFF;
                if (ErrDetailInt == 1)
                    ErrorMsg += "Timeout Error.    ";
                else if (ErrDetailInt == 2)
                    ErrorMsg += "PowerDown Error.    ";
                else if (ErrDetailInt == 3)
                    ErrorMsg += "PowerUp Error.    ";
                else if (ErrDetailInt == 4)
                    ErrorMsg += "Standby Error.    ";
                else if (ErrDetailInt == 5)
                    ErrorMsg += "WakeUp Error.    ";
                else if (ErrDetailInt == 6)
                    ErrorMsg += "Read Register Error.    ";
                else if (ErrDetailInt == 7)
                    ErrorMsg += "Write Register Error.    ";
                else if (ErrDetailInt == 8)
                    ErrorMsg += "Configure Error.    ";
                else if (ErrDetailInt == 9)
                    ErrorMsg += "Set Buffer Error.    ";
                else if (ErrDetailInt == 10)
                    ErrorMsg += "Command Error.    ";
                else if (ErrDetailInt == 11)
                    ErrorMsg += "Set PGA Error.    ";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000004) > 0)  // SD Card Error
            {
                ErrorMsg += "SD Card Error.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000008) > 0)  // EEPROM Error
            {
                ErrorMsg += "EEPROM Error.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000010) > 0)  // BLE Error
            {
                ErrorMsg += "Bluetooth Error.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000020) > 0)  // Spectrum Library Error
            {
                ErrorMsg += "Spectrum Library Error.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000040) > 0)  // Hardware Error
            {
                ErrorMsg += "HW Error : ";
                int ErrDetailInt = Errbyte[11] & 0xFF;
                if (ErrDetailInt == 1)
                    ErrorMsg += "DLPC150 Error.    ";
                else if (ErrDetailInt == 2)
                    ErrorMsg += "Read UUID Error.    ";
                else if (ErrDetailInt == 3)
                    ErrorMsg += "Flash Initial Error.    ";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000100) > 0)  // HDC Sensor Error
            {
                ErrorMsg += "HDC Error : ";
                int ErrDetailInt = Errbyte[13] & 0xFF;
                if (ErrDetailInt == 1)
                    ErrorMsg += "Invalid Manufacturing ID.    ";
                else if (ErrDetailInt == 2)
                    ErrorMsg += "Invalid Device ID.    ";
                else if (ErrDetailInt == 3)
                    ErrorMsg += "Reset Error.    ";
                else if (ErrDetailInt == 4)
                    ErrorMsg += "Read Register Error.    ";
                else if (ErrDetailInt == 5)
                    ErrorMsg += "Write Register Error.    ";
                else if (ErrDetailInt == 6)
                    ErrorMsg += "Timeout Error.    ";
                else if (ErrDetailInt == 7)
                    ErrorMsg += "I2C Error.    ";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000200) > 0)  // Battery Error
            {
                ErrorMsg += "Battery Error : ";
                int ErrDetailInt = Errbyte[14] & 0xFF;
                if (ErrDetailInt == 0x01)
                    ErrorMsg += "Battery Low.    ";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000400) > 0)  // Insufficient Memory Error
            {
                ErrorMsg += "Not Enough Memory.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00000800) > 0)  // UART Error
            {
                ErrorMsg += "UART Error.";
                ErrorMsg += ",";
            }
            if ((ErrorInt & 0x00001000) > 0)   // System Error
            {
                ErrorMsg += "System Error : ";
                int ErrDetailInt = Errbyte[17] & 0xFF;
                if ((ErrDetailInt & 0x01) > 0)
                    ErrorMsg += "Unstable Lamp ADC.    ";
                if ((ErrDetailInt & 0x02) > 0)
                    ErrorMsg += "Unstable Peak Intensity.    ";
                if ((ErrDetailInt & 0x04) > 0)
                    ErrorMsg += "ADS1255 Error.    ";
                if ((ErrDetailInt & 0x08) > 0)
                    ErrorMsg += "Auto PGA Error.    ";

                ErrDetailInt = Errbyte[18] & 0xFF;
                if ((ErrDetailInt & 0x01) > 0)
                    ErrorMsg += "Unstable Scan in Repeated times.    ";
                ErrorMsg += ",";
            }
            if (String.IsNullOrEmpty(ErrorMsg))
                ErrorMsg = "Not Found";
            return ErrorMsg;
        }
        private void SetReferenceType()
        {
            if (RadioButton_RefNew.IsChecked == true)
                referenceType = ReferenceType.New;
            else if (RadioButton_RefPrevious.IsChecked == true)
                referenceType = ReferenceType.Previous;
            else
                referenceType = ReferenceType.BuiltIn;
        }
        private void UpdateLocalNewRefDataForReport()
        {
            LocalNewReferenceData.Header_Version = ScanResultData.Header_Version;
            LocalNewReferenceData.Scan_Name = ScanResultData.Scan_Name;
            LocalNewReferenceData.Datetime.Day = ScanResultData.Datetime.Day;
            LocalNewReferenceData.Datetime.Year = ScanResultData.Datetime.Year;
            LocalNewReferenceData.Datetime.Month = ScanResultData.Datetime.Month;
            LocalNewReferenceData.Datetime.Day_of_Week = ScanResultData.Datetime.Day_of_Week;
            LocalNewReferenceData.Datetime.Minute = ScanResultData.Datetime.Minute;
            LocalNewReferenceData.Datetime.Hour = ScanResultData.Datetime.Hour;
            LocalNewReferenceData.Datetime.Second = ScanResultData.Datetime.Second;
            LocalNewReferenceData.system_temp_hundredths = ScanResultData.system_temp_hundredths;
            LocalNewReferenceData.detector_temp_hundredths = ScanResultData.detector_temp_hundredths;
            LocalNewReferenceData.humidity_hundredths = ScanResultData.humidity_hundredths;
            LocalNewReferenceData.lamp_pd = ScanResultData.lamp_pd;
            LocalNewReferenceData.scanDataIndex = ScanResultData.scanDataIndex;
            LocalNewReferenceData.calibCoeffs.ShiftVectorCoeffs = new double[3];
            LocalNewReferenceData.calibCoeffs.PixelToWavelengthCoeffs = new double[3];
            for (int i = 0; i < ScanResultData.calibCoeffs.ShiftVectorCoeffs.Length; i++)
                LocalNewReferenceData.calibCoeffs.ShiftVectorCoeffs[i] = ScanResultData.calibCoeffs.ShiftVectorCoeffs[i];
            for (int i = 0; i < ScanResultData.calibCoeffs.PixelToWavelengthCoeffs.Length; i++)
                LocalNewReferenceData.calibCoeffs.PixelToWavelengthCoeffs[i] = ScanResultData.calibCoeffs.PixelToWavelengthCoeffs[i];
            LocalNewReferenceData.serial_number = ScanResultData.serial_number;
            LocalNewReferenceData.adc_data_length = ScanResultData.adc_data_length;
            LocalNewReferenceData.black_pattern_first = ScanResultData.black_pattern_first;
            LocalNewReferenceData.black_pattern_period = ScanResultData.black_pattern_period;
            LocalNewReferenceData.pga = ScanResultData.pga;
            LocalNewReferenceData.slewScanConfig.head.scan_type = ScanResultData.slewScanConfig.head.scan_type;
            LocalNewReferenceData.slewScanConfig.head.scanConfigIndex = ScanResultData.slewScanConfig.head.scanConfigIndex;
            LocalNewReferenceData.slewScanConfig.head.ScanConfig_serial_number = ScanResultData.slewScanConfig.head.ScanConfig_serial_number;
            LocalNewReferenceData.slewScanConfig.head.config_name = ScanResultData.slewScanConfig.head.config_name;
            LocalNewReferenceData.slewScanConfig.head.num_repeats = ScanResultData.slewScanConfig.head.num_repeats;
            LocalNewReferenceData.slewScanConfig.head.num_sections = ScanResultData.slewScanConfig.head.num_sections;
            LocalNewReferenceData.slewScanConfig.section = new SlewScanSection[5];
            for (int i=0;i<ScanResultData.slewScanConfig.section.Length;i++)
            {
                LocalNewReferenceData.slewScanConfig.section[i].section_scan_type = ScanResultData.slewScanConfig.section[i].section_scan_type;
                LocalNewReferenceData.slewScanConfig.section[i].width_px = ScanResultData.slewScanConfig.section[i].width_px;
                LocalNewReferenceData.slewScanConfig.section[i].wavelength_start_nm = ScanResultData.slewScanConfig.section[i].wavelength_start_nm;
                LocalNewReferenceData.slewScanConfig.section[i].wavelength_end_nm = ScanResultData.slewScanConfig.section[i].wavelength_end_nm;
                LocalNewReferenceData.slewScanConfig.section[i].num_patterns = ScanResultData.slewScanConfig.section[i].num_patterns;
                LocalNewReferenceData.slewScanConfig.section[i].exposure_time = ScanResultData.slewScanConfig.section[i].exposure_time;
            }
        }
        private void GetSpectrumData()
        {
            try
            {
                ScanData.WaveLength.Clear();
                ScanData.Absorbance.Clear();
                ScanData.Intensity.Clear();
                ScanData.Reflectance.Clear();
                ScanData.Reference.Clear();
                ScanResultData = Client.GetScanData();
                BuildInReferenceData = Client.GetReferenceData();
                int WavCount = ScanResultData.length;
                if(referenceType == ReferenceType.Previous)
                {
                    for (int i = 0; i < WavCount; i++)
                    {
                        ScanData.WaveLength.Add(ScanResultData.Wavelength[i]);
                        ScanData.Intensity.Add(ScanResultData.intensity[i]);
                        if(NewRefPGA != ScanResultData.pga)
                            ScanData.Reference.Add(GetRefInterpretBySamplePGA(ScanResultData.pga, NewReferenceIntensity[i]));
                        else
                            ScanData.Reference.Add(NewReferenceIntensity[i]);
                        ScanData.Absorbance.Add((-1) * Math.Log10(ScanData.Intensity[i] / ScanData.Reference[i]));
                        ScanData.Reflectance.Add(ScanData.Intensity[i] / ScanData.Reference[i]);
                    }
                }
                else
                {
                    if (referenceType == ReferenceType.New)
                    {
                        NewReferenceIntensity.Clear();
                        NewRefPGA = ScanResultData.pga;
                        UpdateLocalNewRefDataForReport();
                        for (int i = 0; i < WavCount; i++)
                        {
                            ScanData.WaveLength.Add(ScanResultData.Wavelength[i]);
                            ScanData.Intensity.Add(ScanResultData.intensity[i]);
                            ScanData.Reference.Add(ScanResultData.intensity[i]);
                            NewReferenceIntensity.Add(ScanResultData.intensity[i]);
                            ScanData.Absorbance.Add(0);
                            ScanData.Reflectance.Add(1);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < WavCount; i++)
                        {
                            ScanData.WaveLength.Add(ScanResultData.Wavelength[i]);
                            ScanData.Intensity.Add(ScanResultData.intensity[i]);
                            ScanData.Reference.Add(BuildInReferenceData.intensity[i]);
                            ScanData.Absorbance.Add((-1) * Math.Log10(ScanData.Intensity[i] / ScanData.Reference[i]));
                            ScanData.Reflectance.Add(ScanData.Intensity[i] / ScanData.Reference[i]);
                        }
                    }
                }              
            }catch
            {

            }         
        }
        private int GetRefInterpretBySamplePGA(int ScanPga, int NewRef)
        {
            return (int)Math.Round(((double)NewRef / NewRefPGA) * ScanPga);
        }
        private void SetPgaByNewReference()
        {
            for (int i = 0; i < PGA_Array.Length; i++)
            {
                if (PGA_Array[i] == NewRefPGA.ToString())
                {
                    ComboBoxPGA.SelectedIndex = i;
                    break;
                }
            }
        }
        private async void ClearError()
        {
            rootPage.NotifyUser("Clear error, please wait...", NotifyType.StatusProcessing);
            DisableUI();
            _ = await Client.ClearError();
            _ = await Client.ReadErrorStatus();
            rootPage.NotifyUser("Clear error finished!", NotifyType.StatusMessage);
            EnableUI();
        }
        private string GetDeviceErrorDetails()
        {
            string ErrMsg = string.Empty;
            if ((Device.Error.status & 0x00000001) > 0)  // Scan Error
            {
                ErrMsg += "Scan Error: ";
                if ((Device.Error.errorCodes.scan & 0x01) > 0)
                    ErrMsg += "DLPC150 Boot Error Detected.    ";
                if ((Device.Error.errorCodes.scan & 0x02) > 0)
                    ErrMsg += "DLPC150 Init Error Detected.    ";
                if ((Device.Error.errorCodes.scan & 0x04) > 0)
                    ErrMsg += "DLPC150 Lamp Driver Error Detected.    ";
                if ((Device.Error.errorCodes.scan & 0x08) > 0)
                    ErrMsg += "DLPC150 Crop Image Failed.    ";
                if ((Device.Error.errorCodes.scan & 0x10) > 0)
                    ErrMsg += "Scan ADC Data Overflow.    ";
                if ((Device.Error.errorCodes.scan & 0x20) > 0)
                    ErrMsg += "Scan Config Invalid.    ";
                if ((Device.Error.errorCodes.scan & 0x40) > 0)
                    ErrMsg += "Scan Pattern Streaming Error.    ";
                if ((Device.Error.errorCodes.scan & 0x80) > 0)
                    ErrMsg += "DLPC150 Read Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000002) > 0)  // ADC Error
            {
                if (Device.Error.errorCodes.adc == 1)
                    ErrMsg += "ADC Error: Timeout Error.    ";
                else if (Device.Error.errorCodes.adc == 2)
                    ErrMsg += "ADC Error: PowerDown Error.    ";
                else if (Device.Error.errorCodes.adc == 3)
                    ErrMsg += "ADC Error: PowerUp Error.    ";
                else if (Device.Error.errorCodes.adc == 4)
                    ErrMsg += "ADC Error: Standby Error.    ";
                else if (Device.Error.errorCodes.adc == 5)
                    ErrMsg += "ADC Error: WakeUp Error.    ";
                else if (Device.Error.errorCodes.adc == 6)
                    ErrMsg += "ADC Error: Read Register Error.    ";
                else if (Device.Error.errorCodes.adc == 7)
                    ErrMsg += "ADC Error: Write Register Error.    ";
                else if (Device.Error.errorCodes.adc == 8)
                    ErrMsg += "ADC Error: Configure Error.    ";
                else if (Device.Error.errorCodes.adc == 9)
                    ErrMsg += "ADC Error: Set Buffer Error.    ";
                else if (Device.Error.errorCodes.adc == 10)
                    ErrMsg += "ADC Error: Command Error.    ";
                else if (Device.Error.errorCodes.adc == 11)
                    ErrMsg += "ADC Error: Set PGA Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000008) > 0)  // EEPROM Error
            {
                ErrMsg += "EEPROM Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000010) > 0)  // BLE Error
            {
                ErrMsg += "Bluetooth Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000020) > 0)  // Spectrum Library Error
            {
                ErrMsg += "Spectrum Library Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000040) > 0)  // Hardware Error
            {
                if (Device.Error.errorCodes.hw == 1)
                    ErrMsg += "HW Error: DLPC150 Error.    ";
                else if (Device.Error.errorCodes.hw == 2)
                    ErrMsg += "HW Error: Read UUID Error.    ";
                else if (Device.Error.errorCodes.hw == 3)
                    ErrMsg += "HW Error: Flash Initial Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000100) > 0)  // HDC Sensor Error
            {
                if (Device.Error.errorCodes.hdc == 1)
                    ErrMsg += "HDC Error: Invalid Manufacturing ID.    ";
                else if (Device.Error.errorCodes.hdc == 2)
                    ErrMsg += "HDC Error: Invalid Device ID.    ";
                else if (Device.Error.errorCodes.hdc == 3)
                    ErrMsg += "HDC Error: Reset Error.    ";
                else if (Device.Error.errorCodes.hdc == 4)
                    ErrMsg += "HDC Error: Read Register Error.    ";
                else if (Device.Error.errorCodes.hdc == 5)
                    ErrMsg += "HDC Error: Write Register Error.    ";
                else if (Device.Error.errorCodes.hdc == 6)
                    ErrMsg += "HDC Error: Timeout Error.    ";
                else if (Device.Error.errorCodes.hdc == 7)
                    ErrMsg += "HDC Error: I2C Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000200) > 0)  // Battery Error
            {
                if (Device.Error.errorCodes.battery == 0x01)
                    ErrMsg += "Battery Error: Battery Low.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000400) > 0)  // Insufficient Memory Error
            {
                ErrMsg += "Not Enough Memory.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00000800) > 0)  // UART Error
            {
                ErrMsg += "UART Error.    ";
                ErrMsg += "\r\n";
            }

            if ((Device.Error.status & 0x00001000) > 0)  // System Error
            {
                int errorCode = (int) Device.Error.errorCodes.system >> 8;
                if ((errorCode & 0x01) > 0)
                    ErrMsg += "Unstable Lamp ADC.    ";
                if ((errorCode & 0x02) > 0)
                    ErrMsg += "Unstable Peak Intensity.    ";
                if ((errorCode & 0x04) > 0)
                    ErrMsg += "ADS1255 Error.    ";
                if ((errorCode & 0x08) > 0)
                    ErrMsg += "Auto PGA Error.    ";
                if ((Device.Error.errorCodes.system & 0x10) > 0)
                    ErrMsg += "Unstable Scan in Repeated times.    ";
                ErrMsg += "\r\n";
            }

            return ErrMsg;
        }
        private void WriteCSV()
        {
            try
            {
                string buf = "";
                String[,] CSV = new String[28, 15];
                String Filename = String.Empty;
                ScanData.ScanResultFileName = Device.Model_Name + "_" + Device.Serial_Number + "_" + TimeScanStart.ToString("yyyyMMdd_HHmmss") + ".csv";
                Filename = Path.Combine(FullScanResDirPath, ScanData.ScanResultFileName);
                FileStream fs = new FileStream(@Filename, FileMode.Create);
                StreamWriter sw = new StreamWriter(fs, System.Text.Encoding.UTF8);

                // Section information field names
                CSV[0, 0] = "***Scan Config Information***";
                CSV[0, 7] = "***Reference Scan Information***";
                CSV[17, 0] = "***General Information***";
                CSV[17, 7] = "***Calibration Coefficients***";
                CSV[27, 0] = "***Scan Data***";
                // Config field names & values(Scan configuration and Reference scan configuration)
                for (int i = 0; i < 2; i++)
                {
                    CSV[1, i * 7] = "Scan Config Name:";
                    CSV[2, i * 7] = "Scan Config Type:";
                    CSV[2, i * 7 + 2] = "Num Section:";
                    CSV[3, i * 7] = "Section Config Type:";
                    CSV[4, i * 7] = "Start Wavelength (nm):";
                    CSV[5, i * 7] = "End Wavelength (nm):";
                    CSV[6, i * 7] = "Pattern Width (nm):";
                    CSV[7, i * 7] = "Exposure (ms):";
                    CSV[8, i * 7] = "Digital Resolution:";
                    CSV[9, i * 7] = "Num Repeats:";
                    CSV[10, i * 7] = "PGA Gain:";
                    CSV[11, i * 7] = "System Temp (C):";
                    CSV[12, i * 7] = "Humidity (%):";
                    CSV[13, i * 7] = "Lamp Indicator:";
                    CSV[14, i * 7] = "Data Date-Time:";
                }
              

                for (int i = 0; i < ScanResultData.slewScanConfig.head.num_sections; i++)
                {
                    if (i == 0)
                    {
                        // Scan config values
                        CSV[1, 1] = ScanResultData.slewScanConfig.head.config_name;
                        CSV[2, 1] = "Slew";
                        CSV[2, 3] = ScanResultData.slewScanConfig.head.num_sections.ToString();
                        CSV[9, 1] = ScanResultData.slewScanConfig.head.num_repeats.ToString();
                        CSV[10, 1] = ScanResultData.pga.ToString();
                        if(ComboBoxPGA.SelectedIndex == 0)
                            CSV[10, 2] = "(AutoPGA)";
                        else
                            CSV[10, 2] = "(FixedPGA)";
                        CSV[11, 1] = ((double)ScanResultData.system_temp_hundredths/100).ToString();
                        CSV[12, 1] = ((double)ScanResultData.humidity_hundredths/100).ToString();
                        CSV[13, 1] = ScanResultData.lamp_pd.ToString();
                        CSV[14, 1] = ScanResultData.Datetime.Year + "/" + ScanResultData.Datetime.Month + "/" + ScanResultData.Datetime.Day + "T" + ScanResultData.Datetime.Hour + ":" + ScanResultData.Datetime.Minute + ":" + ScanResultData.Datetime.Second;

                        if(referenceType == ReferenceType.Previous)
                            CSV[1, 8] = "Local New Reference";
                        else
                        {
                            if (BuildInReferenceData.slewScanConfig.head.config_name == "SystemTest")
                                CSV[1, 8] = "Built-in Factory Reference";
                            else 
                                CSV[1, 8] = "Built-in User Reference";
                        }
                        CSV[2, 8] = "Slew";
                        if (referenceType == ReferenceType.Previous)
                        {
                            CSV[2, 10] = LocalNewReferenceData.slewScanConfig.head.num_sections.ToString();
                            CSV[9, 8] = LocalNewReferenceData.slewScanConfig.head.num_repeats.ToString();
                            CSV[10, 8] = LocalNewReferenceData.pga.ToString();
                            CSV[11, 8] = ((double)LocalNewReferenceData.system_temp_hundredths / 100).ToString();
                            CSV[12, 8] = ((double)LocalNewReferenceData.humidity_hundredths / 100).ToString();
                            CSV[13, 8] = LocalNewReferenceData.lamp_pd.ToString();
                            CSV[14, 8] = LocalNewReferenceData.Datetime.Year + "/" + LocalNewReferenceData.Datetime.Month + "/" + LocalNewReferenceData.Datetime.Day + "T" + LocalNewReferenceData.Datetime.Hour + ":" + LocalNewReferenceData.Datetime.Minute + ":" + LocalNewReferenceData.Datetime.Second;
                        }
                        else
                        {
                            CSV[2, 10] = BuildInReferenceData.slewScanConfig.head.num_sections.ToString();
                            CSV[9, 8] = BuildInReferenceData.slewScanConfig.head.num_repeats.ToString();
                            CSV[10, 8] = BuildInReferenceData.pga.ToString();
                            CSV[11, 8] = ((double)BuildInReferenceData.system_temp_hundredths / 100).ToString();
                            CSV[12, 8] = ((double)BuildInReferenceData.humidity_hundredths / 100).ToString();
                            CSV[13, 8] = BuildInReferenceData.lamp_pd.ToString();
                            CSV[14, 8] = BuildInReferenceData.Datetime.Year + "/" + BuildInReferenceData.Datetime.Month + "/" + BuildInReferenceData.Datetime.Day + "T" + BuildInReferenceData.Datetime.Hour + ":" + BuildInReferenceData.Datetime.Minute + ":" + BuildInReferenceData.Datetime.Second;
                        }                  
                    }
                    CSV[3, i + 1] = Helper.TypeIdxToName(ScanResultData.slewScanConfig.section[i].section_scan_type);
                    CSV[4, i + 1] = ScanResultData.slewScanConfig.section[i].wavelength_start_nm.ToString();
                    CSV[5, i + 1] = ScanResultData.slewScanConfig.section[i].wavelength_end_nm.ToString();
                    CSV[6, i + 1] = Helper.PixelWidth2NM(ScanResultData.slewScanConfig.section[i].width_px);
                    CSV[7, i + 1] = Helper.ExpIdxToTime(ScanResultData.slewScanConfig.section[i].exposure_time).ToString();
                    CSV[8, i + 1] = ScanResultData.slewScanConfig.section[i].num_patterns.ToString();

                    if (referenceType == ReferenceType.Previous)
                    {
                        if (i < LocalNewReferenceData.slewScanConfig.head.num_sections)
                        {
                            CSV[3, i + 8] = Helper.TypeIdxToName(LocalNewReferenceData.slewScanConfig.section[i].section_scan_type);
                            CSV[4, i + 8] = LocalNewReferenceData.slewScanConfig.section[i].wavelength_start_nm.ToString();
                            CSV[5, i + 8] = LocalNewReferenceData.slewScanConfig.section[i].wavelength_end_nm.ToString();
                            CSV[6, i + 8] = Helper.PixelWidth2NM(LocalNewReferenceData.slewScanConfig.section[i].width_px);
                            CSV[7, i + 8] = Helper.ExpIdxToTime(LocalNewReferenceData.slewScanConfig.section[i].exposure_time).ToString();
                            CSV[8, i + 8] = LocalNewReferenceData.slewScanConfig.section[i].num_patterns.ToString();
                        }
                    }
                    else
                    {
                        // Reference config section values
                        if (i < BuildInReferenceData.slewScanConfig.head.num_sections)
                        {
                            CSV[3, i + 8] = Helper.TypeIdxToName(BuildInReferenceData.slewScanConfig.section[i].section_scan_type);
                            CSV[4, i + 8] = BuildInReferenceData.slewScanConfig.section[i].wavelength_start_nm.ToString();
                            CSV[5, i + 8] = BuildInReferenceData.slewScanConfig.section[i].wavelength_end_nm.ToString();
                            CSV[6, i + 8] = Helper.PixelWidth2NM(BuildInReferenceData.slewScanConfig.section[i].width_px);
                            CSV[7, i + 8] = Helper.ExpIdxToTime(BuildInReferenceData.slewScanConfig.section[i].exposure_time).ToString();
                            CSV[8, i + 8] = BuildInReferenceData.slewScanConfig.section[i].num_patterns.ToString();
                        }
                    }                       
                }
                CSV[15, 0] = "Total Measurement Time in sec:";
                TimeSpan ts = new TimeSpan(TimeScanEnd.Ticks - TimeScanStart.Ticks);
                CSV[15, 1] = ts.TotalSeconds.ToString();

                // Coefficients filed names & valus
                CSV[18, 7] = "Shift Vector Coefficients:";
                CSV[18, 8] = ScanResultData.calibCoeffs.ShiftVectorCoeffs[0].ToString();
                CSV[18, 9] = ScanResultData.calibCoeffs.ShiftVectorCoeffs[1].ToString();
                CSV[18, 10] = ScanResultData.calibCoeffs.ShiftVectorCoeffs[2].ToString();
                CSV[19, 7] = "Pixel to Wavelength Coefficients:";
                CSV[19, 8] = ScanResultData.calibCoeffs.PixelToWavelengthCoeffs[0].ToString();
                CSV[19, 9] = ScanResultData.calibCoeffs.PixelToWavelengthCoeffs[1].ToString();
                CSV[19, 10] = ScanResultData.calibCoeffs.PixelToWavelengthCoeffs[2].ToString();

                // General information field names & values
                CSV[18, 0] = "Model Name:";
                CSV[18, 1] = Device.Model_Name;
                CSV[19, 0] = "Serial Number:";
                CSV[19, 1] = Device.Serial_Number;
               // CSV[19, 2] = "(" + Device.MFG_Name + ")";
                CSV[20, 0] = "GUI Version:";
                PackageVersion version = Package.Current.Id.Version;
                string appVersion = $"{version.Major}.{version.Minor}.{version.Build}";
                CSV[20, 1] = appVersion;
                CSV[20, 2] = "(PC BLE)";
                CSV[21, 0] = "TIVA Version:";
                CSV[21, 1] = Device.Tiva_Rev;
                CSV[21, 7] = "***Lamp Usage ***";
                CSV[22, 7] = "Total Time(hh:mm:ss):";
                CSV[22, 8] = Device.Lamp_Usage;
                CSV[22, 0] = "UUID:";
                CSV[22, 1] = Device.UUID;
                CSV[22, 7] = "***Device/Error/Activation Status***";
                CSV[23, 0] = "Main Board Version:";
                CSV[23, 1] = ((!String.IsNullOrEmpty(Device.Hardware_Rev)) ? Device.Hardware_Rev.Substring(0, 1) : "N/A");
                CSV[24, 7] = "Device Status:";
                StringBuilder stringBuilder = new StringBuilder(8);
                byte[] devbyte = Client.GetDeviceStatusByte();
                for (int i = 3; i >= 0; i--)
                    stringBuilder.AppendFormat("{0:X2}", devbyte[i]);
                CSV[24, 8] = "0x" + stringBuilder.ToString();
                CSV[24, 9] = "Activation Status:";
                CSV[24, 10] = Device.ActivateState;
                CSV[24, 0] = "Detector Board Version:";
                CSV[24, 1] = ((!String.IsNullOrEmpty(Device.Hardware_Rev)) ? Device.Hardware_Rev.Substring(4, 1) : "N/A");
                CSV[25, 7] = "Error status:";
                StringBuilder stringBuilder_errorstatus = new StringBuilder(8);
                byte[] errbyte = Client.GetErrorStatusByte();
                for (int i = 3; i >= 0; i--)
                    stringBuilder_errorstatus.AppendFormat("{0:X2}", errbyte[i]);
                CSV[25, 8] = "0x" + stringBuilder_errorstatus.ToString();
                CSV[25, 9] = "Error Code:";
                StringBuilder stringBuilderErrorCode = new StringBuilder(8);
                bool haveError = false;

                for (int i = 4; i < 20; i++)
                {
                    stringBuilderErrorCode.Append(errbyte[i].ToString("X2"));
                    if (errbyte[i] != 0)
                        haveError = true;
                }
                CSV[25, 10] = "0x" + stringBuilderErrorCode.ToString();
                CSV[26, 9] = "Error Details:";
                if (haveError)
                {
                    CSV[26, 10] = ErrorByteTransfer(errbyte);
                    ClearErrorButton.Background = new SolidColorBrush(Colors.Red);
                }                   
                else
                {
                    CSV[26, 10] = "Not found";
                    ClearErrorButton.Background = new SolidColorBrush(Colors.Transparent);
                }                   
                for (int i = 0; i < 28; i++)
                {
                    buf = "";
                    for (int j = 0; j < 15; j++)
                        buf += (CSV[i, j] + ",");
                    sw.WriteLine(buf);
                }
                sw.WriteLine("Wavelength (nm)" + "," + "Absorbance (AU)" + "," + "Reference Signal (unitless)" + "," + "Sample Signal (unitless)");
                for (Int32 i = 0; i < ScanData.WaveLength.Count; i++)
                    sw.WriteLine(ScanData.WaveLength[i] + "," + ScanData.Absorbance[i] + "," + ScanData.Reference[i] + "," + ScanData.Intensity[i]);
                sw.Flush();
                sw.Close();
                fs.Close();
                fs.Dispose();
                rootPage.NotifyUser("Scan finished! Save report to " + @Filename, NotifyType.StatusMessage);
            }
            catch
            {
            }
        }
        private async void OpenReportFolder()
        {
            try
            {
                StorageFolder folder = await StorageFolder.GetFolderFromPathAsync(FullScanResDirPath);
                bool success = await Launcher.LaunchFolderAsync(folder);

                if (!success)
                    rootPage.NotifyUser("Can not open the report folder!", NotifyType.ErrorMessage);
            }
            catch (Exception ex)
            {
                rootPage.NotifyUser($"Can not open the report folder! {ex.Message}", NotifyType.ErrorMessage);
            }
        }
    }
}
