using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ISC_Win_CS_LIB;
using LabinLightApi.Models;
using LabinLightScan.ServiceClient;
using static ISC_Win_CS_LIB.ScanConfig;
using System.Windows.Threading;
using System.Windows.Documents;
using System.Globalization;
using System.Linq.Expressions;
using Newtonsoft.Json.Linq;
using static System.Runtime.CompilerServices.RuntimeHelpers;
using System.Xml.Linq;
using System.Windows.Data;
using System.Windows.Media.Animation;
using LabinLightScan.services;
using System.Runtime.InteropServices;
using System.Windows.Input;
using System.Windows.Automation.Peers;
using System.Text;
using System.Text.RegularExpressions;

namespace LabinLightScan
{

    public static class GlobalData
    {
        public static int RepeatedScanCountDown = 0;
        public static int ScannedCounts = 0;
        public static int TargetScanNumber = 0;
        public static bool UserCancelRepeatedScan = false;

    }

    public partial class MainWindow : Window
    {
        #region Declarations

        public System.Collections.ObjectModel.ObservableCollection<AnalysisType> AnalysisTypesList { get; set; } = new System.Collections.ObjectModel.ObservableCollection<AnalysisType>();
        public static String ConfigDir { get; set; }
        private static int UserSelectedDeviceIndex;

        private static DeviceConfigLog DevConfigLog;

        public delegate void ClearScanPlots();
        public static event ClearScanPlots ClearScanPlotsEvent;

        public static event Action<int> OnScanGUIControl = null;
        private static int SendScanGUIEvent { set { OnScanGUIControl(value); } }

        public static event Action<int> OnUtilityGUIControl = null;
        private static int SendUtilityGUIEvent { set { OnUtilityGUIControl(value); } }

        private Scan.SCAN_REF_TYPE ReferenceSelect = Scan.SCAN_REF_TYPE.SCAN_REF_NEW;



        public enum GUI_State
        {
            DEVICE_ON,
            DEVICE_ON_SCANTAB_SELECT,
            DEVICE_OFF,
            DEVICE_OFF_SCANTAB_SELECT,
            SCAN,
            SCAN_FINISHED,
            FW_UPDATE,
            FW_UPDATE_FINISHED,
            REFERENCE_DATA_UPDATE,
            REFERENCE_DATA_UPDATE_FINISHED,
            KEY_ACTIVATE,
            KEY_NOT_ACTIVATE,
        };

        #endregion

        // Background worker for Performing Scan
        BackgroundWorker bwScan;

        // Background worker for gui
        BackgroundWorker bw = new BackgroundWorker();
        BackgroundWorker PBbw = new BackgroundWorker();


        //Progress Bar
        DispatcherTimer _timerPB;
        public string selectedAnalysisBundle { get; set; } = "global";
        public string birthYear { get; set; }
        public string processNbr { get; set; }
        public string selectedGender { get; set; }
        public string selectedPatientIdCountry  { get; set; }
        public string patientId { get; set; }
        public string patientContact { get; set; }

        public LabinLightApi.Models.SlewScanConfig ServerConfiguration = null;

        bool deviceIsConnected = false;
        bool serverIsAvailable = false;
        volatile bool deviceIsBusy = false;
        bool serverCommIsRunning = false;
        public bool deviceIsCold = true;
        public double deviceTemp = 0;

        public bool deviceIsUnavailable = false;

        String deviceStatusInfo = "";


        private void LoadHandler(object sender, System.EventArgs e) => initForm();
        public MainWindow()
        {
            InitializeComponent();


            this.DataContext = this;

            String version = Assembly.GetExecutingAssembly().GetName().Version.ToString();
            version = version.Substring(0, version.LastIndexOf('.'));

            // Setup the MainWindow Position to center desktop screen
            var desktopWorkingArea = SystemParameters.WorkArea;
            double thisLeft = desktopWorkingArea.Right - this.Width;
            if (thisLeft < 0)
                thisLeft = 0;
            else
                thisLeft /= 2;
            double thisTop = desktopWorkingArea.Bottom - this.Height;
            if (thisTop < 0)
                thisTop = 0;
            else
                thisTop /= 2;
            this.Left = thisLeft;
            this.Top = thisTop;

            Loaded += new RoutedEventHandler(MainWindow_Loaded);

            StatusIcon(Image_ServerStatusIcon, 0);
            StatusIcon(Image_DeviceStatusIcon, 0);
            initCheckServerStatus();
            initCheckDeviceTemp();


            // Enable the CPP DLL debug output for development
           // DBG.Enable_CPP_Console();



            //Grid_MainWin.Children.Add(scanPage);
            bw.DoWork += bw_DoWork;
            bw.RunWorkerCompleted += Bw_RunWorkerCompleted;
            bw.ProgressChanged += Bw_ProgressChanged;
            bw.WorkerReportsProgress = true;


            ResultsCsvExporter.instance.setup();
            initForm();
        }

        private void MainWindow_GUI_Handler(int state)
        {
            Boolean isEnable = false;

            switch (state)
            {
                case (int)GUI_State.DEVICE_ON:
                case (int)GUI_State.DEVICE_OFF:
                    {
                        String HWRev = String.Empty;
                        if (Device.IsConnected())
                            HWRev = (!String.IsNullOrEmpty(Device.DevInfo.HardwareRev)) ? Device.DevInfo.HardwareRev.Substring(0, 1) : String.Empty;

                        if ((IsOldTivaFW() && HWRev == "D") || (!IsOldTivaFW() && HWRev != "A" && HWRev != String.Empty))
                        {
                        }
                        else
                        {
                        }

                        if (state == (int)GUI_State.DEVICE_ON)
                            isEnable = true;
                        else
                            isEnable = false;
                        break;
                    }
                case (int)GUI_State.SCAN:
                case (int)GUI_State.SCAN_FINISHED:
                    {
                        if (state == (int)GUI_State.SCAN)
                            isEnable = false;
                        else
                            isEnable = true;
                        break;
                    }

                case (int)GUI_State.FW_UPDATE:
                case (int)GUI_State.FW_UPDATE_FINISHED:
                    {
                        if (state == (int)GUI_State.FW_UPDATE)
                            isEnable = false;
                        else
                            isEnable = true;
                        break;
                    }
                case (int)GUI_State.REFERENCE_DATA_UPDATE:
                case (int)GUI_State.REFERENCE_DATA_UPDATE_FINISHED:
                    {
                        if (state == (int)GUI_State.REFERENCE_DATA_UPDATE)
                            isEnable = false;
                        else
                            isEnable = true;
                        break;
                    }
                default:
                    break;
            }
        }

        #region Initial Components

        private void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            Device.Init();
            this.WindowState = WindowState.Minimized;
            Show();
            Focus();
            this.WindowState = WindowState.Normal;



            //MenuItem_SelectDevice.MouseEnter += new System.Windows.Input.MouseEventHandler(Enumerate_Devices);
            SDK.OnDeviceConnectionLost += new Action<bool>(Device_Disconncted_Handler);
            SDK.OnDeviceConnected += new Action<string>(Device_Connected_Handler);
            SDK.OnDeviceFound += new Action(Device_Found_Handler);
            SDK.OnDeviceError += new Action<string>(Device_Error_Handler);
            SDK.OnErrorStatusFound += new Action(RefreshErrorStatus);
            SDK.OnBeginConnectingDevice += new Action<string>(Connecting_Device);
            SDK.OnBeginScan += new Action(BeginScan);
            SDK.OnScanCompleted += new Action(ScanCompleted);
            SDK.OnUSBConnectionBusy += new Action(USBIsBusy);

            // ScanPage.OnMainGUIControl += new Action<int>(MainWindow_GUI_Handler);
            //UtilityPage.OnMainGUIControl += new Action<int>(MainWindow_GUI_Handler);

            if (!Device.IsConnected())
            {
                SDK.AutoSearch = true;
                MainWindow_GUI_Handler((int)GUI_State.DEVICE_OFF);
            }
            // Setting the interval that checks the USB connection and open device deley
            SDK.ConnectionCheckInterval = 2000;
            SDK.DeviceOpenDeley = 1000;

            LoadSettings();
        }

        private void LoadSettings()
        {
            // Config Directory
            String path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            ConfigDir = Path.Combine(path, "LabInLight\\Config Data");

            if (Directory.Exists(ConfigDir) == false)
            {
                Directory.CreateDirectory(ConfigDir);
                DBG.WriteLine("The directory {0} was created.", ConfigDir);
            }
        }

        private void CheckFactoryRefData()
        {
            String FacRefFile = Device.DevInfo.SerialNumber + "_FacRef.dat";
            String path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            String FilePath = Path.Combine(path, "LabInLight\\Reference Data", FacRefFile);

            if (!File.Exists(FilePath))
                MenuItem_BackupFacRef_Click(null, null);
        }

