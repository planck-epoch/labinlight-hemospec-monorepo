using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Deployment.Application;

namespace LabinLightApi.Models
{
    public class DeviceConfigLog
    {
        public List<string> FoundConfigs { get; set; }
        public string ConfigToSet { get; set; }
        public string ConfigToSetId { get; set; }
        public int Result { get; set; }
        public int DefaultResult { get; set; }
        public string Default { get; set; }
        public string AppVersion { get; }


        public DeviceConfigLog()
        {
            FoundConfigs = new List<string>();
            if (ApplicationDeployment.IsNetworkDeployed)
            {
                AppVersion = ApplicationDeployment.CurrentDeployment.CurrentVersion.ToString();
            }
            else
            {
                AppVersion = "Unkown";
            }
        }
    }
}
