
using ISC_UUID_DEFINITION;
using Microsoft.UI.Xaml.Controls;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Net.NetworkInformation;
using System.Numerics;
using System.Reflection.PortableExecutable;
using System.Runtime.InteropServices;
using System.ServiceModel.Channels;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Devices.Enumeration;
using Windows.Graphics.Printing3D;
using Windows.Security.Cryptography;
using Windows.Storage.Streams;
using Windows.System.Profile;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Text;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Documents;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using static ISC_BLE_SDK.Config;
using Client = ISC_BLE_SDK.Scenario2_Client;

namespace ISC_BLE_SDK
{
    public sealed partial class Scenario4_SetConfig : Page
    {
        private MainPage rootPage = MainPage.Current;

        private readonly int MAX_SECTION_NUM = 5;

        List<TextBlock>  CFG_Section_Label = new List<TextBlock>();
        List<ComboBox> CFG_Type = new List<ComboBox>();
        List<TextBox> CFG_Start = new List<TextBox>();
        List<TextBox> CFG_End = new List<TextBox>();
        List<TextBox> CFG_Resolution = new List<TextBox>();
        List<ComboBox> CFG_Width = new List<ComboBox>();
        List<ComboBox> CFG_Exposure = new List<ComboBox>();

        List<TextBlock> CFG_Section_Label_Set = new List<TextBlock>();
        List<ComboBox> CFG_Type_Set = new List<ComboBox>();
        List<TextBox> CFG_Start_Set = new List<TextBox>();
        List<TextBox> CFG_End_Set = new List<TextBox>();
        List<TextBox> CFG_Resolution_Set = new List<TextBox>();
        List<ComboBox> CFG_Width_Set = new List<ComboBox>();
        List<ComboBox> CFG_Exposure_Set = new List<ComboBox>();
        List<TextBox> CFG_OverSampling_Set = new List<TextBox>();

        List<TextBlock> CFG_Section_Label_Active = new List<TextBlock>();
        List<ComboBox> CFG_Type_Active = new List<ComboBox>();
        List<TextBox> CFG_Start_Active = new List<TextBox>();
        List<TextBox> CFG_End_Active = new List<TextBox>();
        List<TextBox> CFG_Resolution_Active = new List<TextBox>();
        List<ComboBox> CFG_Width_Active = new List<ComboBox>();
        List<ComboBox> CFG_Exposure_Active = new List<ComboBox>();

        private static int PrevTotalPatternUsed = 0;
        private static int TotalPatternUsed = 0;
        private int minWav = 900;
        private int maxWav = 1700;
        private readonly List<string> Con_OneNM_PixWidth = new List<string> { "R13", "T13", "F13" };