        private String IsCfgValidForSaveToDevice(ScanConfig.SlewScanConfig cfg)
        {
            String ret = "";
            Int32 TotalPatterns = 0;

            // Config Name
            if (cfg.head.config_name == String.Empty)
                ret = "-1";

            // Num Scans to Average
            if (cfg.head.num_repeats == 0)
                ret = "-2";

            // Sections
            for (Byte i = 0; i < cfg.head.num_sections; i++)
            {
                // Start nm
                if (cfg.section[i].wavelength_start_nm < Device.DevInfo.MinWavelength)
                    ret = "-3 <"+ Device.DevInfo.MinWavelength  + ">";

                // End nm
                if (cfg.section[i].wavelength_end_nm > Device.DevInfo.MaxWavelength || cfg.section[i].wavelength_end_nm < Device.DevInfo.MinWavelength)
                    ret = "-4 <" + Device.DevInfo.MinWavelength + ", "+ Device.DevInfo.MaxWavelength + ">";
                if (cfg.section[i].wavelength_start_nm >= cfg.section[i].wavelength_end_nm)
                    ret = "-5";

                // Check Max Patterns
                Int32 MaxPattern = 0;
                Int32 HadPattern = 0;

                MaxPattern = ScanConfig.GetMaxResolutions(cfg, i);
                if ((cfg.section[i].section_scan_type == 0 && cfg.section[i].num_patterns < 2) ||  // Column Mode
                    (cfg.section[i].section_scan_type == 1 && cfg.section[i].num_patterns < 3) ||  // Hadamard Mode
                    (cfg.section[i].num_patterns > MaxPattern) ||
                    (MaxPattern <= 0))
                {
                    ret = "-6 <" + MaxPattern + ">";
                    if (MaxPattern < 0) MaxPattern = 0;
                }
                else
                {
                    HadPattern = ScanConfig.GetHadamardUsedPatterns(cfg, i);

                    if (cfg.section[i].num_patterns > MaxPattern)
                        ret = "-7 <" + MaxPattern + ">";
                }

                if (HadPattern != -1)
                    TotalPatterns += HadPattern;
                else
                    TotalPatterns += cfg.section[i].num_patterns;
            }

            // Check total patterns
            if (TotalPatterns > 624)
                ret = "-8";

            return ret;
        }

        private String checkAndUpdateDeviceConfig(String SerialNumber, List<String> errors, List<String> infos)
        {
            String ConfigName = "";
            DevConfigLog = new DeviceConfigLog();
            try
            {
                ServerConfiguration = LabinLightReadingsServiceClient.instance.GetDeviceConfig(SerialNumber);
            }
            catch (Exception e)
            {
                errors.Add("Erro a obter configuração de dispositivo. O processo irá continuar com configuração predefinida no Hemospec.");
            }
            if (ServerConfiguration != null)
            {
                try
                {
                    ConfigName = ServerConfiguration.Head.config_name;
                    DevConfigLog.ConfigToSet = ConfigName;
                    DevConfigLog.Result = -1;
                    DevConfigLog.DefaultResult = -1;
                    var expectedConfigName = LabinLightReadingsServiceClient.ServerName + "_" + ServerConfiguration.id + "_";
                    DevConfigLog.ConfigToSetId = expectedConfigName + ServerConfiguration.LastUpdate;

                    Regex r = new Regex(LabinLightReadingsServiceClient.ServerName + "_[0-9]+_([0-9]+)", RegexOptions.IgnoreCase);

                    int activeScanIndex = ScanConfig.GetTargetActiveScanIndex();
                    int targetScanIndex = -1;

                    bool validConfigFound = false;
                    List<ScanConfig.SlewScanConfig> toRemove = new List<ScanConfig.SlewScanConfig>();
                    for (int i = 0; i < ScanConfig.TargetConfig.Count; i++)
                    {
                        ScanConfig.SlewScanConfig cfg = ScanConfig.TargetConfig[i];
                        toRemove.Add(cfg);
                        DevConfigLog.FoundConfigs.Add(cfg.head.config_name);
                        var nameMatch = r.Match(cfg.head.config_name);
                        if (nameMatch.Success)
                        {
                            if (cfg.head.config_name.StartsWith(expectedConfigName))
                            {
                                var timestamp = Int32.Parse(nameMatch.Groups[1].Value);
                                bool valid = timestamp == ServerConfiguration.LastUpdate;
                                validConfigFound |= valid;
                                if (valid)
                                {
                                    toRemove.Remove(cfg);
                                    targetScanIndex = i;
                                }

                            }
                        }
                    }
                    if (!validConfigFound)
                    {
                        ScanConfig.SlewScanConfig newCfg = new ScanConfig.SlewScanConfig
                        {
                            section = new ScanConfig.SlewScanSection[5]
                        };

                        newCfg.head.config_name = expectedConfigName + ServerConfiguration.LastUpdate;
                        newCfg.head.ScanConfig_serial_number = SerialNumber;
                        newCfg.head.scan_type = 2;
                        newCfg.head.num_sections = Convert.ToByte(ServerConfiguration.Section.Length);
                        newCfg.head.num_repeats = Convert.ToUInt16(ServerConfiguration.Head.num_repeats);

                        for (Int32 i = 0; i < 5; i++)
                        {
                            int serverIdx = ServerConfiguration.Section.Length > i ? i : ServerConfiguration.Section.Length - 1;
                            newCfg.section[i].wavelength_start_nm = Convert.ToUInt16(ServerConfiguration.Section[serverIdx].wavelength_start_nm);
                            newCfg.section[i].wavelength_end_nm = Convert.ToUInt16(ServerConfiguration.Section[serverIdx].wavelength_end_nm);
                            newCfg.section[i].num_patterns = Convert.ToUInt16(ServerConfiguration.Section[serverIdx].num_patterns);
                            newCfg.section[i].section_scan_type = ServerConfiguration.Section[serverIdx].section_scan_type;
                            newCfg.section[i].width_px = ServerConfiguration.Section[serverIdx].width_px;
                            newCfg.section[i].exposure_time = Convert.ToUInt16(ServerConfiguration.Section[serverIdx].exposure_time);
                        }
                        String validCfgRet = IsCfgValidForSaveToDevice(newCfg);
                        if (!validCfgRet.Equals(""))
                        {
                            errors.Add("A configuração não é válida para este dispositivo, contacte-nos. Erro: " + validCfgRet);
                        }
                        else
                        {
                            ScanConfig.TargetConfig.Add(newCfg);
                            foreach (ScanConfig.SlewScanConfig toRemoveCfg in toRemove)
                            {
                                ScanConfig.TargetConfig.Remove(toRemoveCfg);
                            }
                            ScanConfig.SetTargetActiveScanIndex(0);
                            if (ScanConfig.SetConfigList() != 0)
                            {
                                DevConfigLog.Result = 1;
                                errors.Add("Não foi possivel atualizar a configuração do HemoSpec");
                            }
                            else
                            {
                                ScanConfig.SetTargetActiveScanIndex(ScanConfig.TargetConfig.IndexOf(newCfg));
                                Console.WriteLine("Write done");
                                infos.Add("Configuração do HemoSpec atualizada.");
                                DevConfigLog.Result = 0;
                            }
                        }

                    }
                    else if (targetScanIndex != activeScanIndex)
                    {
                        if (ScanConfig.SetTargetActiveScanIndex(targetScanIndex) <= 0)
                        {
                            DevConfigLog.DefaultResult = 1;
                            errors.Add("Não foi possivel atualizar a configuração do HemoSpec. Código: -2");
                        }
                        else
                        {
                            DevConfigLog.DefaultResult = 0;
                            if (infos.Count == 0)
                            {
                                infos.Add("Configuração do HemoSpec atualizada.");
                            }
                        }
                    }
                    DevConfigLog.Default = ScanConfig.TargetConfig[ScanConfig.GetTargetActiveScanIndex()].head.config_name;
                    LabinLightReadingsServiceClient.instance.SendDeviceConfigLogs(DevConfigLog);

                }
                catch (Exception e)
                {
                    errors.Add("Erro a guardar nova configuração no Hemospec.");
                }
            }
            return ConfigName;

        }

