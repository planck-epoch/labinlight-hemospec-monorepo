using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using DLP_NIR_Win_SDK_CS;

namespace LabinLightScan
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        //public MainWindow()
        //{
        //    InitializeComponent();
        //}

        private static int UserSelectedDeviceIndex;

        public static event Action<int> OnScanGUIControl = null;
        private static int SendScanGUIEvent { set { OnScanGUIControl(value); } }

        public static event Action<int> OnUtilityGUIControl = null;
        private static int SendUtilityGUIEvent { set { OnUtilityGUIControl(value); } }

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

        public MainWindow()
        {
            InitializeComponent();
            Loaded += new RoutedEventHandler(MainWindow_Loaded);

            // Enable the CPP DLL debug output for development
            //DBG.Enable_CPP_Console();
        }

        private void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            Device.Init();
            SDK.OnDeviceConnectionLost += new Action<bool>(Device_Disconncted_Handler);
            SDK.OnDeviceConnected += new Action<string>(Device_Connected_Handler);
            SDK.OnDeviceFound += new Action(Device_Found_Handler);
            SDK.OnDeviceError += new Action<string>(Device_Error_Handler);
            SDK.OnErrorStatusFound += new Action(RefreshErrorStatus);
            SDK.OnBeginConnectingDevice += new Action(Connecting_Device);
            SDK.OnBeginScan += new Action(BeginScan);
            SDK.OnScanCompleted += new Action(ScanCompleted);

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

        private void Device_Disconncted_Handler(bool error)
        {
            Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() => {
                MainWindow_GUI_Handler((int)GUI_State.DEVICE_OFF);
                //MenuItem_SelectDevice.Items.Clear();
                //StatusIcon(0);
                //StatusBarItem_DeviceStatus.Content = "Device disconnect!";
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
                    ShowWarning("Device was disconnected !");
                }
            }
            else
            {
                DBG.WriteLine("Device disconnected successfully !");
            }
        }

        private void Device_Connected_Handler(String SerialNumber)
        {
            //ProgressWindowCompleted();

            if (SerialNumber == null)
            {
                DBG.WriteLine("Device connecting failed !");
            }
            else
            {
                DBG.WriteLine("Device <{0}> connected successfullly !", SerialNumber);

                String HWRev = (!String.IsNullOrEmpty(Device.DevInfo.HardwareRev)) ? Device.DevInfo.HardwareRev.Substring(0, 1) : String.Empty;
                if (Device.ChkBleExist() == 1)
                    Device.SetBluetooth(false);
                //if ((IsOldTivaFW() && HWRev == "D") || (!IsOldTivaFW() && HWRev != "A" && HWRev != String.Empty))
                //    CheckFactoryRefData();

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
                
                Dispatcher.BeginInvoke(System.Windows.Threading.DispatcherPriority.Send, new Action(() => {
                    MainWindow_GUI_Handler((int)GUI_State.DEVICE_ON);
                    //StatusIcon(1);
                    Device.ReadErrorStatusAndCode();

                    //if ((IsOldTivaFW() && HWRev == "D") || (!IsOldTivaFW() && HWRev != "A" && HWRev != String.Empty))
                    //{
                    //    ActivationKeyWindow window = new ActivationKeyWindow();
                    //    if (window.IsActivated)
                    //    {
                    //        //StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected!";
                    //        SendScanGUIEvent = (int)GUI_State.KEY_ACTIVATE;
                    //        SendUtilityGUIEvent = (int)GUI_State.KEY_ACTIVATE;
                    //    }
                    //    else
                    //    {
                    //        //StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected but advanced functions locked!";
                    //        SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    //        SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    //    }
                    //}
                    //else
                    //{
                    //    //StatusBarItem_DeviceStatus.Content = "Device " + Device.DevInfo.ModelName + " (" + Device.DevInfo.SerialNumber + ") connected!";
                    //    SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    //    SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    //}
                }));
            }
        }

        private void Device_Found_Handler()
        {
            SDK.AutoSearch = false;
            Dispatcher.Invoke((Action)delegate ()
            {
                Enumerate_Devices(null, null);
            });
        }

        private void Device_Error_Handler(string error)
        {
            ShowWarning(error);
        }

        private void RefreshErrorStatus()
        {
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
                //StatusBarItem_ErrorStatus.Content = ErrMsg;
            }));
        }

        private void Connecting_Device()
        {
            //try { ProgressWindowCompleted(); } catch { }
            //ProgressWindowStart("Connecting...\n\nPlease wait!", false);
        }

        private void BeginScan()
        {
            //try { ProgressWindowCompleted(); } catch { }
            //if (GlobalData.RepeatedScanCountDown - 1 < 1)
            //    ProgressWindowStart("Scanning...\n\nPlease wait!", false);
            //else
            //{
            //    string msg = string.Format("                    Scanning...\n\n{0} scans remaining, please wait!", GlobalData.TargetScanNumber - GlobalData.ScannedCounts - 1);
            //    ProgressWindowStart(msg, true);
            //}
        }

        private void ScanCompleted()
        {
            //ProgressWindowCompleted();
            //if (pbw.IsCancelled)
            //    GlobalData.UserCancelRepeatedScan = true;
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

                        //if ((IsOldTivaFW() && HWRev == "D") || (!IsOldTivaFW() && HWRev != "A" && HWRev != String.Empty))
                        //{
                        //    Separator_Advance.Visibility = Visibility.Visible;
                        //    MenuItem_Advance.Visibility = Visibility.Visible;
                        //    Separator_ActKeyMGMT.Visibility = Visibility.Visible;
                        //    MenuItem_ActKeyMGMT.Visibility = Visibility.Visible;
                        //    MenuItem_ClearActKey.Visibility = Visibility.Visible;
                        //}
                        //else
                        //{
                        //    Separator_Advance.Visibility = Visibility.Collapsed;
                        //    MenuItem_Advance.Visibility = Visibility.Collapsed;
                        //    Separator_ActKeyMGMT.Visibility = Visibility.Collapsed;
                        //    MenuItem_ActKeyMGMT.Visibility = Visibility.Collapsed;
                        //    MenuItem_ClearActKey.Visibility = Visibility.Collapsed;
                        //}

                        if (state == (int)GUI_State.DEVICE_ON)
                            isEnable = true;
                        else
                            isEnable = false;

                        //MenuItem_Info.IsEnabled = isEnable;
                        //MenuItem_ResetSys.IsEnabled = isEnable;
                        //MenuItem_UpdateRef.IsEnabled = isEnable;
                        //MenuItem_Advance.IsEnabled = isEnable;
                        //MenuItem_ActKeyMGMT.IsEnabled = isEnable;
                        //MenuItem_ClearActKey.IsEnabled = isEnable;
                        //Button_ClearAllErrors.IsEnabled = isEnable;
                        break;
                    }
                case (int)GUI_State.SCAN:
                case (int)GUI_State.SCAN_FINISHED:
                    {
                        if (state == (int)GUI_State.SCAN)
                            isEnable = false;
                        else
                            isEnable = true;

                        //MenuItem_Utility.IsEnabled = isEnable;
                        //MenuItem_Device.IsEnabled = isEnable;
                        //Button_ClearAllErrors.IsEnabled = isEnable;
                        break;
                    }

                case (int)GUI_State.FW_UPDATE:
                case (int)GUI_State.FW_UPDATE_FINISHED:
                    {
                        if (state == (int)GUI_State.FW_UPDATE)
                            isEnable = false;
                        else
                            isEnable = true;

                        //MenuItem_Scan.IsEnabled = isEnable;
                        //MenuItem_Device.IsEnabled = isEnable;
                        //Button_ClearAllErrors.IsEnabled = isEnable;
                        break;
                    }
                case (int)GUI_State.REFERENCE_DATA_UPDATE:
                case (int)GUI_State.REFERENCE_DATA_UPDATE_FINISHED:
                    {
                        if (state == (int)GUI_State.REFERENCE_DATA_UPDATE)
                            isEnable = false;
                        else
                            isEnable = true;

                        //MenuItem_Scan.IsEnabled = isEnable;
                        //MenuItem_Utility.IsEnabled = isEnable;
                        //MenuItem_Device.IsEnabled = isEnable;
                        //Button_ClearAllErrors.IsEnabled = isEnable;
                        break;
                    }
                default:
                    break;
            }
        }

        private void LoadSettings()
        {
            //// Config Directory
            //String path = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
            //ConfigDir = Path.Combine(path, "InnoSpectra\\Config Data");

            //if (Directory.Exists(ConfigDir) == false)
            //{
            //    Directory.CreateDirectory(ConfigDir);
            //    DBG.WriteLine("The directory {0} was created.", ConfigDir);
            //}
        }

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
                    //ClearScanPlotsEvent();
                    SendScanGUIEvent = (int)GUI_State.DEVICE_OFF_SCANTAB_SELECT;
                    SendScanGUIEvent = (int)GUI_State.DEVICE_OFF;
                    SendScanGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    SendUtilityGUIEvent = (int)GUI_State.DEVICE_OFF;
                    SendUtilityGUIEvent = (int)GUI_State.KEY_NOT_ACTIVATE;
                    MainWindow_GUI_Handler((int)GUI_State.DEVICE_OFF);
                    //MenuItem_SelectDevice.Items.Clear();
                    //StatusIcon(0);
                    //StatusBarItem_DeviceStatus.Content = "Device disconnect!";

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
            String Title = "Error!";
            MessageBoxImage Image = MessageBoxImage.Error;
            MessageBoxButton Button = MessageBoxButton.OK;

            MessageBox.Show(Text, Title, Button, Image);
        }

        public static void ShowWarning(String Text)
        {
            String Title = "Warning!";
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


    }
}
