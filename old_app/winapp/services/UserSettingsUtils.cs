using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LabinLightScan.services
{

    public static class UserSettingsUtil
    {
        private static string GetSettingsPath(string key)
        {
            var appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            var lilDir = System.IO.Path.Combine(appData, "LabinLightScan");
            if (!System.IO.Directory.Exists(lilDir))
                System.IO.Directory.CreateDirectory(lilDir);
            return System.IO.Path.Combine(lilDir, key);
        }

        public static void WriteLastAnalysisBundle(string value)
        {
            try
            {
                System.IO.File.WriteAllText(GetSettingsPath("last_analysis_type"), value ?? "");
            }
            catch { }
        }
       
        public static string ReadLastAnalysisBundle()
        {
            try
            {
                var path = GetSettingsPath("last_analysis_type");
                if (System.IO.File.Exists(path))
                    return System.IO.File.ReadAllText(path).Trim();
            }
            catch { }
            return null;
        }
    }
}
