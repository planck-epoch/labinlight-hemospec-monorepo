using System;
using System.Threading.Tasks;
using System.Windows;
using ISC_Win_CS_LIB;
using LabinLightApi.Models;
using LabinLightScan.ServiceClient;
using LabinLightScan.services;
using static System.Net.Mime.MediaTypeNames;

namespace LabinLightScan
{
    public partial class CalibrationWindow : Window
    {
        private string deviceSerialNumber;
        private bool lidConfirmed = false;
        private DateTime? lastCalibrationDate = null;
        public bool DeviceIsUnavailable { get; private set; } = false;
        public bool CalibrationPerformed { get; private set; } = false;
        private MainWindow mainWin;
        private System.Windows.Threading.DispatcherTimer tempRefreshTimer;

        public CalibrationWindow(string serialNumber, MainWindow mainWin)
        {
            InitializeComponent();
            deviceSerialNumber = serialNumber;
            this.mainWin = mainWin;
            UpdateCalibrationControls();
            if(mainWin.ServerConfiguration == null)
            {
                CalibrationPerformed = true;
                this.DialogResult = false;
                this.Close();
            }
            lastCalibrationDate = DateTimeOffset.FromUnixTimeSeconds(mainWin.ServerConfiguration.LastCalibrationAt).LocalDateTime;
            if (lastCalibrationDate.HasValue && (DateTime.Now - lastCalibrationDate.Value).TotalDays < 30)
            {
                SkipButton.Visibility = Visibility.Visible;
            }
            this.Closing += CalibrationWindow_Closing;

            
            tempRefreshTimer = new System.Windows.Threading.DispatcherTimer();
            tempRefreshTimer.Interval = TimeSpan.FromSeconds(3);
            tempRefreshTimer.Tick += TempRefreshTimer_Tick;
            tempRefreshTimer.Start();
        }

        private void LidCheckBox_Checked(object sender, RoutedEventArgs e)
        {
            lidConfirmed = LidCheckBox.IsChecked == true;
            UpdateCalibrationControls();
        }

        private void UpdateCalibrationControls()
        {
            
            TempText.Text = "Temperatura do HemoSpec: " + (mainWin.deviceTemp == 0 ? "--" : mainWin.deviceTemp+"") + "°C";
            TempWarning.Visibility = !mainWin.deviceIsCold ? Visibility.Collapsed : Visibility.Visible;
            TempText.Visibility = TempWarning.Visibility;
            StartCalibrationButton.IsEnabled = lidConfirmed && !mainWin.deviceIsCold && !DeviceIsUnavailable;
        }

        private async void StartCalibrationButton_Click(object sender, RoutedEventArgs e)
        {
            ErrorText.Visibility = Visibility.Collapsed;
            StartCalibrationButton.IsEnabled = false;
            try
            {
                Sample individualScan = mainWin.PerformIndividualScan();
                await LabinLightReadingsServiceClient.instance.CalibrateDeviceAsync(deviceSerialNumber, individualScan);
           
                CalibrationPerformed = true;
                this.Close();
            }
            catch (Exception ex)
            {
                DeviceIsUnavailable = true;
                UpdateCalibrationControls();

                String Title = "Erro";
                MessageBoxImage Image = MessageBoxImage.Error;
                MessageBoxButton Button = MessageBoxButton.OK;

                MessageBox.Show("Erro 03: Reinicie o Hemospec e confirme que a tampa está inserida corretamente.", Title, Button, Image);

                this.DialogResult = false;
                this.Close();
            }
        }

        private void SkipButton_Click(object sender, RoutedEventArgs e)
        {
            this.DialogResult = false;
            this.Close();
        }

        private void CalibrationWindow_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (!CalibrationPerformed && this.DialogResult != false)
            {
                e.Cancel = true;
            }
        }

        private void TempRefreshTimer_Tick(object sender, EventArgs e)
        {
            UpdateCalibrationControls();
            if (!mainWin.deviceIsCold && tempRefreshTimer != null)
            {
                tempRefreshTimer.Stop();
                tempRefreshTimer = null;
            }
        }
    }
}
