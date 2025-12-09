using ISC_UUID_DEFINITION;
using OxyPlot;
using OxyPlot.Axes;
using OxyPlot.Series;
using OxyPlot.Windows;
using System;
using System.Collections.Generic;
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
using Client = ISC_BLE_SDK.Scenario2_Client;

namespace ISC_BLE_SDK
{
    public sealed partial class Scenario6_ViewSpectrum : Page
    {
        enum PlotType
        {
            Intensity,
            ReferenceIntensity,
            Absorbance,
            Reflectance
        }

        #region UI Code
        public Scenario6_ViewSpectrum()
        {
            this.InitializeComponent();
        }
        private void Scenario6_ViewSpectrum_Loaded(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            CreatePlotModel(PlotType.Absorbance);
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
           
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {

        }
        private void RadioButtonPlotType_Checked(object sender, Windows.UI.Xaml.RoutedEventArgs e)
        {
            PlotType plotType;
            RadioButton radioButton = (RadioButton)sender;
            switch (radioButton.Content.ToString())
            {
                case "Reflectance":
                    plotType = PlotType.Reflectance;
                    break;
                case "Absorbance":
                    plotType = PlotType.Absorbance;
                    break;
                case "Intensity":
                    plotType = PlotType.Intensity;
                    break;
                case "Reference":
                    plotType = PlotType.ReferenceIntensity;
                    break;
                default: 
                    return;
            }
            CreatePlotModel(plotType);
        }
        #endregion

        private void CreatePlotModel(PlotType pType)
        {
            var plotModel = new PlotModel { Title = string.IsNullOrEmpty(ScanData.ScanResultFileName) ? "" : ScanData.ScanResultFileName.Remove(ScanData.ScanResultFileName.Length - 4) };
            List<double> plotData;
            String yAxisName = string.Empty; 

            switch (pType)
            {
                case PlotType.Intensity:
                    plotData = ScanData.Intensity;
                    yAxisName = "Intensity";
                    break;
                case PlotType.ReferenceIntensity:
                    plotData = ScanData.Reference;
                    yAxisName = "Reference Intensity";
                    break;
                case PlotType.Absorbance:
                    plotData = ScanData.Absorbance;
                    yAxisName = "Absorbance";
                    break;
                case PlotType.Reflectance:
                    plotData = ScanData.Reflectance;
                    yAxisName = "Reflectance";
                    break;
                default:
                    return;
            }

            var series = new LineSeries();
            for (int i = 0; i < ScanData.WaveLength.Count; i++)
            {
                series.Points.Add(new DataPoint(ScanData.WaveLength[i], plotData[i]));
            }

            plotModel.TextColor = OxyColors.White;
            plotModel.PlotAreaBorderColor = OxyColors.White;
            plotModel.TitleColor = OxyColors.White;
            plotModel.PlotAreaBackground = OxyColors.Black;
            plotModel.Background = OxyColors.Black;

            var xAxis = new LinearAxis
            {
                Position = AxisPosition.Bottom,
                Title = "Wavelength (nm)"
            };
            plotModel.Axes.Add(xAxis);

            var yAxis = new LinearAxis
            {
                Position = AxisPosition.Left,
                Title = yAxisName
            };
            plotModel.Axes.Add(yAxis);

            plotModel.Series.Add(series);
            plotView.Model = plotModel;
        }
    }
}