        private async void Device_Connected_Handler(String SerialNumber)
        {
            this.IsEnabled = false;
            Console.WriteLine("Device_Connected_Handler");
            if (SerialNumber == null)
            {
                DBG.WriteLine("Device connecting failed !");
            }
            else
            {
                String ModelName = Device.DevInfo.ModelName;
                DBG.WriteLine("Device <{0}> connected successfullly !", SerialNumber);

                String HWRev = (!String.IsNullOrEmpty(Device.DevInfo.HardwareRev)) ? Device.DevInfo.HardwareRev.Substring(0, 1) : String.Empty;
                if (Device.ChkBleExist() == 1)
                    Device.SetBluetooth(false);
                if ((IsOldTivaFW() && HWRev == "D") || (!IsOldTivaFW() && HWRev != "A" && HWRev != String.Empty))
                    CheckFactoryRefData();

                Device.DeviceDateTime DevDateTime = new Device.DeviceDateTime();
                DateTime Current = DateTime.Now;

                DevDateTime.Year = Current.Year;
                DevDateTime.Month = Current.Month;
                DevDateTime.Day = Current.Day;
                DevDateTime.DayOfWeek = (Int32)Current.DayOfWeek;
                DevDateTime.Hour = Current.Hour;
                DevDateTime.Minute = Current.Minute;
                DevDateTime.Second = Current.Second;
                Device.SetDateTime(DevDateTime);

                List<String> cfgErrors = new List<String>();
                List<String> cfgInfos = new List<String>();
                String ConfigName = "<NA>";
                String ServerConfigName = "";

                deviceIsBusy = true;
                await Dispatcher.InvokeAsync(() =>
                {
                    MainWindow_GUI_Handler((int)GUI_State.DEVICE_ON);
                    Device.ReadErrorStatusAndCode();

                    Console.WriteLine("start update");
                    ServerConfigName = checkAndUpdateDeviceConfig(SerialNumber, cfgErrors, cfgInfos);
                    Console.WriteLine("update done");
                    ScanConfig.SetScanConfig(ScanConfig.TargetConfig[ScanConfig.GetTargetActiveScanIndex()]);
                    Console.WriteLine("Set Scan Config");

                    if (cfgErrors.Count == 0)
                    {
                        var devCfgId = ScanConfig.TargetConfig[ScanConfig.GetTargetActiveScanIndex()].head.config_name;
                        if (DevConfigLog.ConfigToSetId.Equals(devCfgId)){
                            ConfigName = ServerConfigName;
                        }
                        else
                        {
                            cfgErrors.Add("Erro a atribuir configuração ao Hemospec. "+devCfgId+" "+ DevConfigLog.ConfigToSetId);
                        }
                    }

                    deviceStatusInfo = "Hemospec " + ModelName + " (" + SerialNumber + ") Config: " + ConfigName;
                    StatusBarItem_DeviceStatus.Content = deviceStatusInfo;

                    this.Title = "LabInLight - " + DevConfigLog.AppVersion;

                    StatusIcon(Image_DeviceStatusIcon, 1);
                    ProgressWindowCompleted();

                    Console.WriteLine("will change...");
                });

                deviceIsConnected = true;
                Console.WriteLine("done waiting.");

                this.IsEnabled = true;
                
                if (cfgErrors.Count != 0)
                {
                    ShowWarning(string.Join("; ", cfgErrors));
                }
                if (cfgInfos.Count != 0)
                {
                   ShowInfo(string.Join("; ", cfgInfos));
                }
                

                deviceIsBusy = false;
                deviceIsConnected = true;
                deviceIsCold = true;
                var calibWin = new CalibrationWindow(SerialNumber, this) { Owner = this };
                var result = calibWin.ShowDialog();
                if (calibWin.DeviceIsUnavailable)
                {
                    deviceIsUnavailable = true;
                }
                checkDeviceTemp();
                

            }

        }

