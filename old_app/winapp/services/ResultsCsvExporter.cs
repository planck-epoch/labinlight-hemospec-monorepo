using LabinLightApi.Models;
using LabinLightScan.ServiceClient;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using static System.Net.Mime.MediaTypeNames;
using System.Windows;
using System.Windows.Forms;
using System.Globalization;

namespace LabinLightScan.services
{
    public class ResultsCsvExporter
    {

        private readonly string exportPath;


        public static readonly ResultsCsvExporter instance = new ResultsCsvExporter();

        private ResultsCsvExporter()
        {
            string path = Environment.GetEnvironmentVariable("LIL_CSV_EXPORT_PATH");
            string disabled_env = Environment.GetEnvironmentVariable("LIL_CSV_EXPORT_DISABLED");
            bool disabled = disabled_env != null && "TRUE".Equals(disabled_env);
            if (!disabled)
            {
                if (path != null && Directory.Exists(path))
                {
                    exportPath = path;
                }
                else { 
                    MessageBoxResult dialogResult = System.Windows.MessageBox.Show("Configurar directório de exportar resultados em CSV?", "CSV Export", MessageBoxButton.YesNo);
                    if (dialogResult == MessageBoxResult.Yes)
                    {
                        FolderBrowserDialog folderDlg = new FolderBrowserDialog();
                        folderDlg.ShowNewFolderButton = true;
                        DialogResult result = folderDlg.ShowDialog();
                        if (result == DialogResult.OK)
                        {
                            exportPath = folderDlg.SelectedPath;
                            Environment.SetEnvironmentVariable("LIL_CSV_EXPORT_PATH", exportPath, EnvironmentVariableTarget.User);
                        }
                    }
                    else if (dialogResult == MessageBoxResult.No)
                    {
                        Environment.SetEnvironmentVariable("LIL_CSV_EXPORT_DISABLED", "TRUE", EnvironmentVariableTarget.User);
                    }
                }
            }
        }

        internal void export(String processNbr, List<BloodResult> results)
        {
            if(this.exportPath == null) { return; }
            try
            {
                Dictionary<String, String> resultsDictionary = new Dictionary<String, String>();
                foreach (var result in results)
                {
                    resultsDictionary[result.Code] = !String.IsNullOrEmpty(result.ValueString) ? result.ValueString : "";
                }
                File.WriteAllText(
                    String.Format("{0}\\{1}.csv", exportPath, DateTimeOffset.Now.ToUnixTimeSeconds()),
                    String.Format("{0};{1};{2};{3};{4};{5};{6}", 
                        processNbr,
                        resultsDictionary.ContainsKey("Eritrocitos") ? resultsDictionary["Eritrocitos"] : "",
                        resultsDictionary.ContainsKey("Hemoglobina") ? resultsDictionary["Hemoglobina"] : "",
                        resultsDictionary.ContainsKey("Hematocrito") ? resultsDictionary["Hematocrito"] : "",
                        resultsDictionary.ContainsKey("RDW") ? resultsDictionary["RDW"] : "",
                        resultsDictionary.ContainsKey("PCR") ? resultsDictionary["PCR"] : "",
                        resultsDictionary.ContainsKey("Creatinina") ? resultsDictionary["Creatinina"] : ""
                   )
                );
            }
            catch (Exception)
            {

                throw;
            }
        }

        internal void setup()
        {
            // do nothing just ensure instance is ready
        }
    }
}
