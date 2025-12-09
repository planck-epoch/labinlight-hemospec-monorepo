using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using Windows.ApplicationModel;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Automation.Peers;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace ISC_BLE_SDK
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public static MainPage Current;

        public string SelectedBleDeviceId;
        public string SelectedBleDeviceName = "No device selected";
        public bool SelectedDeviceConnected = false;

        public MainPage()
        {
            this.InitializeComponent();

            // This is a static public property that allows downstream pages to get a handle to the MainPage instance
            // in order to call methods that are in this class.
            Current = this;
            SampleTitle.Text = FEATURE_NAME;
            DisplayAppVersion();
        }
        private void DisplayAppVersion()
        {
            PackageVersion version = Package.Current.Id.Version;
            string appVersion = $"{version.Major}.{version.Minor}.{version.Build}";
            Header.Text = "ISC NIRScan BLE (" + appVersion + ")";
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            // Populate the scenario list from the SampleConfiguration.cs file
            var itemCollection = new List<Scenario>();
            int i = 1;
            foreach (Scenario s in scenarios)
            {
                itemCollection.Add(new Scenario { Title = $"{i++}) {s.Title}", ClassType = s.ClassType });
            }
            ScenarioControl.ItemsSource = itemCollection;

            if (Window.Current.Bounds.Width < 640)
            {
                ScenarioControl.SelectedIndex = -1;
            }
            else
            {
                ScenarioControl.SelectedIndex = 0;
            }
        }

        /// <summary>
        /// Called whenever the user changes selection in the scenarios list.  This method will navigate to the respective
        /// sample scenario page.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ScenarioControl_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            // Clear the status block when navigating scenarios.
            NotifyUser(String.Empty, NotifyType.StatusMessage);

            Scenario prevSelection = null;
            if (e.RemovedItems.Count != 0)
            {
                prevSelection = e.RemovedItems[0] as Scenario;
            }

            ListBox scenarioListBox = sender as ListBox;
            if (scenarioListBox.SelectedIndex > 1 && SelectedDeviceConnected == false)
            {
                try
                {
                    ScenarioFrame.Navigate(prevSelection.ClassType);
                    ScenarioControl.SelectionChanged -= ScenarioControl_SelectionChanged;
                    ScenarioControl.SelectedIndex = GetScenarioIndex(prevSelection.ClassType.Name);
                    ScenarioControl.SelectionChanged += ScenarioControl_SelectionChanged;
                    NotifyUser("No device connected!", NotifyType.ErrorMessage);
                }
                catch { }
                return;
            }
            else if (scenarioListBox.SelectedIndex == 5 && ScanData.WaveLength.Count == 0) // Scenario6_ViewSpectrum is selected
            {
                try
                {
                    ScenarioFrame.Navigate(prevSelection.ClassType);
                    ScenarioControl.SelectionChanged -= ScenarioControl_SelectionChanged;
                    ScenarioControl.SelectedIndex = GetScenarioIndex(prevSelection.ClassType.Name);
                    ScenarioControl.SelectionChanged += ScenarioControl_SelectionChanged;
                    NotifyUser("No spectrum data!", NotifyType.ErrorMessage);
                }
                catch { }
                return;
            }

            Scenario s = scenarioListBox.SelectedItem as Scenario;
            if (s != null)
            {
                ScenarioFrame.Navigate(s.ClassType);
                if (Window.Current.Bounds.Width < 640)
                {
                    Splitter.IsPaneOpen = false;
                }
            }
        }

        public List<Scenario> Scenarios
        {
            get { return this.scenarios; }
        }

        /// <summary>
        /// Display a message to the user.
        /// This method may be called from any thread.
        /// </summary>
        /// <param name="strMessage"></param>
        /// <param name="type"></param>
        public void NotifyUser(string strMessage, NotifyType type)
        {
            // If called from the UI thread, then update immediately.
            // Otherwise, schedule a task on the UI thread to perform the update.
            if (Dispatcher.HasThreadAccess)
            {
                UpdateStatus(strMessage, type);
            }
            else
            {
                var task = Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () => UpdateStatus(strMessage, type));
            }
        }

        private void UpdateStatus(string strMessage, NotifyType type)
        {
            switch (type)
            {
                case NotifyType.StatusMessage:
                    StatusBorder.Background = new SolidColorBrush(Windows.UI.Colors.Green);
                    break;
                case NotifyType.StatusProcessing:
                    StatusBorder.Background = new SolidColorBrush(Windows.UI.Colors.DarkSlateBlue);
                    break;
                case NotifyType.ErrorMessage:
                    StatusBorder.Background = new SolidColorBrush(Windows.UI.Colors.Red);
                    break;
            }

            StatusBlock.Text = strMessage;

            // Collapse the StatusBlock if it has no text to conserve real estate.
            StatusBorder.Visibility = (StatusBlock.Text != String.Empty) ? Visibility.Visible : Visibility.Collapsed;
            if (StatusBlock.Text != String.Empty)
            {
                StatusBorder.Visibility = Visibility.Visible;
                StatusPanel.Visibility = Visibility.Visible;
            }
            else
            {
                StatusBorder.Visibility = Visibility.Collapsed;
                StatusPanel.Visibility = Visibility.Collapsed;
            }

			// Raise an event if necessary to enable a screen reader to announce the status update.
			var peer = FrameworkElementAutomationPeer.FromElement(StatusBlock);
			if (peer != null)
			{
				peer.RaiseAutomationEvent(AutomationEvents.LiveRegionChanged);
			}
		}

        async void Footer_Click(object sender, RoutedEventArgs e)
        {
            await Windows.System.Launcher.LaunchUriAsync(new Uri(((HyperlinkButton)sender).Tag.ToString()));
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            Splitter.IsPaneOpen = !Splitter.IsPaneOpen;
        }
    }
    public enum NotifyType
    {
        StatusMessage,
        StatusProcessing,
        ErrorMessage
    };
}