        private async void USBIsBusy()
        {
            Console.WriteLine("USBIsBusy");
            deviceIsBusy = true;
            this.IsEnabled = false;
            await Dispatcher.InvokeAsync(() =>
            {
                Thread.Sleep(1000);
                this.IsEnabled = true;
                deviceIsBusy = false;
            });
        }
        private void Device_Disconncted_Handler(bool error)
        {
            Console.WriteLine("Device_Disconncted_Handler");
            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                MainWindow_GUI_Handler((int)GUI_State.DEVICE_OFF);
                //MenuItem_SelectDevice.Items.Clear();
                StatusIcon(Image_DeviceStatusIcon,0);

                deviceIsConnected = false;
                deviceStatusInfo = "";
                StatusBarItem_DeviceStatus.Content = "Hemospec desconectado!";
            }));

            if (error)
            {
                DBG.WriteLine("Device disconnected abnormally !");
                if (SDK.IsUsbConnectionBusy)  // USB connection busy from other devices.
                {
                    MessageBoxResult input = ShowQuestion("Device is busy connected from others.\n" +
                                                          "If you want to connect automatically when the device is available, please press \"YES\"!\n" +
                                                          "If you want to connect manually, please press \"NO\"!\n" +
                                                          "If you want to quit the system, please press \"CANCEL\"!", MessageBoxButton.YesNoCancel);
                    if (input == MessageBoxResult.Yes)
                    {
                        SDK.AutoSearch = true;
                    }
                    else if (input == MessageBoxResult.No)
                    {
                        SDK.AutoSearch = false;
                        ShowInfo("The system will not connect to the device automatically, you have to operate manually!");
                    }
                    else
                    {
                        Environment.Exit(0);
                    }
                }
                else
                {
                    SDK.AutoSearch = true;
                    ShowWarning("Nenhum Hemospec encontrado!");
                }
            }
            else
            {
                DBG.WriteLine("Device disconnected successfully !");
            }
        }

        private void Device_Found_Handler()
        {

            Console.WriteLine("Device_Found_Handler");
            SDK.AutoSearch = false;
            Dispatcher.Invoke((Action)delegate ()
            {
                Enumerate_Devices(null, null);
            });
        }

        private void Device_Error_Handler(string error) //erro inicial da leitura da info do device
        {
            // ShowWarning(error);

            Console.WriteLine("Device_Error_Handler" + error);
        }

        #endregion

        private void MenuItem_Scan_Click(object sender, RoutedEventArgs e)
        {

        }
        private void MenuItem_Utility_Click(object sender, RoutedEventArgs e)
        {

        }

        private void MenuItem_Info_Click(object sender, RoutedEventArgs e)
        {

        }

        #region Reset System

        private void MenuItem_ResetSys_Click(object sender, RoutedEventArgs e)
        {
            if (!Device.IsConnected())
                return;

            bwTivaReset = new BackgroundWorker
            {
                WorkerReportsProgress = false,
                WorkerSupportsCancellation = true
            };
            bwTivaReset.DoWork += new DoWorkEventHandler(bwTivaReset_DoWork);
            bwTivaReset.RunWorkerCompleted += new RunWorkerCompletedEventHandler(bwTivaReset_DoWorkCompleted);

            MessageBoxResult input = ShowQuestion("Are you sure to RESET system?", MessageBoxButton.OKCancel);
            if (input == MessageBoxResult.OK)
            {
                SDK.IsConnectionChecking = false;
                //SendScanGUIEvent = (int)GUI_State.DEVICE_OFF_SCANTAB_SELECT;
                bwTivaReset.RunWorkerAsync();
            }
        }

        public class ConfigurationData : INotifyPropertyChanged
        {
            private ushort _scanAvg;
            public ushort ScanAvg
            {
                get { return _scanAvg; }
                set { _scanAvg = value; OnPropertyChanged(nameof(ScanAvg)); }
            }

            private byte _pgaGain;
            public byte PGAGain
            {
                get { return _pgaGain; }
                set { _pgaGain = value; OnPropertyChanged(nameof(PGAGain)); }
            }

            private int _repeatedScanCountDown;
            public int RepeatedScanCountDown
            {
                get { return _repeatedScanCountDown; }
                set
                {
                    GlobalData.RepeatedScanCountDown = value;
                    _repeatedScanCountDown = value;
                    OnPropertyChanged(nameof(RepeatedScanCountDown));
                }
            }

            private int _scanInterval;
            public int ScanInterval
            {
                get { return _scanInterval; }
                set { _scanInterval = value; OnPropertyChanged(nameof(ScanInterval)); }
            }

            private int _scanedCounts;
            public int scanedCounts
            {
                get { return _scanedCounts; }
                set
                {
                    GlobalData.ScannedCounts = value;
                    _scanedCounts = value;
                }
            }

            private int _scanCountsTarget;
            public int scanCountsTarget
            {
                get { return _scanCountsTarget; }
                set
                {
                    GlobalData.TargetScanNumber = value;
                    _scanCountsTarget = value;
                }
            }

            public event PropertyChangedEventHandler PropertyChanged;
            protected void OnPropertyChanged(string name)
            {
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
            }

            // Chart 
            //public SeriesCollection SeriesCollection { get; set; }
            private string _ZoomButtonTitle;
            public string ZoomButtonTitle
            {
                get { return _ZoomButtonTitle; }
                set { _ZoomButtonTitle = value; OnPropertyChanged("ZoomButtonTitle"); }
            }

            public string _DataTooltipButtonTitle;
            public string DataTooltipButtonTitle
            {
                get { return _DataTooltipButtonTitle; }
                set { _DataTooltipButtonTitle = value; OnPropertyChanged("DataTooltipButtonTitle"); }
            }

            private string _ZoomButtonBackground;
            public string ZoomButtonBackground
            {
                get { return _ZoomButtonBackground; }
                set { _ZoomButtonBackground = value; OnPropertyChanged("ZoomButtonBackground"); }
            }

            public string _DataTooltipButtonBackground;
            public string DataTooltipButtonBackground
            {
                get { return _DataTooltipButtonBackground; }
                set { _DataTooltipButtonBackground = value; OnPropertyChanged("DataTooltipButtonBackground"); }
            }
        }

        private BackgroundWorker bwTivaReset;
        private static void bwTivaReset_DoWork(object sender, DoWorkEventArgs e)
        {
            int ret = Device.ResetTiva(false);
        }
        private static void bwTivaReset_DoWorkCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            SDK.IsConnectionChecking = true;
        }

        #endregion

        #region Update Reference Data



        private ProgressWindow pbw = null;
        public static double ProgressWindow_left = 0;//record the last window position when move
        public static double ProgressWindow_top = 0;//record the last window position when move
        private void ProgressWindowStart(string msg, bool buttonEnabled)
        {
            Thread t = new Thread(delegate ()
            {
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
                {
                    pbw = new ProgressWindow
                    {
                        info = msg,
                        ButtonEnabled = buttonEnabled,
                        Owner = this
                    };
                    pbw.ShowDialog();
                }));
            })
            {
                Priority = ThreadPriority.Highest
            };
            t.Start();
        }

        private void ProgressWindowCompleted()
        {
            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                try
                {
                    if(pbw != null )
                    {
                        pbw.Close();
                    }
                }
                catch { }
            }));
        }

        #endregion

        #region Select Device

        private void Enumerate_Devices(object sender, System.Windows.Input.MouseEventArgs e)
        {
            String callerName;
            if (sender != null)
                callerName = sender.GetType().ToString();
            else
                callerName = "";

            Device.Enumerate();

            String ConnectedName = String.Empty;
            if (Device.IsConnected())
            {
                if (Convert.ToInt32(Device.DevInfo.TivaRev[0]) == 2 && Convert.ToInt32(Device.DevInfo.TivaRev[1]) == 0)
                    ConnectedName = "Nirscan Nano (12345678)";
                else
                    ConnectedName = Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ")";
            }

            //MenuItem_SelectDevice.Items.Clear();

            int i = 0;
            if ((callerName == "DLP_NIR_Win_SDK_App_CS.MainWindow" || callerName == "") && Device.DeviceCounts == 1)
            {
                Device.Open(Device.DeviceFound[0].SerialNumber);
                String strHeader = Device.DeviceFound[0].ProductString + " (" + Device.DeviceFound[0].SerialNumber + ")";
                MenuItem DevItem = new MenuItem { Header = strHeader };
                DevItem.Click += new RoutedEventHandler(MenuItem_Device_Connect_Click);
                //MenuItem_SelectDevice.Items.Add(DevItem);
                ConnectedName = strHeader;
                DevItem.IsChecked = true;
            }
            else if (callerName != "DLP_NIR_Win_SDK_App_CS.MainWindow" && Device.DeviceCounts >= 1)
            {
                for (i = 0; i < Device.DeviceCounts; i++)
                {
                    DBG.WriteLine("USB Device [{0}]: Product Name --> {1}", i, Device.DeviceFound[i].ProductString);
                    DBG.WriteLine("USB Device [{0}]: Serial Numer --> {1}", i, Device.DeviceFound[i].SerialNumber);

                    String strHeader = Device.DeviceFound[i].ProductString + " (" + Device.DeviceFound[i].SerialNumber + ")";

                    MenuItem DevItem = new MenuItem
                    {
                        Header = strHeader
                    };
                    DevItem.Click += new RoutedEventHandler(MenuItem_Device_Connect_Click);
                    //MenuItem_SelectDevice.Items.Add(DevItem);

                    if (ConnectedName == strHeader)
                        DevItem.IsChecked = true;
                }
            }
            else
                return;

            if (callerName == "DLP_NIR_Win_SDK_App_CS.MainWindow" || callerName == "" && Device.DeviceCounts > 1)
            {
                DeviceWindow deviceWindow = new DeviceWindow { Owner = this };
                deviceWindow.UserSelection += value => UserSelectedDeviceIndex = value;
                deviceWindow.ShowDialog();
                String serNum = Device.DeviceFound[UserSelectedDeviceIndex].ProductString + " (" + Device.DeviceFound[UserSelectedDeviceIndex].SerialNumber + ")";
                string[] SerNum = serNum.Split(new char[] { '(', ')' }, StringSplitOptions.RemoveEmptyEntries);
                Device.Open(SerNum[1]);
                //foreach (MenuItem MyMI in MenuItem_SelectDevice.Items)
                //    if ((string)MyMI.Header == serNum)
                //        MyMI.IsChecked = true;
            }
        }

        private void MenuItem_Device_Connect_Click(object sender, RoutedEventArgs e)
        {
            var MI = sender as MenuItem;
            String ItemName = MI.Header.ToString();
            Int32 SerNumIndexStart = ItemName.IndexOf('(') + 1;
            String Model = ItemName.Substring(0, SerNumIndexStart - 2);
            String SerNum;

            if (MI.IsChecked == true && Device.IsConnected())
                ShowWarning("Device has been already connected!");
            else
            {
                if (Device.IsConnected())
                {
                    // Manual control GUI when device closed
                    ClearScanPlotsEvent();
                    //SendScanGUIEvent = (int)GUI_State.DEVICE_OFF_SCANTAB_SELECT;
                    //SendScanGUIEvent = (int)GUI_State.DEVICE_OFF;
                    //SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    //SendUtilityGUIEvent = (int)GUI_State.DEVICE_OFF;
                    //SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    MainWindow_GUI_Handler((int)GUI_State.DEVICE_OFF);
                    //MenuItem_SelectDevice.Items.Clear();
                    StatusIcon(Image_DeviceStatusIcon,0);

                    deviceIsConnected = false;
                    deviceStatusInfo = "";
                    StatusBarItem_DeviceStatus.Content = "Hemospec desconectado!";

                    if (Device.ChkBleExist() == 1)
                        Device.SetBluetooth(true);

                    SDK.IsEnableNotify = false;  // Disable GUI notify
                    Device.Close();
                }

                //foreach (MenuItem MyMI in MenuItem_SelectDevice.Items)
                //    MyMI.IsChecked = false;

                if (Model == "Nirscan Nano")
                    SerNum = ItemName.Substring(SerNumIndexStart, 8);
                else
                    SerNum = ItemName.Substring(SerNumIndexStart, 7);

                SDK.IsEnableNotify = true;  // Enable GUI notify
                Device.Open(SerNum);
                MI.IsChecked = true;
            }
        }

        #endregion

        #region Advance

        private void MenuItem_BackupFacRef_Click(object sender, RoutedEventArgs e)
        {
            if (Device.IsConnected())
            {
                int ret;
                string serNum = Device.DevInfo.SerialNumber.ToString();
                ret = Device.Backup_Factory_Reference(serNum);
                if (ret < 0)
                {
                    switch (ret)
                    {
                        case -1:
                            ShowError("Factory reference data backup FAILED!\n\nOut of memory.");
                            break;
                        case -2:
                            ShowError("Factory reference data backup FAILED!\n\nSystem I/O error");
                            break;
                        case -3:
                            ShowError("Factory reference data backup FAILED!\n\nDevice communcation error");
                            break;
                        case -4:
                            ShowError("Factory reference data backup FAILED!\n\nDevice does not have the original factory reference data");
                            break;
                    }
                }
                else
                    ShowInfo("Factory reference data has been saved in local storage successfully!");
            }
            else
                ShowError("No device connected for backup factory reference!");
        }

        private void MenuItem_RestoreFacRef_Click(object sender, RoutedEventArgs e)
        {
            if (Device.IsConnected())
            {
                int ret;
                string serNum = Device.DevInfo.SerialNumber.ToString();
                ret = Device.Restore_Factory_Reference(serNum);
                if (ret < 0)
                {
                    switch (ret)
                    {
                        case -1:
                            ShowError("Factory reference data restore FAILED!\n\nOut of memory.");
                            break;
                        case -2:
                            ShowError("Factory reference data restore FAILED!\n\nBackup directory not found");
                            break;
                        case -3:
                            ShowError("Factory reference data restore FAILED!\n\nRead file error");
                            break;
                        case -4:
                            ShowError("Factory reference data restore FAILED!\n\nReference data currupted");
                            break;
                        case -5:
                            ShowError("Factory reference data restore FAILED!\n\nDevice communcation error");
                            break;
                        case -6:
                            ShowError("Factory reference data restore FAILED!\n\nData was NOT the original factory reference data");
                            break;
                    }
                }
                else
                {
                    ShowInfo("Factory reference data has been restored successfully!\n\nPlease start a new scan to check the result.");
                    ClearScanPlotsEvent();
                }
            }
            else
                ShowError("No device connected for restoring factory reference!");
        }

        #endregion

        #region ActKey
        private void MenuItem_ActKeyMGMT_Click(object sender, RoutedEventArgs e)
        {
            //ActivationKeyWindow window = new ActivationKeyWindow { Owner = this };
            //window.ShowDialog(); // Execution only continues here after the window is closed.

            //if (window.IsActivated)
            //{
            //    StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected!";
            //    SendScanGUIEvent = (int)GUI_State.KEY_ACTIVATE;
            //    SendUtilityGUIEvent = (int)GUI_State.KEY_ACTIVATE;
            //}
            //else
            //{
            //    StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected but advanced functions locked!";
            //    SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
            //    SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
            //}
        }

        private void MenuItem_ClearActKey_Click(object sender, RoutedEventArgs e)
        {
            MessageBoxResult input = ShowQuestion("Are you sure to CLEAR Activation Key?", MessageBoxButton.OKCancel);

            if (input == MessageBoxResult.OK)
            {
                Byte[] ByteKey = new Byte[12];
                Device.SetActivationKey(ByteKey);

                //ActivationKeyWindow window = new ActivationKeyWindow { Owner = this };
                //if (window.IsActivated)
                //{
                //    StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected!";
                //    SendScanGUIEvent = (int)GUI_State.KEY_ACTIVATE;
                //    SendUtilityGUIEvent = (int)GUI_State.KEY_ACTIVATE;
                //}
                //else
                //{
                //    StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected but advanced functions locked!";
                //    SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                //    SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                //}
            }
        }

        private void MenuItem_License_Click(object sender, RoutedEventArgs e)
        {
            LicenseWindow window = new LicenseWindow { Owner = this };
            window.ShowDialog(); // Execution only continues here after the window is closed.
        }

        private void MenuItem_AboutUs_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                Process.Start("http://www.inno-spectra.com/");
            }
            catch { }
        }
        #endregion

        #region Device Status

        private void StatusIcon(Image imgElement, Int32 status)
        {
            BitmapImage statusIcon = new BitmapImage();
            statusIcon.BeginInit();
            if (status == 1)
                statusIcon.UriSource = new Uri("Images\\Led_G.png", UriKind.Relative);
            else if (status == 0)
                statusIcon.UriSource = new Uri("Images\\Led_Gray.png", UriKind.Relative);
            else if (status == -1)
                statusIcon.UriSource = new Uri("Images\\Led_R.png", UriKind.Relative);
            else if (status == -2)
                statusIcon.UriSource = new Uri("Images\\Led_B.png", UriKind.Relative);
            statusIcon.EndInit();
            imgElement.Source = statusIcon;
        }

        private void RefreshErrorStatus()
        {
            Console.WriteLine("RefreshErrorStatus");
            String ErrMsg = String.Empty;

            if (SDK.IsConnectionChecking == false)
            {
                if (Device.ReadErrorStatusAndCode() != 0)
                    return;
            }

            if ((Device.ErrStatus & 0x00000001) == 0x00000001)  // Scan Error
            {
                if (Device.ErrCode[0] == 0x00000001)
                    ErrMsg += "Scan Error: DLPC150 boot error detected.    ";
                else if (Device.ErrCode[0] == 0x00000002)
                    ErrMsg += "Scan Error: DLPC150 init error detected.    ";
                else if (Device.ErrCode[0] == 0x00000004)
                    ErrMsg += "Scan Error: DLPC150 lamp driver error detected.    ";
                else if (Device.ErrCode[0] == 0x00000008)
                    ErrMsg += "Scan Error: DLPC150 crop image failed.    ";
                else if (Device.ErrCode[0] == 0x00000010)
                    ErrMsg += "Scan Error: ADC data error.    ";
                else if (Device.ErrCode[0] == 0x00000020)
                    ErrMsg += "Scan Error: Scan config Invalid.    ";
                else if (Device.ErrCode[0] == 0x00000040)
                    ErrMsg += "Scan Error: Scan pattern streaming error.    ";
                else if (Device.ErrCode[0] == 0x00000080)
                    ErrMsg += "Scan Error: DLPC150 read error.    ";
            }

            if ((Device.ErrStatus & 0x00000002) == 0x00000002)  // ADC Error
            {
                if (Device.ErrCode[1] == 0x00000001)
                    ErrMsg += "ADC Error: ADC timeout error.    ";
                else if (Device.ErrCode[1] == 0x00000002)
                    ErrMsg += "ADC Error: ADC PowerDown error.    ";
                else if (Device.ErrCode[1] == 0x00000003)
                    ErrMsg += "ADC Error: ADC PowerUp error.    ";
                else if (Device.ErrCode[1] == 0x00000004)
                    ErrMsg += "ADC Error: ADC StandBy error.    ";
                else if (Device.ErrCode[1] == 0x00000005)
                    ErrMsg += "ADC Error: ADC WAKEUP error.    ";
                else if (Device.ErrCode[1] == 0x00000006)
                    ErrMsg += "ADC Error: ADC read register error.    ";
                else if (Device.ErrCode[1] == 0x00000007)
                    ErrMsg += "ADC Error: ADC write register error.    ";
                else if (Device.ErrCode[1] == 0x00000008)
                    ErrMsg += "ADC Error: ADC configure error.    ";
                else if (Device.ErrCode[1] == 0x00000009)
                    ErrMsg += "ADC Error: ADC set buffer error.    ";
                else if (Device.ErrCode[1] == 0x0000000A)
                    ErrMsg += "ADC Error: ADC command error.    ";
            }

            if ((Device.ErrStatus & 0x00000004) == 0x00000004)  // SD Card Error
            {
                ErrMsg += "SD Card Error.    ";
            }

            if ((Device.ErrStatus & 0x00000008) == 0x00000008)  // EEPROM Error
            {
                ErrMsg += "EEPROM Error.    ";
            }

            if ((Device.ErrStatus & 0x00000010) == 0x00000010)  // BLE Error
            {
                ErrMsg += "Bluetooth Error.    ";
            }

            if ((Device.ErrStatus & 0x00000020) == 0x00000020)  // Spectrum Library Error
            {
                ErrMsg += "Spectrum Library Error.    ";
            }

            if ((Device.ErrStatus & 0x00000040) == 0x00000040)  // Hardware Error
            {
                if (Device.ErrCode[6] == 0x00000001)
                    ErrMsg += "HW Error: DLPC150 Error.    ";
            }

            if ((Device.ErrStatus & 0x00000080) == 0x00000080)  // TMP Sensor Error
            {
                if (Device.ErrCode[7] == 0x00000001)
                    ErrMsg += "TMP Error: Invalid manufacturing id.    ";
                else if (Device.ErrCode[7] == 0x00000002)
                    ErrMsg += "TMP Error: Invalid device id.    ";
                else if (Device.ErrCode[7] == 0x00000003)
                    ErrMsg += "TMP Error: Reset error.    ";
                else if (Device.ErrCode[7] == 0x00000004)
                    ErrMsg += "TMP Error: Read register error.    ";
                else if (Device.ErrCode[7] == 0x00000005)
                    ErrMsg += "TMP Error: Write register error.    ";
                else if (Device.ErrCode[7] == 0x00000006)
                    ErrMsg += "TMP Error: Timeout error.    ";
                else if (Device.ErrCode[7] == 0x00000007)
                    ErrMsg += "TMP Error: I2C error.    ";
            }

            if ((Device.ErrStatus & 0x00000100) == 0x00000100)  // HDC1000 Sensor Error
            {
                if (Device.ErrCode[8] == 0x00000001)
                    ErrMsg += "HDC1000 Error: Invalid manufacturing id.    ";
                else if (Device.ErrCode[8] == 0x00000002)
                    ErrMsg += "HDC1000 Error: Invalid device id.    ";
                else if (Device.ErrCode[8] == 0x00000003)
                    ErrMsg += "HDC1000 Error: Reset error.    ";
                else if (Device.ErrCode[8] == 0x00000004)
                    ErrMsg += "HDC1000 Error: Read register error.    ";
                else if (Device.ErrCode[8] == 0x00000005)
                    ErrMsg += "HDC1000 Error: Write register error.    ";
                else if (Device.ErrCode[8] == 0x00000006)
                    ErrMsg += "HDC1000 Error: Timeout error.    ";
                else if (Device.ErrCode[8] == 0x00000007)
                    ErrMsg += "HDC1000 Error: I2C error.    ";
            }

            if ((Device.ErrStatus & 0x00000200) == 0x00000200)  // Battery Error
            {
                if (Device.ErrCode[9] == 0x00000001)
                    ErrMsg += "Battery Error: Battery low.    ";
            }

            if ((Device.ErrStatus & 0x00000400) == 0x00000400)  // Insufficient Memory Error
            {
                ErrMsg += "Not enough memory.    ";
            }

            if ((Device.ErrStatus & 0x00000800) == 0x00000800)  // UART Error
            {
                ErrMsg += "UART error.    ";
            }

            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                StatusBarItem_ErrorStatus.Content = ErrMsg;
            }));
        }


        #endregion

        #region Progress Window

        private void Connecting_Device(String ModelnSN)
        {
            Console.WriteLine("Connecting_Device");
            try { ProgressWindowCompleted(); } catch { }
            ProgressWindowStart("A estalebecer a ligação com o Hemospec...\n\nPor favor aguarde.", false);
        }

        private void BeginScan()// comentei para tirar a janela de progress durante as leituras
        {
            Console.WriteLine("BeginScan");
            //    try
            //    {
            //        ProgressWindowCompleted();
            //    }
            //    catch { }
            //    if (GlobalData.RepeatedScanCountDown - 1 < 1)
            //        ProgressWindowStart("Efectuando a leitura...\n\nPor favor aguarde.", false); // ProgressWindowStart("Scanning...\n\nPlease wait!", false);
            //    else
            //    {
            //        //   string msg = string.Format("Efectuando a leitura...\n\n{0} leituras em espera, por favor aguarde", GlobalData.TargetScanNumber - GlobalData.ScannedCounts - 1);
            //        // ProgressWindowStart(msg, true);
            //        //  string msg = string.Format(" Scanning...\n\n{0} scans remaining, please wait!", GlobalData.TargetScanNumber - GlobalData.ScannedCounts - 1);
            //        //ProgressWindowStart(msg, true);
            //    }
        }

        private void ScanCompleted()
        {
            Console.WriteLine("ScanCompleted");
            //    ProgressWindowCompleted();
            //    if (pbw.IsCancelled)
            //        GlobalData.UserCancelRepeatedScan = true;
        }

        #endregion

        #region Message Box

        public static void ShowInfo(String Text)
        {
            String Title = "Information";
            MessageBoxImage Image = MessageBoxImage.Information;
            MessageBoxButton Button = MessageBoxButton.OK;

            MessageBox.Show(Text, Title, Button, Image);
        }

        public static void ShowError(String Text)
        {
            String Title = "Erro";
            MessageBoxImage Image = MessageBoxImage.Error;
            MessageBoxButton Button = MessageBoxButton.OK;

            MessageBox.Show(Text, Title, Button, Image);
        }


        public static void ShowWarning(String Text)
        {
            String Title = "Aviso";
            MessageBoxImage Image = MessageBoxImage.Warning;
            MessageBoxButton Button = MessageBoxButton.OK;

            MessageBox.Show(Text, Title, Button, Image);
        }

        public static MessageBoxResult ShowQuestion(String Text, MessageBoxButton Button)
        {
            String Title = "Question?";
            MessageBoxImage Image = MessageBoxImage.Question;
            MessageBoxResult Default;

            if (Button == MessageBoxButton.OKCancel || Button == MessageBoxButton.YesNoCancel)
            {
                Default = MessageBoxResult.Cancel;
            }
            else if (Button == MessageBoxButton.YesNo)
            {
                Default = MessageBoxResult.No;
            }
            else
            {
                Default = MessageBoxResult.None;
            }

            return MessageBox.Show(Text, Title, Button, Image, Default);
        }

        #endregion

        public static Boolean IsOldTivaFW()
        {
            int lastVer, curVer;
            Byte[] latetestVerCode = { Convert.ToByte(2), Convert.ToByte(1), Convert.ToByte(0), Convert.ToByte(59) };

            if (Device.IsConnected())
            {
                lastVer = BitConverter.ToInt32(latetestVerCode, 0);
                curVer = BitConverter.ToInt32(Device.DevInfo.TivaRev, 0);

                if (curVer < lastVer)
                    return true;
                else
                    return false;
            }
            else
                return true;
        }

        //New functions 

        private void PBBw_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            progressBar1.Value = e.ProgressPercentage;
        }

        private void PBBw_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            _timerPB.Stop();
        }


        public void bw_DoWork(object sender, DoWorkEventArgs e)
        {
            CallReadingValues(e.Argument as ReadingValues);
        }

        private void Bw_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            progressBar1.Value = e.ProgressPercentage;
        }

        private void Bw_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
        }


       
        private void startScan()
        {
            // checkAndUpdateDeviceConfig(Device.DevInfo.SerialNumber, new List<String>(), new List<String>());
           // if (true) return;
            progressBar1.Value = 10;
            // Store selectedAnalysisBundle persistently as lastAnalysisBundle
            UserSettingsUtil.WriteLastAnalysisBundle(selectedAnalysisBundle);

            ReadingValues rValues = new ReadingValues()
            {
                DeviceConfigLog = DevConfigLog,
                ProcessNumber = processNbr,
                Sex = selectedGender,
                PatientIdCountry = selectedPatientIdCountry,
                PatientId = patientId,
                PatientContactNumber = patientContact,
                AnalysisBundle = selectedAnalysisBundle,
                BirthdayYear = birthYear == "" ? 0 : Int32.Parse(birthYear),
                Age = birthYear == "" ? 0 : DateTime.Now.Year - Int32.Parse(birthYear),
                ModelName = Device.DevInfo.ModelName,
                SerialNumber = Device.DevInfo.SerialNumber
            };
            bw.RunWorkerAsync(rValues);

        }
        
        public Sample PerformIndividualScan()
        {
            if(deviceIsBusy)
            {
                Thread.Sleep(1100);
            }
            deviceIsBusy = true;
            
            DateTime TimeScanStart = DateTime.Now;
            int res2 = Scan.PerformScan(Scan.SCAN_REF_TYPE.SCAN_REF_BUILT_IN);
            if(res2 != 0)
            {
                return null;
            }
            DateTime TimeScanEnd = DateTime.Now;
            Thread.Sleep(100);

            Sample sample = new Sample();

            sample.TotalTimeScan = (TimeScanEnd - TimeScanStart).TotalSeconds;
                #region Scan Data Sample
                //DEVICE IDENTIFICATION
                sample.UUID = Device.DevInfo.DeviceUUID;
                sample.HostDateTime = Scan.ScanDateTime;


                // SCAN SENSORS INFO
                sample.SystemTemp = Scan.SensorData[0];
                sample.DetectorTemp = Scan.SensorData[1];
                sample.Humidity = Scan.SensorData[2];
                sample.LampPD = Scan.SensorData[3];
                sample.ShiftVectorCoefficients = Device.Calib_Coeffs.ShiftVectorCoeffs;
                sample.PixelWavelengtCoefficients = Device.Calib_Coeffs.PixelToWavelengthCoeffs;

                // SCAN INFO
                sample.HeaderVersion = (int)Scan.ScanDataVersion;
                sample.Method = Scan.ScanConfigData.head.config_name;
                sample.ScanConfigName = Scan.ScanConfigData.head.config_name;
                sample.ScanConfigType = (Scan.ScanConfigData.head.num_sections == 1) ? Scan.ScanConfigData.section[0].section_scan_type : Scan.ScanConfigData.head.scan_type;
                sample.NumSection = Scan.ScanConfigData.head.num_sections;
                
            int sectionLastIdx = Scan.ScanConfigData.head.num_sections - 1;
                sample.StartWavelength = Scan.ScanConfigData.section == null ? 0 : Scan.ScanConfigData.section[sectionLastIdx].wavelength_start_nm; //Start wavelength (nm):
                sample.EndWavelength = Scan.ScanConfigData.section == null ? 0 : Scan.ScanConfigData.section[sectionLastIdx].wavelength_end_nm; // End wavelength (nm):
                sample.PatternPixelWidth = Scan.ReferenceScanConfigData.section == null ? 0 : Math.Round(Helper.CfgWidthPixelToNM(Scan.ReferenceScanConfigData.section[1].width_px), 2);  //Pattern Pixel Width (nm):
                sample.Exposure = Scan.ScanConfigData.section == null ? 0 : Helper.CfgExpIndexToTime(Scan.ScanConfigData.section[sectionLastIdx].exposure_time); //Exposure (ms):
                sample.DigitalResolution = Scan.ReferenceScanConfigData.section == null ? 0 : Scan.ScanConfigData.section[sectionLastIdx].num_patterns;
                sample.NumRep = Scan.ScanConfigData.head.num_repeats;
                sample.PGAgain = Scan.PGA;

                //SCAN DATA
                sample.Absorbance = new List<double>(Scan.Absorbance);
                sample.WaveLength = new List<double>(Scan.WaveLength);
                // Reflectance = Scan.Reflectance;
                sample.Intensity = Scan.Intensity;

                //reference device
                sample.RefSerialNumber = Scan.ReferenceScanConfigData.head.ScanConfig_serial_number;
                sample.RefDateTime = Scan.ReferenceScanDateTime;
                //reference scan
                //RefScanConfigName = Scan.ReferenceScanConfigData.head.config_name;

                sample.ReferencePGA = Scan.ReferencePGA;
                sample.ReferenceIntensity = Scan.ReferenceIntensity;
                sample.ReferenceScanDataVersion = (int)Scan.ReferenceScanDataVersion;
                sample.RefScanConfigType = (Scan.ScanConfigData.head.num_sections == 1) ? Scan.ReferenceScanConfigData.section[0].section_scan_type : Scan.ReferenceScanConfigData.head.scan_type;
            sample.ReferenceScanConfigData = new LabinLightApi.Models.SlewScanConfig();
            sample.ReferenceScanConfigData.Head = new LabinLightApi.Models.SlewScanConfigHead();
                sample.ReferenceScanConfigData.Head.scan_type = Scan.ReferenceScanConfigData.head.scan_type;
                        sample.ReferenceScanConfigData.Head.scanConfigIndex = Scan.ReferenceScanConfigData.head.scanConfigIndex;
                        sample.ReferenceScanConfigData.Head.ScanConfig_serial_number = Scan.ReferenceScanConfigData.head.ScanConfig_serial_number;
                        sample.ReferenceScanConfigData.Head.config_name = Scan.ReferenceScanConfigData.head.config_name;
                        sample.ReferenceScanConfigData.Head.num_repeats = Scan.ReferenceScanConfigData.head.num_repeats;
                        sample.ReferenceScanConfigData.Head.num_sections = Scan.ReferenceScanConfigData.head.num_sections;
            if(Scan.ReferenceScanConfigData.section != null)
            {
                sample.ReferenceScanConfigData.Section = Scan.ReferenceScanConfigData.section.Select(p => new LabinLightApi.Models.SlewScanSection()
            {
                section_scan_type = p.section_scan_type,
                width_px = p.width_px,
                wavelength_start_nm = p.wavelength_start_nm,
                wavelength_end_nm = p.wavelength_end_nm,
                num_patterns = p.num_patterns,
                exposure_time = p.exposure_time,
            }).ToArray();
            }



            //reference sensors info
            sample.RefTemp = Scan.ReferenceSensorData[0];
                sample.RefDetectorTemp = Scan.ReferenceSensorData[1];
                sample.RefHumidity = Scan.ReferenceSensorData[2];
                sample.RefLampPD = Scan.ReferenceSensorData[3];

            #endregion

            deviceIsBusy = false;
            return sample;
        }


        private Timer checkDeviceTempTimer;
        public void initCheckDeviceTemp()
        {
            var autoEvent = new AutoResetEvent(false);
            checkDeviceTempTimer = new Timer(this.checkDeviceTemp,
                                   autoEvent, 500, 2000);
        }

        private void checkDeviceTemp(Object stateInfo)
        {
            checkDeviceTemp();
        }
        private void checkDeviceTemp()
        {
            int icon;
            if(deviceIsConnected && !deviceIsBusy)
            {

                try
                {
                    deviceIsBusy = true; 
                    Device.ReadSensorsData();
                    deviceTemp = Device.DevSensors.HDCTemp;
                    deviceIsCold = deviceTemp < 25;
                    icon = deviceIsCold ? -2 : 1;
                }
                catch
                {
                    icon = -2;
                }
                finally { deviceIsBusy = false; }
                calculateButtonEnabled();
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Render, new Action(() =>
                {
                    var content = deviceIsCold ? ". Temperatura inferior a 25ºC" : "";
                    StatusBarItem_DeviceStatus.Content = deviceStatusInfo +  content +  " (" + deviceTemp + "ºC)";
                    if (deviceIsUnavailable)
                    {
                        StatusBarItem_DeviceStatus.Content = StatusBarItem_DeviceStatus.Content + " - Necessário calibrar";
                        icon = -1;
                    }
                    StatusIcon(Image_DeviceStatusIcon, icon);
                }));

            }
        }

        private Timer checkServerStatusTimer;
        public void initCheckServerStatus()
        {
            var autoEvent = new AutoResetEvent(false);
            checkServerStatusTimer = new Timer(this.checkServerStatus,
                                   autoEvent, 500, 60000); 
        }

        private void checkServerStatus(Object stateInfo)
        {
            checkServerStatus();
        }
        private bool checkServerStatus()
        {
            int icon;
            bool showErrMsg = true;
            bool serverIsAvailable = false;
            try {
                serverIsAvailable = LabinLightReadingsServiceClient.instance.CheckServerStatus();
                icon = serverIsAvailable ? 1 : 0;
                showErrMsg = !serverIsAvailable;
                if (serverIsAvailable)
                {
                    // Load analysis types from API when server is available
                    Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(async () =>
                    {
                        await LoadAnalysisTypesAsync();
                    }));
                }
            }
            catch
            {
                icon = -1;
            }

            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                var content = showErrMsg ? "Não é possíver estabelecer ligação com o servidor." : "";
                StatusBarItem_ServerStatus.Content = content;
                StatusIcon(Image_ServerStatusIcon, icon);
            }));
            return serverIsAvailable;
        }

        private int nReads = 1;
        public void CallReadingValues(ReadingValues varReading)
        {
            int perc = (int) Math.Floor((decimal) 80 / nReads);
            int progress = 10;
            for (int i = 0; i < nReads;i++)
            {
                Sample individualScan = PerformIndividualScan();
                if (individualScan == null)
                {
                    ShowError("Occorreu um erro em obter leitura do Hemospec");
                    BorderAnalisar.Visibility = Visibility.Collapsed;
                    deviceIsBusy = false;
                    return;
                }
                varReading.Samples.Add(individualScan);
                progress += perc;
                bw.ReportProgress(progress);
            }

            serverCommIsRunning = true;
            calculateButtonEnabled();
            try
            {

                var results = LabinLightReadingsServiceClient.instance.CalcAsync(varReading).Result;
                ResultsCsvExporter.instance.export(processNbr,results);
                interpretResults(results);
                bw.ReportProgress(99);
            }
            catch(KnownServiceErrorException e)
            {
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
                {
                    BorderAnalisar.Visibility = Visibility.Collapsed;
                    ShowError(e.Message);
                    calculateButtonEnabled();
                }));
            }
            catch (Exception e)
            {
                var msg = "Ocorreu um erro na comunicação com o servidor.";
                if (e is AggregateException && ((AggregateException) e).InnerException != null)
                {
                    if( ((AggregateException)e).InnerException is KnownServiceErrorException)
                    {
                        msg = ((AggregateException)e).InnerException.Message;
                    }
                    Console.WriteLine("!!! Server error: " + ((AggregateException)e).InnerException.Message);
                }
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
                {
                    BorderAnalisar.Visibility = Visibility.Collapsed;
                    ShowError(msg);
                    Console.WriteLine("!!! Server error: " + e.Message);
                    calculateButtonEnabled();
                }));
            }
            finally
            {
                serverCommIsRunning = false; 
            }
        }



        public async void interpretResults(List<BloodResult> results) 
        {

            await Dispatcher.BeginInvoke(DispatcherPriority.Send, new Action(() =>
            {

                try
                {

                    BorderAnalisar.Visibility = Visibility.Collapsed;


                    resResults.Children.Clear();
                    resResults.RowDefinitions.Clear();
                    addResultsColumnLabels();

                    int row = 1;
                    foreach (var result in results)
                    {
                        if (String.IsNullOrEmpty(result.ValueString)) {
                            continue;
                        }
                        addResultData(row, result);
                        row++;
                    }
                    if (row == 1) { // No results? All outliers
                        ShowWarning("A leitura foi considerada inválida, certifique-se que o cartucho se encontra correctamente inserido no Hemospec.");
                        return;
                    }


                    setAllFormFieldsEnabled(null, false);
                    showResultsPopup();
                }
                catch (Exception e)
                {
                        
                    ShowError("Ocorreu um erro na interpretação de resultados.");
                    Console.WriteLine("!!! Parsing error: " + e.Message);

                }
            }));
            

        }

        private void addResultData(int row, BloodResult result)
        {
            int col = 0;
            resResults.RowDefinitions.Add(new RowDefinition());
            Label resultLabel = new Label
            {
                FontWeight = FontWeights.Bold,
                FontSize = 18,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Left,
                Margin = new Thickness(0, 0, 20, 0),
                Content = result.Label
            };
            Grid.SetRow(resultLabel, row);
            Grid.SetColumn(resultLabel, col++);

            TextBlock resultData = new TextBlock()
            {
                FontSize = 16,
                Text = result.ValueString,
                TextAlignment = TextAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 10, 0),
                TextWrapping = TextWrapping.Wrap,
            };
            Grid.SetRow(resultData, row);
            Grid.SetColumn(resultData, col++);

            TextBlock resultUnits = new TextBlock
            {
                FontSize = 16,
                Text = result.Unit,
                FontWeight = FontWeights.Bold,
                TextAlignment = TextAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 10, 0),
            };
            Grid.SetRow(resultUnits, row);
            Grid.SetColumn(resultUnits, col++);

            Button resultDataCopy = new Button
            {
                Width = 24,
                Height = 24,
                Margin = new Thickness(10, 0, 0, 0),
                Content = new Image
                {
                    Source = new BitmapImage(new Uri(@"pack://application:,,,/Images/paste.png")),
                    VerticalAlignment = VerticalAlignment.Center
                }
            };
            resultDataCopy.Click += (x, y) => Clipboard.SetText(result.ValueString);
            Grid.SetRow(resultDataCopy, row);
            Grid.SetColumn(resultDataCopy, col++);


            TextBlock resultReferenceValues = new TextBlock
            {
                FontSize = 16,
                Text = result.ReferenceValues,
                FontWeight = FontWeights.Bold,
                TextAlignment = TextAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 10, 0),
            };
            Grid.SetRow(resultReferenceValues, row);
            Grid.SetColumn(resultReferenceValues, col++);


            resResults.Children.Add(resultLabel);
            resResults.Children.Add(resultData);
            resResults.Children.Add(resultUnits);
            resResults.Children.Add(resultDataCopy);
            resResults.Children.Add(resultReferenceValues);
        }

        private void addResultsColumnLabels()
        {
            resResults.RowDefinitions.Add(new RowDefinition());
            TextBlock columnLabel;
            columnLabel = new TextBlock
            {
                FontWeight = FontWeights.Bold,
                FontSize = 18,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Left,
                Margin = new Thickness(0, 0, 20, 30),
                Text = "Hematologia",
                Foreground = Brushes.Gray
            };
            Grid.SetRow(columnLabel, 0);
            Grid.SetColumn(columnLabel, 0);
            resResults.Children.Add(columnLabel);
            columnLabel = new TextBlock
            {
                FontWeight = FontWeights.Bold,
                FontSize = 18,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 20, 30),
                Text = "Valor",
                Foreground = Brushes.Gray
            };
            Grid.SetRow(columnLabel, 0);
            Grid.SetColumn(columnLabel, 1);
            resResults.Children.Add(columnLabel);
            columnLabel = new TextBlock
            {
                FontWeight = FontWeights.Bold,
                FontSize = 18,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 20, 30),
                Text = "Unidade",
                Foreground = Brushes.Gray
            };
            Grid.SetRow(columnLabel, 0);
            Grid.SetColumn(columnLabel, 2);
            resResults.Children.Add(columnLabel);
            columnLabel = new TextBlock
            {
                FontWeight = FontWeights.Bold,
                FontSize = 18,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 20, 30),
                Text = "Valor de Referência",
                Foreground = Brushes.Gray
            };
            Grid.SetRow(columnLabel, 0);
            Grid.SetColumn(columnLabel, 4);
            resResults.Children.Add(columnLabel);
        }

        public void TextBox_GotFocus(object sender, RoutedEventArgs e)
        {
            TextBox tb = (TextBox)sender;
            tb.Text = string.Empty;
            tb.FontStyle = default;
            tb.Foreground = Brushes.Black;
            tb.Background = Brushes.AliceBlue;
            tb.GotFocus -= TextBox_GotFocus;

        }

        public void BStart_Click(object sender, RoutedEventArgs e)
        {
            start_click(1);
        }

        private void start_click(int retryN)
        {
            this.BStart.IsEnabled = false;
            if (calculateButtonEnabled() && checkServerStatus())
            {
                BorderAnalisar.Visibility = Visibility.Visible;
                startScan();
            }
            else if (retryN > 15)
            {
                MessageBox.Show("Falha de ligação com o Hemospec ou servidor.");
                calculateButtonEnabled();
            }
            else {
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
                {
                    Thread.Sleep(200);
                    start_click(++retryN);
                }));
            }
        }

       

        private void BContinuar_Click(object sender, RoutedEventArgs e)
        {
            initForm();
            setAllFormFieldsEnabled(null, true);
            hideResultsPopup();
        }

        private void showResultsPopup()
        {
            DoubleAnimation db2 = new DoubleAnimation();
            db2.From = 0;
            db2.To = 1;
            db2.Duration = TimeSpan.FromSeconds(0.1);
            BorderResults.BeginAnimation(Border.OpacityProperty, db2);

            DoubleAnimation db = new DoubleAnimation();
            db.From = -BorderResults.ActualHeight;
            db.To = 0;
            db.Duration = TimeSpan.FromSeconds(0.5);
            borderResultsTransform.BeginAnimation(TranslateTransform.YProperty, db);

            Continuar.Focus();
        }
        private void hideResultsPopup()
        {
            DoubleAnimation db = new DoubleAnimation();
            db.From = 0;
            db.To = -BorderResults.ActualHeight;
            db.Duration = TimeSpan.FromSeconds(0.5);
            borderResultsTransform.BeginAnimation(TranslateTransform.YProperty, db);

            DoubleAnimation db2 = new DoubleAnimation();
            db2.From = 1;
            db2.To = 0;
            db2.Duration = TimeSpan.FromSeconds(1.5);
            BorderResults.BeginAnimation(Border.OpacityProperty, db2);


            TbId.Focus();
        }
       
        private void open_Help2Popup(object sender, RoutedEventArgs e)
        {
            Help2Popup.IsOpen = true;
        }
        private void open_Help3Popup(object sender, RoutedEventArgs e)
        {
            Help3Popup.IsOpen = true;
        }
        private void open_Help4Popup(object sender, RoutedEventArgs e)
        {
            Help4Popup.IsOpen = true;
        }
        private void initForm()
        {
            processNbr = string.Empty;
            birthYear = string.Empty;
            selectedGender = string.Empty;
            selectedPatientIdCountry = "PT";
            patientContact = string.Empty;
            patientId = string.Empty;
            forceSync(null);
            TbId.Focus();

            calculateButtonEnabled();
        }

        private async Task LoadAnalysisTypesAsync()
        {
            if(AnalysisTypesList.Count != 0)
            {
                return;
            }
            try
            {
                var types = await LabinLightReadingsServiceClient.instance.FetchAnalysisTypesAsync();
                var defaultType = null as AnalysisType;
                foreach (var t in types){
                    if (t.Default)
                        defaultType = t;
                    AnalysisTypesList.Add(t);
                }

                if (defaultType == null)
                {
                    // Try to load lastAnalysisBundle from persistent settings
                    var lastAnalysisBundle = UserSettingsUtil.ReadLastAnalysisBundle();
                    var found = !string.IsNullOrEmpty(lastAnalysisBundle) ? types.FirstOrDefault(x => x.Value == lastAnalysisBundle) : null;
                    if (found != null)
                    {
                        selectedAnalysisBundle = found.Value;
                        DropdownAnalsysisType.SelectedValue = found.Value;
                    }
                    else if (types.Count > 0)
                    {
                        selectedAnalysisBundle = types[0].Value;
                        DropdownAnalsysisType.SelectedValue = types[0].Value;
                    }
                }
                else
                {
                    selectedAnalysisBundle = defaultType.Value;
                    DropdownAnalsysisType.SelectedValue = defaultType.Value;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Erro ao obter tipos de análise", "Erro", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private bool isFormValid()
        {
           
            bool result = false;
            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                if (TbId != null && TbId.IsEnabled)
                {
                    result = IsValid(null);
                }
            })).Wait();
            return result;
        }

        public void setAllFormFieldsEnabled(DependencyObject parent, bool enabled)
        {   
            if(parent == null)
            { 
                parent = GridPersonalData;
            }
           
            for (int i = 0; i != VisualTreeHelper.GetChildrenCount(parent); ++i)
            {
                DependencyObject child = VisualTreeHelper.GetChild(parent, i);
                if (child is ComboBox)
                {
                    ((ComboBox)child).IsEnabled = enabled;
                }
                else if (child is TextBox)
                {
                    ((TextBox)child).IsEnabled = enabled;
                }
                else if(child is RadioButton)
                {
                    ((RadioButton)child).IsEnabled = enabled;
                }
                setAllFormFieldsEnabled(child, enabled);
            }
        }
        public void forceSync(DependencyObject parent)
        {

            if (parent == null)
            {
                parent = GridPersonalData;
            }
            LocalValueEnumerator localValues = parent.GetLocalValueEnumerator();
            while (localValues.MoveNext())
            {
                LocalValueEntry entry = localValues.Current;
                if (BindingOperations.IsDataBound(parent, entry.Property))
                {
                   BindingOperations.GetBindingExpression(parent, entry.Property).UpdateTarget();
                }
            }
            for (int i = 0; i != VisualTreeHelper.GetChildrenCount(parent); ++i)
            {
                DependencyObject child = VisualTreeHelper.GetChild(parent, i);
                forceSync(child);
            }
        }
        public bool IsValid(DependencyObject parent)
        {
            if (parent == null)
            {
                parent = GridPersonalData;
            }
            // Validate all the bindings on the parent
            bool valid = true;
            LocalValueEnumerator localValues = parent.GetLocalValueEnumerator();
            while (localValues.MoveNext())
            {
                LocalValueEntry entry = localValues.Current;
                if (BindingOperations.IsDataBound(parent, entry.Property))
                {
                    Binding binding = BindingOperations.GetBinding(parent, entry.Property);
                    foreach (ValidationRule rule in binding.ValidationRules)
                    {
                        ValidationResult result = rule.Validate(parent.GetValue(entry.Property), null);
                        if (!result.IsValid)
                        {
                            BindingExpression expression = BindingOperations.GetBindingExpression(parent, entry.Property);
                            System.Windows.Controls.Validation.MarkInvalid(expression, new ValidationError(rule, expression, result.ErrorContent, null));
                            valid = false;
                        }
                    }
                }
            }

            // Validate all the bindings on the children
            for (int i = 0; i != VisualTreeHelper.GetChildrenCount(parent); ++i)
            {
                DependencyObject child = VisualTreeHelper.GetChild(parent, i);
                if (!IsValid(child)) {
                    valid = false; 
                }
            }

            return valid;
        }


        private bool calculateButtonEnabled()
        {
            bool btnEnabled = !deviceIsUnavailable && isFormValid() && deviceIsConnected && !deviceIsBusy && !deviceIsCold && !serverCommIsRunning && isFormValid();
            
            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() =>
            {
                this.BStart.IsEnabled = btnEnabled;
            })).Wait();
            return btnEnabled;
        }


        protected void formInputChanged(object sender, EventArgs e)
        {
            calculateButtonEnabled();
        }

        

        private void TbId_keydown(object sender, System.Windows.Input.KeyEventArgs e)
        {
            if (e.Key == Key.Enter )
            {
                BStart.RaiseEvent(new RoutedEventArgs(Button.ClickEvent));

            }
        }

        private System.Text.RegularExpressions.Regex yearRegex = new System.Text.RegularExpressions.Regex("[0-9]+");
        private void birthYearValidate(System.Object sender, System.Windows.Input.TextCompositionEventArgs e)
        {
            e.Handled = !yearRegex.IsMatch(e.Text);   
        }

        
        private System.Text.RegularExpressions.Regex phoneNbrRegex = new System.Text.RegularExpressions.Regex("[0-9\\(\\) \\+]+");
        private void patientContactValidate(System.Object sender, System.Windows.Input.TextCompositionEventArgs e)
        {
            e.Handled = !phoneNbrRegex.IsMatch(e.Text);

        }
        
    }

};