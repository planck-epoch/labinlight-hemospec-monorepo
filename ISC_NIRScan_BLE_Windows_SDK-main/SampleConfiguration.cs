using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Background;
using Windows.Storage;
using Windows.UI.Xaml.Controls;
// pfk password: ISC7825

namespace ISC_BLE_SDK
{
    public partial class MainPage : Page
    {
        public const string FEATURE_NAME = "Scenarios";

        List<Scenario> scenarios = new List<Scenario>
        {
            new Scenario() { Title="Discover ISC devices", ClassType=typeof(Scenario1_Discovery) },
            new Scenario() { Title="Connect to a ISC device", ClassType=typeof(Scenario2_Client) },
            new Scenario() { Title="Show Device Information", ClassType=typeof(Scenario3_DeviceInfo) },
            new Scenario() { Title="Set Scan Configuration", ClassType=typeof(Scenario4_SetConfig) },
            new Scenario() { Title="Perform Scan", ClassType=typeof(Scenario5_PerformScan) },
            new Scenario() { Title="View Spectrums", ClassType=typeof(Scenario6_ViewSpectrum) }
        };

        public int GetScenarioIndex(string scenarioName)
        {
            if (scenarioName == "Scenario1_Discovery")
                return 0;
            else if (scenarioName == "Scenario2_Client")
                return 1;
            else if (scenarioName == "Scenario3_DeviceInfo")
                return 2;
            else if (scenarioName == "Scenario4_SetConfig")
                return 3;
            else if (scenarioName == "Scenario5_PerformScan")
                return 4;
            else if (scenarioName == "Scenario6_ViewSpectrum")
                return 5;
            else
                return -1;
        }
    }

    public class Scenario
    {
        public string Title { get; set; }
        public Type ClassType { get; set; }
    }
}