        #region UI Code
        public Scenario4_SetConfig()
        {
            InitializeComponent();

            if (!rootPage.SelectedDeviceConnected)
            {
                this.IsHitTestVisible = false;
                rootPage.NotifyUser("No device connected...", NotifyType.ErrorMessage);
                return;
            }
            else
                this.IsHitTestVisible = true;
            GetWavRange();

            CFG_Section_Label.Add(CFG_Section_Label_1);
            CFG_Section_Label.Add(CFG_Section_Label_2);
            CFG_Section_Label.Add(CFG_Section_Label_3);
            CFG_Section_Label.Add(CFG_Section_Label_4);
            CFG_Section_Label.Add(CFG_Section_Label_5);
            CFG_Type.Add(CFG_Type_1);
            CFG_Type.Add(CFG_Type_2);
            CFG_Type.Add(CFG_Type_3);
            CFG_Type.Add(CFG_Type_4);
            CFG_Type.Add(CFG_Type_5);
            CFG_Start.Add(CFG_Start_1);
            CFG_Start.Add(CFG_Start_2);
            CFG_Start.Add(CFG_Start_3);
            CFG_Start.Add(CFG_Start_4);
            CFG_Start.Add(CFG_Start_5);
            CFG_End.Add(CFG_End_1);
            CFG_End.Add(CFG_End_2);
            CFG_End.Add(CFG_End_3);
            CFG_End.Add(CFG_End_4);
            CFG_End.Add(CFG_End_5);
            CFG_Width.Add(CFG_Width_1);
            CFG_Width.Add(CFG_Width_2);
            CFG_Width.Add(CFG_Width_3);
            CFG_Width.Add(CFG_Width_4);
            CFG_Width.Add(CFG_Width_5);
            CFG_Resolution.Add(CFG_Resolution_1);
            CFG_Resolution.Add(CFG_Resolution_2);
            CFG_Resolution.Add(CFG_Resolution_3);
            CFG_Resolution.Add(CFG_Resolution_4);
            CFG_Resolution.Add(CFG_Resolution_5);
            CFG_Exposure.Add(CFG_Exposure_1);
            CFG_Exposure.Add(CFG_Exposure_2);
            CFG_Exposure.Add(CFG_Exposure_3);
            CFG_Exposure.Add(CFG_Exposure_4);
            CFG_Exposure.Add(CFG_Exposure_5);
           
            for (int i = 0; i < MAX_SECTION_NUM; i++)
            {
                CFG_NumSections.Items.Add(i+1);
                for (int j = 0; j < 2; j++)
                    CFG_Type[i].Items.Add(Helper.TypeIdxToName(j));
                for (int j = 0; j < 51; j++)
                    CFG_Width[i].Items.Add(Helper.PixelWidth2NM(j+2));
                for (int j = 0; j < 5; j++)
                    CFG_Exposure[i].Items.Add(Helper.ExpIdxToTime(j));
                CFG_Type[i].SelectedIndex = 0;
                CFG_Width[i].SelectedIndex = 6;
                CFG_Exposure[i].SelectedIndex = 0;
            }

            ApplyActiveScanConfigIndex.Click += new RoutedEventHandler(ApplyActiveScanConfigIndex_Click);
            Btn_ResetConfig.Click += new RoutedEventHandler(ResetConfig_Click);
            CFG_NumSections.SelectionChanged += new SelectionChangedEventHandler(CFG_NumSections_SelectionChanged);
            CFG_SelectScanConfig.SelectionChanged += new SelectionChangedEventHandler(CFG_SelectScanConfig_SelectionChanged);
            CFG_NumSections.SelectedIndex = 0;

            CFG_Section_Label_Set.Add(CFG_Section_Label_Set_1);
            CFG_Section_Label_Set.Add(CFG_Section_Label_Set_2);
            CFG_Section_Label_Set.Add(CFG_Section_Label_Set_3);
            CFG_Section_Label_Set.Add(CFG_Section_Label_Set_4);
            CFG_Section_Label_Set.Add(CFG_Section_Label_Set_5);
            CFG_Type_Set.Add(CFG_Type_Set_1);
            CFG_Type_Set_1.SelectedIndex = 0;
            CFG_Type_Set.Add(CFG_Type_Set_2);
            CFG_Type_Set.Add(CFG_Type_Set_3);
            CFG_Type_Set.Add(CFG_Type_Set_4);
            CFG_Type_Set.Add(CFG_Type_Set_5);
            CFG_Start_Set.Add(CFG_Start_Set_1);
            CFG_Start_Set_1.Text = minWav.ToString();
            CFG_Start_Set.Add(CFG_Start_Set_2);
            CFG_Start_Set.Add(CFG_Start_Set_3);
            CFG_Start_Set.Add(CFG_Start_Set_4);
            CFG_Start_Set.Add(CFG_Start_Set_5);
            CFG_End_Set.Add(CFG_End_Set_1);
            CFG_End_Set_1.Text = maxWav.ToString();
            CFG_End_Set.Add(CFG_End_Set_2);
            CFG_End_Set.Add(CFG_End_Set_3);
            CFG_End_Set.Add(CFG_End_Set_4);
            CFG_End_Set.Add(CFG_End_Set_5);
            CFG_Width_Set.Add(CFG_Width_Set_1);
            CFG_Width_Set_1.SelectedIndex = 6;            
            CFG_Width_Set.Add(CFG_Width_Set_2);
            CFG_Width_Set.Add(CFG_Width_Set_3);
            CFG_Width_Set.Add(CFG_Width_Set_4);
            CFG_Width_Set.Add(CFG_Width_Set_5);
            CFG_Resolution_Set.Add(CFG_Resolution_Set_1);
            CFG_Resolution_Set_1.Text = "128";
            CFG_Resolution_Set.Add(CFG_Resolution_Set_2);
            CFG_Resolution_Set.Add(CFG_Resolution_Set_3);
            CFG_Resolution_Set.Add(CFG_Resolution_Set_4);
            CFG_Resolution_Set.Add(CFG_Resolution_Set_5);
            CFG_Exposure_Set.Add(CFG_Exposure_Set_1);
            CFG_Exposure_Set_1.SelectedIndex = 1;
            CFG_Exposure_Set.Add(CFG_Exposure_Set_2);
            CFG_Exposure_Set.Add(CFG_Exposure_Set_3);
            CFG_Exposure_Set.Add(CFG_Exposure_Set_4);
            CFG_Exposure_Set.Add(CFG_Exposure_Set_5);
            CFG_OverSampling_Set.Add(Oversampling_Set_1);
            CFG_OverSampling_Set.Add(Oversampling_Set_2);
            CFG_OverSampling_Set.Add(Oversampling_Set_3);
            CFG_OverSampling_Set.Add(Oversampling_Set_4);
            CFG_OverSampling_Set.Add(Oversampling_Set_5);
            for (int i = 0; i < MAX_SECTION_NUM; i++)
            {
                CFG_NumSections_Set.Items.Add(i + 1);
                for (int j = 0; j < 2; j++)
                    CFG_Type_Set[i].Items.Add(Helper.TypeIdxToName(j));
                for (int j = 0; j < 51; j++)
                    CFG_Width_Set[i].Items.Add(Helper.PixelWidth2NM(j + 2));
                for (int j = 0; j < 5; j++)
                    CFG_Exposure_Set[i].Items.Add(Helper.ExpIdxToTime(j));
                CFG_Type_Set[i].SelectedIndex = 0;
                CFG_Width_Set[i].SelectedIndex = 6;
                CFG_Exposure_Set[i].SelectedIndex = 0;
                CFG_OverSampling_Set[i].IsEnabled = false;
            }
            CFG_NumSections_Set.SelectionChanged += new SelectionChangedEventHandler(CFG_NumSections_SelectionChanged);
            SetScanConfig.Click += new RoutedEventHandler(SetScanConfig_Click);
            SaveScanConfig.Click += new RoutedEventHandler(SaveScanConfig_Click);
            Tabs.SelectionChanged += new SelectionChangedEventHandler(Tabs_SelectionChanged);
            CFG_NumSections_Set.SelectedIndex = 0;
            for (int i = 0; i < MAX_SECTION_NUM; i++)
            {
                CFG_Grid_Set.ColumnDefinitions[i + 1].Width = i <= CFG_NumSections_Set.SelectedIndex ? new GridLength(1, GridUnitType.Star) : new GridLength(0);
            }

            CFG_Section_Label_Active.Add(CFG_Section_Label_Active_1);
            CFG_Section_Label_Active.Add(CFG_Section_Label_Active_2);
            CFG_Section_Label_Active.Add(CFG_Section_Label_Active_3);
            CFG_Section_Label_Active.Add(CFG_Section_Label_Active_4);
            CFG_Section_Label_Active.Add(CFG_Section_Label_Active_5);
            CFG_Type_Active.Add(CFG_Type_Active_1);
            CFG_Type_Active.Add(CFG_Type_Active_2);
            CFG_Type_Active.Add(CFG_Type_Active_3);
            CFG_Type_Active.Add(CFG_Type_Active_4);
            CFG_Type_Active.Add(CFG_Type_Active_5);
            CFG_Start_Active.Add(CFG_Start_Active_1);
            CFG_Start_Active.Add(CFG_Start_Active_2);
            CFG_Start_Active.Add(CFG_Start_Active_3);
            CFG_Start_Active.Add(CFG_Start_Active_4);
            CFG_Start_Active.Add(CFG_Start_Active_5);
            CFG_End_Active.Add(CFG_End_Active_1);
            CFG_End_Active.Add(CFG_End_Active_2);
            CFG_End_Active.Add(CFG_End_Active_3);
            CFG_End_Active.Add(CFG_End_Active_4);
            CFG_End_Active.Add(CFG_End_Active_5);
            CFG_Width_Active.Add(CFG_Width_Active_1);
            CFG_Width_Active.Add(CFG_Width_Active_2);
            CFG_Width_Active.Add(CFG_Width_Active_3);
            CFG_Width_Active.Add(CFG_Width_Active_4);
            CFG_Width_Active.Add(CFG_Width_Active_5);
            CFG_Resolution_Active.Add(CFG_Resolution_Active_1);
            CFG_Resolution_Active.Add(CFG_Resolution_Active_2);
            CFG_Resolution_Active.Add(CFG_Resolution_Active_3);
            CFG_Resolution_Active.Add(CFG_Resolution_Active_4);
            CFG_Resolution_Active.Add(CFG_Resolution_Active_5);
            CFG_Exposure_Active.Add(CFG_Exposure_Active_1);
            CFG_Exposure_Active.Add(CFG_Exposure_Active_2);
            CFG_Exposure_Active.Add(CFG_Exposure_Active_3);
            CFG_Exposure_Active.Add(CFG_Exposure_Active_4);
            CFG_Exposure_Active.Add(CFG_Exposure_Active_5);

            CFG_Name_Active.IsEnabled = false;
            CFG_NumAvg_Active.IsEnabled = false;
            CFG_NumSections_Active.IsEnabled = false;
            for (int i = 0; i < MAX_SECTION_NUM; i++)
            {
                CFG_NumSections_Active.Items.Add(i + 1);
                for (int j = 0; j < 2; j++)
                    CFG_Type_Active[i].Items.Add(Helper.TypeIdxToName(j));
                for (int j = 0; j < 51; j++)
                    CFG_Width_Active[i].Items.Add(Helper.PixelWidth2NM(j + 2));
                for (int j = 0; j < 5; j++)
                    CFG_Exposure_Active[i].Items.Add(Helper.ExpIdxToTime(j));
                CFG_Type_Active[i].SelectedIndex = 0;
                CFG_Width_Active[i].SelectedIndex = (6 - 2);
                CFG_Exposure_Active[i].SelectedIndex = 0;

                CFG_Type_Active[i].IsEnabled = false;
                CFG_Start_Active[i].IsEnabled = false;
                CFG_End_Active[i].IsEnabled = false;
                CFG_Width_Active[i].IsEnabled = false;
                CFG_Exposure_Active[i].IsEnabled = false;
                CFG_Resolution_Active[i].IsEnabled = false;
            }
            CFG_NumSections_Active.SelectionChanged += new SelectionChangedEventHandler(CFG_NumSections_SelectionChanged);

            if (Config.ScanConfigList == null || Config.ScanConfigList.Count == 0)
            {
                rootPage.IsEnabled = false;
                rootPage.NotifyUser("Reading device scan configuration data, please wait...", NotifyType.StatusProcessing);
                this.IsHitTestVisible = false;
                RefreshDeviceConfigDataToUI(false);
            }
            else
            {
                CFG_SelectScanConfig.Items.Clear();
                for (int i = 0; i < Config.NumStoredConfig; i++)
                {
                    CFG_SelectScanConfig.Items.Add($"{Config.ScanConfigList[i].head.config_name}");
                }
                for (int i = 0; i < Config.ScanConfigIndexList.Count; i++)
                {
                    if (BitConverter.ToInt16(Config.ScanConfigIndexList[i], 0) == BitConverter.ToInt16(Config.ActiveScanIndex, 0))
                    {
                        CFG_SelectScanConfig.SelectedIndex = i;
                        break;
                    }
                }
            }
           
        }
        private void GetWavRange()
        {
            if (Device.Tiva_Rev.Substring(0, 1).Equals("3") && (Device.Hardware_Rev.Substring(0, 1).Equals("E") || Device.Hardware_Rev.Substring(0, 1).Equals("O")))
            {
                minWav = 1350;
                maxWav = 2150;
            }
            else if (Device.Tiva_Rev.Substring(0, 1).Equals("5"))
            {
                minWav = 1600;
                maxWav = 2400;
            }
            else
            {
                minWav = 900;
                maxWav = 1700;
            }
        }
        private async void RefreshDeviceConfigDataToUI(Boolean IsReset)
        {
            Config.ScanConfigList = null;
            Config.ScanConfigIndexList = null;
            _ = await Client.ReadNumOfConfig();
            _ = await Client.ReadDeviceScanConfigList();

            CFG_SelectScanConfig.Items.Clear();
            for (int i = 0; i < Config.NumStoredConfig; i++)
            {
                CFG_SelectScanConfig.Items.Add($"{Config.ScanConfigList[i].head.config_name}");
            }
            if (IsReset)
                CFG_SelectScanConfig.SelectedIndex = 0;
            else
            {
                for (int i = 0; i < Config.ScanConfigIndexList.Count; i++)
                {
                    if (BitConverter.ToInt16(Config.ScanConfigIndexList[i], 0) == BitConverter.ToInt16(Config.ActiveScanIndex, 0))
                    {
                        CFG_SelectScanConfig.SelectedIndex = i;
                        break;
                    }
                }
            }
            rootPage.NotifyUser("Reading device scan configuration data finished!", NotifyType.StatusMessage);
            this.IsHitTestVisible = true;
            rootPage.IsEnabled = true;
            Tabs.IsEnabled = true;
        }
        private async void RefreshDeviceActiveConfigDataToUI()
        {
            await Client.ReadDeviceActiveScanConfig();

            for (int i = 0; i < Config.ScanConfigIndexList.Count; i++)
            {
                if (BitConverter.ToInt16(Config.ScanConfigIndexList[i], 0) == BitConverter.ToInt16(Config.ActiveScanIndex, 0))
                {
                    CFG_SelectScanConfig.SelectedIndex = i;
                    break;
                }
            }

            rootPage.NotifyUser("Reading active device scan configuration data finished!", NotifyType.StatusMessage);
            this.IsHitTestVisible = true;
        }
        private void CFG_SelectScanConfig_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            int idx = (int)(sender as ComboBox).SelectedIndex;
            if(idx >= 0)
            {
                CFG_Name.Text = Config.ScanConfigList[idx].head.config_name;
                CFG_NumAvg.Text = Config.ScanConfigList[idx].head.num_repeats.ToString();
                CFG_NumSections.SelectedIndex = Config.ScanConfigList[idx].head.num_sections - 1;

                CFG_Name.IsEnabled = false;
                CFG_NumAvg.IsEnabled = false;
                CFG_NumSections.IsEnabled = false;

                for (int i = 0; i < Config.ScanConfigList[idx].head.num_sections; i++)
                {
                    CFG_Type[i].SelectedIndex = Config.ScanConfigList[idx].section[i].section_scan_type;
                    CFG_Start[i].Text = Config.ScanConfigList[idx].section[i].wavelength_start_nm.ToString();
                    CFG_End[i].Text = Config.ScanConfigList[idx].section[i].wavelength_end_nm.ToString();
                    CFG_Width[i].SelectedIndex = Config.ScanConfigList[idx].section[i].width_px - 2;
                    CFG_Exposure[i].SelectedIndex = Config.ScanConfigList[idx].section[i].exposure_time;
                    CFG_Resolution[i].Text = Config.ScanConfigList[idx].section[i].num_patterns.ToString();

                    CFG_Type[i].IsEnabled = false;
                    CFG_Start[i].IsEnabled = false;
                    CFG_End[i].IsEnabled = false;
                    CFG_Width[i].IsEnabled = false;
                    CFG_Exposure[i].IsEnabled = false;
                    CFG_Resolution[i].IsEnabled = false;
                }
            }          
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            RefreshUiData();
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {

        }
        private void Tabs_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var sel = (TabView)sender;
            if (sel.SelectedIndex == 2)
            {
                StartReadCurrentScanConfig();
            }
        }
        private void CFG_NumSections_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (Tabs.SelectedIndex == 0)
            {
                for (int i = 0; i < MAX_SECTION_NUM; i++)
                {
                    CFG_Grid.ColumnDefinitions[i + 1].Width = i <= CFG_NumSections.SelectedIndex ? new GridLength(1, GridUnitType.Star) : new GridLength(0);
                }
            }
            else if (Tabs.SelectedIndex == 1)
            {
                for (int i = 0; i < MAX_SECTION_NUM; i++)
                {
                    CFG_Grid_Set.ColumnDefinitions[i + 1].Width = i <= CFG_NumSections_Set.SelectedIndex ? new GridLength(1, GridUnitType.Star) : new GridLength(0);
                }
            }
            else
            {
                for (int i = 0; i < MAX_SECTION_NUM; i++)
                {
                    CFG_Grid_Active.ColumnDefinitions[i + 1].Width = i <= CFG_NumSections_Active.SelectedIndex ? new GridLength(1, GridUnitType.Star) : new GridLength(0);
                }
            }
        }
        private void ApplyActiveScanConfigIndex_Click(object sender, RoutedEventArgs e)
        {
            var idxData = Config.ScanConfigIndexList[CFG_SelectScanConfig.SelectedIndex];
            _ = Client.WriteActiveScanConfigIndex(idxData);
            rootPage.NotifyUser("Apply " + CFG_SelectScanConfig.SelectedItem.ToString() + " for default/active scan config finished!", NotifyType.StatusMessage);           
        }
        private void ResetConfig_Click(object sender, RoutedEventArgs e)
        {
            ResetConfig();
        }
        private void SetScanConfig_Click(object sender, RoutedEventArgs e)
        {
            StartSetOrSaveScanConfig(true);
        }     
        private void SaveScanConfig_Click(object sender, RoutedEventArgs e)
        {
            StartSetOrSaveScanConfig(false);
        }
        #endregion

        private void RefreshUiData()
        {
            if (rootPage.SelectedDeviceConnected)
            {}
            else
            {}
        }
        private async void ResetConfig()
        {
            rootPage.IsEnabled = false;
            Tabs.IsEnabled = false;
            rootPage.NotifyUser("Reset config, please wait...", NotifyType.StatusProcessing);
            _ = await Client.ResetConfig();
            rootPage.NotifyUser("Reset config finished!" + " Reading device scan configuration data, please wait...", NotifyType.StatusProcessing);
            RefreshDeviceConfigDataToUI(true);
        }
        private async void StartReadCurrentScanConfig()
        {
            rootPage.IsEnabled = false;
            Tabs.IsEnabled = false;
            rootPage.NotifyUser("Read current active scan config, please wait...", NotifyType.StatusProcessing);
            _ = await Client.ReadCurrentScanConfig();
            Config.SlewScanConfig CurrentScanConfigurationData = Client.GetCurrentScanConfig();
            int SectionNum = CurrentScanConfigurationData.head.num_sections;
            CFG_Name_Active.Text = CurrentScanConfigurationData.head.config_name;
            CFG_NumAvg_Active.Text = CurrentScanConfigurationData.head.num_repeats.ToString();
            CFG_NumSections_Active.SelectedIndex = CurrentScanConfigurationData.head.num_sections - 1;
            for (int i = 0; i < SectionNum; i++)
            {
                CFG_Type_Active[i].SelectedIndex = CurrentScanConfigurationData.section[i].section_scan_type;
                CFG_Start_Active[i].Text = CurrentScanConfigurationData.section[i].wavelength_start_nm.ToString();
                CFG_End_Active[i].Text = CurrentScanConfigurationData.section[i].wavelength_end_nm.ToString();
                CFG_Width_Active[i].SelectedIndex = CurrentScanConfigurationData.section[i].width_px - 2;
                CFG_Exposure_Active[i].SelectedIndex = CurrentScanConfigurationData.section[i].exposure_time;
                CFG_Resolution_Active[i].Text = CurrentScanConfigurationData.section[i].num_patterns.ToString();
            }
            rootPage.NotifyUser("Read current active scan config finished!", NotifyType.StatusMessage);
            Tabs.IsEnabled = true;
            rootPage.IsEnabled = true;
        }
        private async void StartSetOrSaveScanConfig(Boolean IsSet)
        {
            try
            {
                if(!IsValidConfigSet())
                {
                    rootPage.NotifyUser("Not valid parameter, please check.", NotifyType.ErrorMessage);
                    Tabs.IsEnabled = true;
                    rootPage.IsEnabled = true;
                    return;
                }
                rootPage.IsEnabled = false;
                Tabs.IsEnabled = false;
                Config.SlewScanConfig cfg = new Config.SlewScanConfig { section = new Config.SlewScanSection[5] };
                cfg.head.scan_type = Convert.ToByte(Config.SCAN_TYPE.SLEW); // This must be set to slewType
                cfg.head.config_name = CFG_Name_Set.Text.ToString();
                cfg.head.ScanConfig_serial_number = Device.Serial_Number == null ? "NULL" : Device.Serial_Number;
                cfg.head.num_repeats = Convert.ToUInt16(uint.Parse(CFG_NumAvg_Set.Text));
                cfg.head.num_sections = Convert.ToByte(CFG_NumSections_Set.SelectedIndex + 1);
                for (int i = 0; i < CFG_NumSections_Set.SelectedIndex + 1; i++)
                {
                    cfg.section[i].section_scan_type = (byte)CFG_Type_Set[i].SelectedIndex;
                    cfg.section[i].wavelength_start_nm = (ushort)Int16.Parse(CFG_Start_Set[i].Text);
                    cfg.section[i].wavelength_end_nm = (ushort)Int16.Parse(CFG_End_Set[i].Text);
                    cfg.section[i].width_px = (byte)(Helper.NM2PixelWidth((string)CFG_Width_Set[i].SelectedItem));
                    cfg.section[i].num_patterns = (ushort)Int16.Parse(CFG_Resolution_Set[i].Text);
                    cfg.section[i].exposure_time = (ushort)CFG_Exposure_Set[i].SelectedIndex;
                }
                if (IsSet)
                {
                    rootPage.NotifyUser("Set scan config, please wait...", NotifyType.StatusProcessing);
                    _ = await Client.WriteScanConfigToDevice(cfg, true, false);
                    rootPage.NotifyUser("Set scan config finished!", NotifyType.StatusMessage);
                    Tabs.IsEnabled = true;
                    rootPage.IsEnabled = true;
                }
                else
                {
                    rootPage.NotifyUser("Save scan config, please wait...", NotifyType.StatusProcessing);
                    _ = await Client.WriteScanConfigToDevice(cfg, false, true);
                    rootPage.NotifyUser("Save scan config finished!" + " Reading device scan configuration data, please wait...", NotifyType.StatusProcessing);
                    RefreshDeviceConfigDataToUI(false);
                }
            }
            catch
            {
                rootPage.NotifyUser("Not valid parameter, please check.", NotifyType.ErrorMessage);
                Tabs.IsEnabled = true;
                rootPage.IsEnabled = true;
            }          
        }
        #region Check set config value valid
        private async void CfgDetails_LostFocus(object sender, RoutedEventArgs e)
        {
            string senderName = sender.GetType().Name;
            String Msg = "";
            if (sender is TextBox cfgField)
            {
                string cfgFieldName = cfgField.Name;
                if (cfgFieldName.Contains("CFG_Name_Set"))
                {
                    if (string.IsNullOrEmpty(cfgField.Text))
                    {
                        setcfg_note.Text = "Invalid input! Config name cannot be empty.";
                        label_CFG_Name_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    label_CFG_Name_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
                    // setcfg_note.Text = "";
                }
                else if (cfgFieldName.Contains("CFG_NumAvg_Set"))
                {
                    long numAvg = 0;
                    if (!long.TryParse(cfgField.Text, out numAvg))
                    {
                        setcfg_note.Text = "Invalid input! Number average should be integer.";
                        label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    else if (numAvg == 0)
                    {
                        setcfg_note.Text = "Invalid input! Number average is Zero.";
                        cfgField.Text = "1";
                        return;
                    }
                    else if (numAvg > 255)
                    {
                        setcfg_note.Text = "Invalid input! Number average is too large.";
                        cfgField.Text = "255";
                        return;
                    }
                    label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
                    //setcfg_note.Text = "";
                }
                else if (cfgFieldName.Contains("CFG_Start_Set"))
                {
                    long rangeStart = 0;
                    if (!long.TryParse(cfgField.Text, out rangeStart))
                    {
                        setcfg_note.Text = "Invalid input! Wavelength start should be integer.";
                        label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    else if (rangeStart < minWav)
                    {
                        String errMsg = "Invalid input! Wavelength start should be greater than " + minWav.ToString();
                        setcfg_note.Text = errMsg;
                        label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
                    // setcfg_note.Text = "";
                }
                else if (cfgFieldName.Contains("CFG_End_Set"))
                {
                    long rangeEnd = 0;
                    if (!long.TryParse(cfgField.Text, out rangeEnd))
                    {
                        setcfg_note.Text = "Invalid input! Wavelength end should be integer.";
                        label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    else if (rangeEnd > maxWav)
                    {
                        String errMsg = "Invalid input! Wavelength end should be less than " + maxWav.ToString();
                        setcfg_note.Text = errMsg;
                        label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
                    // setcfg_note.Text = "";
                }
                else if (cfgFieldName.Contains("CFG_Resolution_Set"))
                {
                    ushort digiRes = 0;
                    if (!ushort.TryParse(cfgField.Text, out digiRes))
                    {
                        setcfg_note.Text = "Invalid input! Digital resolution should be integer.";
                        label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    else if (digiRes < 3)
                    {
                        setcfg_note.Text = "Invalid input! Minimum digital resolution should be equal or greater than 3.";
                        label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        return;
                    }
                    label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
                    // setcfg_note.Text = "";
                }
                if (Update_Scan_Resolution_and_Pattern_Label(ref Msg) > 624)
                {
                    /*int prevSet, patLimit;
                    int patDiff = TotalPatternUsed - PrevTotalPatternUsed;

                    int.TryParse(CfgFieldPrevValue, out prevSet);
                    if (prevSet == 0)
                        patLimit = 624 - PrevTotalPatternUsed;
                    else
                        patLimit = int.Parse(cfgField.Text) - patDiff + prevSet;

                    cfgField.Text = patLimit.ToString();*/
                    Msg += "Exceed total scan patterns limit! The max number of total patterns is 624.\n";
                    //setcfg_note.Text = "Exceed total scan patterns limit! The max number of total patterns is 624.";
                    //Update_Scan_Resolution_and_Pattern_Label(); // Refresh the UI
                    setcfg_note.Text = Msg;
                    return;
                }
                setcfg_note.Text = Msg;
            }
            else if (senderName == "ComboBox" || senderName == "MyComboBox")
            {
                int totalPatterns = Update_Scan_Resolution_and_Pattern_Label(ref Msg);
                if (totalPatterns > 624)
                {
                    Msg += "Exceed total scan patterns limit! The max number of total patterns is 624.\n";
                    // setcfg_note.Text = "Exceed total scan patterns limit! The max number of total patterns is 624.";
                    setcfg_note.Text = Msg;
                    return;
                }
                setcfg_note.Text = Msg;
            }
        }
        private Boolean IsValidConfigSet()
        {
            Boolean IsValid = true;
            String Msg = "";
            int num_sections = int.Parse(CFG_NumSections_Set.SelectedItem.ToString());
            if (string.IsNullOrEmpty(CFG_Name_Set.Text))
            {
                Msg += "Config name cannot be empty.\n";
                label_CFG_Name_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                IsValid = false;
            }
            else
                label_CFG_Name_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
            
            long numAvg = 0;
            if (!long.TryParse(CFG_NumAvg_Set.Text, out numAvg))
            {
                Msg += "Number average should be integer.\n";
                label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                IsValid = false;
            }
            else if (numAvg == 0)
            {
                Msg += "Number average is Zero.\n";
                label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                IsValid = false;
            }
            else if (numAvg > 255)
            {
                Msg += "Number average is too large.\n";
                label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                IsValid = false;
            }
            else
                label_CFG_NumAvg_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);

            label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
            label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
            label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.White);
            for (int i = 0; i < num_sections; i++)
            {
                long rangeStart = 0, rangeEnd = 0, digiRes = 0;
                if (!long.TryParse(CFG_Start_Set[i].Text, out rangeStart))
                {
                    Msg += "Wavelength start should be integer." + "(Section : " + (i+1).ToString() +  ")\n";
                    label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }
                else if (rangeStart < minWav)
                {
                    Msg += "Wavelength start should be greater than " + minWav.ToString() + "(Section : " + (i + 1).ToString() + ")\n";
                    label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }

                if (!long.TryParse(CFG_End_Set[i].Text, out rangeEnd))
                {
                    Msg += "Wavelength end should be integer." + "(Section : " + (i + 1).ToString() + ")\n";
                    label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }
                else if (rangeEnd > maxWav)
                {
                    Msg += "Wavelength end should be greater than " + maxWav.ToString() + "(Section : " + (i + 1).ToString() + ")\n";
                    label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }

                if (long.TryParse(CFG_Start_Set[i].Text, out rangeStart) && long.TryParse(CFG_End_Set[i].Text, out rangeEnd))
                {
                    if (rangeStart >= rangeEnd)
                    {
                        CFG_OverSampling_Set[i].Text = "";
                        Msg += String.Format("Start wavelength should be smaller than end wavelength!" + "(section : " + (i + 1) + ")\n");
                        label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        IsValid = false;
                    }
                    else if (rangeStart < minWav)
                    {
                        CFG_OverSampling_Set[i].Text = "";
                        Msg += String.Format("Wavelength range is not applicable to the connected device!" + "(section : " + (i + 1) + ")\n");
                        label_CFG_Start_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        IsValid = false;
                    }
                    if (rangeEnd > maxWav)
                    {
                        CFG_OverSampling_Set[i].Text = "";
                        Msg += String.Format("Wavelength range is not applicable to the connected device!" + "(section : " + (i + 1) + ")\n");
                        label_CFG_End_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                        IsValid = false;
                    }
                }

                if (!long.TryParse(CFG_Resolution_Set[i].Text, out digiRes))
                {
                    Msg += String.Format("Digital resolution should be integer!" + "(section : " + (i + 1) + ")\n");
                    label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }
                else if (digiRes < 3)
                {
                    Msg += String.Format(" Minimum digital resolution should be equal or greater than 3!" + "(section : " + (i + 1) + ")\n");
                    label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                    IsValid = false;
                }
            }

            String WarnMsg = "";
            int totalPatterns = Update_Scan_Resolution_and_Pattern_Label(ref WarnMsg);
            if (totalPatterns > 624)
            {
                Msg += "Exceed total scan patterns limit! The max number of total patterns is 624.\n";
                label_CFG_Resolution_Set.Foreground = new SolidColorBrush(Windows.UI.Colors.Pink);
                IsValid = false;
            }
            setcfg_note.Text = Msg;
            return IsValid;
        }
        private int Update_Scan_Resolution_and_Pattern_Label(ref string msg)
        {
            int num_sections = int.Parse(CFG_NumSections_Set.SelectedItem.ToString());
            int total_patterns = 0;
            PrevTotalPatternUsed = TotalPatternUsed;
            ushort rangeStart, rangeEnd, numPat;

            for (int i = 0; i < num_sections; i++)
            {
                if (!ushort.TryParse(CFG_Start_Set[i].Text, out rangeStart) || !ushort.TryParse(CFG_End_Set[i].Text, out rangeEnd))
                    break;
                if (rangeStart >= rangeEnd)
                {
                    CFG_OverSampling_Set[i].Text = "";
                    msg += String.Format("Start wavelength should be smaller than end wavelength!" + "(section : " + (i+1) + ")");
                    setcfg_note.Text = msg;
                    return -1;
                }
                if (rangeStart < minWav || rangeEnd > maxWav)
                {
                    CFG_OverSampling_Set[i].Text = "";
                    msg += String.Format("Wavelength range is not applicable to the connected device!" + "(section : " + (i + 1) + ")");
                    setcfg_note.Text = msg;
                    return -1;
                }
            }

            for (int i = 0; i < num_sections; i++)
            {
                Int32 PatternUsed = 0;
                Int32 MaxResolution = 0;

                ushort.TryParse(CFG_Start_Set[i].Text, out rangeStart);
                ushort.TryParse(CFG_End_Set[i].Text, out rangeEnd);

                SlewScanConfig cfg = new SlewScanConfig
                {
                    section = new SlewScanSection[5]
                };

                cfg.section[0].section_scan_type = byte.Parse(CFG_Type_Set[i].SelectedIndex.ToString());
                cfg.section[0].wavelength_start_nm = rangeStart;
                cfg.section[0].wavelength_end_nm = rangeEnd;
                cfg.section[0].width_px = (Byte)CfgWidthIndexToPixel(CFG_Width_Set[i].SelectedIndex);

                MaxResolution = ScanConfig_GetMaxResolutions(ref cfg, 0, Device.SpecCalCoefficients, minWav, maxWav);

                if (cfg.section[0].section_scan_type == 1 && MaxResolution > 0) // Hadamard
                {

                    Int32 HadPattern = Int32.MaxValue;

                    while (HadPattern > 624)
                    {
                        cfg.section[0].num_patterns = (ushort)MaxResolution;
                        HadPattern = ScanConfig_GetHadamardUsedPatterns(ref cfg, 0, Device.SpecCalCoefficients);
                        MaxResolution--;
                    }
                    MaxResolution++;
                }

                int patWidth = 0;
                double baseOverSampleRate = 0.0;
                if (Con_OneNM_PixWidth.FirstOrDefault(stringToCheck => stringToCheck.Contains(Device.Get_Model_Identifier())) == Device.Get_Model_Identifier())
                {
                    patWidth = (int)Math.Ceiling(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex));
                    baseOverSampleRate = (double)(Math.Ceiling((double)(rangeEnd - rangeStart) / patWidth));
                }
                else
                {
                    patWidth = (int)Math.Floor(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex));
                    baseOverSampleRate = (double)(Math.Floor((double)(rangeEnd - rangeStart) / patWidth));
                }

                if (ushort.TryParse(CFG_Resolution_Set[i].Text, out numPat) && numPat != 0)
                {
                    string s = CFG_Resolution_Set[i].Text;
                    int inputDigitalResolution = (int.Parse(s));
                    double overSampleRate = 0.0;
                    if (Con_OneNM_PixWidth.FirstOrDefault(stringToCheck => stringToCheck.Contains(Device.Get_Model_Identifier())) == Device.Get_Model_Identifier())
                        overSampleRate = (double)inputDigitalResolution / Math.Floor((double)(rangeEnd - rangeStart) / Math.Ceiling(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex)));
                    else
                        overSampleRate = (double)inputDigitalResolution / Math.Floor((double)(rangeEnd - rangeStart) / Math.Floor(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex)));

                    if ((numPat > MaxResolution) || (numPat > Math.Floor(baseOverSampleRate * 4.5)))
                    {
                        if ((int)Math.Floor(baseOverSampleRate * 4.5) > MaxResolution)
                        {
                            msg += String.Format("Exceed the section max resolution! (max = {0})", MaxResolution) + "(section : " + (i + 1) + ")\n";
                            numPat = (ushort)MaxResolution;
                            cfg.section[0].num_patterns = (ushort)numPat;
                            if (Con_OneNM_PixWidth.FirstOrDefault(stringToCheck => stringToCheck.Contains(Device.Get_Model_Identifier())) == Device.Get_Model_Identifier())
                                overSampleRate = (double)(numPat / Math.Floor((double)(rangeEnd - rangeStart) / Math.Ceiling(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex))));
                            else
                                overSampleRate = (double)(numPat / Math.Floor((double)(rangeEnd - rangeStart) / Math.Floor(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex))));
                        }
                        else
                        {
                            msg = String.Format("Exceed the max oversampling rate! (max = 4.5, set = {0:F1}x)", overSampleRate);
                            numPat = (ushort)Math.Floor(baseOverSampleRate * 4.5);
                            cfg.section[0].num_patterns = (ushort)numPat;
                            if (Con_OneNM_PixWidth.FirstOrDefault(stringToCheck => stringToCheck.Contains(Device.Get_Model_Identifier())) == Device.Get_Model_Identifier())
                                overSampleRate = (double)(numPat / Math.Floor((double)(rangeEnd - rangeStart) / Math.Ceiling(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex))));
                            else
                                overSampleRate = (double)(numPat / Math.Floor((double)(rangeEnd - rangeStart) / Math.Floor(Helper.CfgWidthIndexToNM(CFG_Width_Set[i].SelectedIndex))));
                        }
                    }
                    overSampleRate = Math.Round(overSampleRate, 1);

                    if ((overSampleRate > 3 || overSampleRate < 2) && patWidth > 7)
                    {
                        int upperLimit = (int)baseOverSampleRate * 3;
                        upperLimit = upperLimit > MaxResolution ? MaxResolution : upperLimit;
                        msg += String.Format("The recommended oversampling is between 2.0 ~ 3.0 for this pattern width setting\ni.e. Digital resolution should be between {0:F0} ~ {1:F0}", baseOverSampleRate * 2, upperLimit) + "(section : " + (i + 1) + ")\n";                     
                    }
                    else if (overSampleRate < 2 && patWidth > 4 && patWidth < 8)
                    {                      
                        msg += String.Format("The recommended oversampling is above 2.0 for this pattern width setting\ni.e. Digital resolution should be between {0:F0} ~ {1:F0}", baseOverSampleRate * 2, MaxResolution) + "(section : " + (i + 1) + ")\n";
                    }
                    else
                    {
                        setcfg_note.Text = "";
                    }

                    CFG_OverSampling_Set[i].Text = overSampleRate.ToString("F1");

                    if (cfg.section[0].section_scan_type > 0)
                        PatternUsed = ScanConfig_GetHadamardUsedPatterns(ref cfg, 0, Device.SpecCalCoefficients);
                    else
                        PatternUsed = numPat;

                    total_patterns += PatternUsed;
                }
            }          
            TotalPatternUsed = total_patterns;
            return total_patterns;
        }

        [DllImport("DLPSpecLib.dll", EntryPoint = "CfgWidthIndexToPixel", ExactSpelling = false)]
        public static extern Int32 CfgWidthIndexToPixel(Int32 Index);
        [DllImport("DLPSpecLib.dll", EntryPoint = "ScanConfig_GetMaxResolutions", ExactSpelling = false)]
        private static extern Int32 ScanConfig_GetMaxResolutions(ref SlewScanConfig scanCfg, Int32 section, byte[] SpecCalCoefficients,int minWav,int maxWav);
        [DllImport("DLPSpecLib.dll", EntryPoint = "ScanConfig_GetHadamardUsedPatterns", ExactSpelling = false)]
        private static extern Int32 ScanConfig_GetHadamardUsedPatterns(ref SlewScanConfig scanCfg, Int32 section, byte[] SpecCalCoefficients);
        #endregion
    }
}
