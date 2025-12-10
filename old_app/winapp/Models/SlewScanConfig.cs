using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace LabinLightApi.Models
{
    public class SlewScanConfig
    {
        public SlewScanConfigHead Head { get; set; }
        public SlewScanSection[] Section { get; set; }
        public int LastUpdate { get; set; }
        public int LastCalibrationAt { get; set; }
        public int id { get; set; }
    }
    public class SlewScanSection
    {

        public byte section_scan_type { get; set; }
        public byte width_px { get; set; }
        public int wavelength_start_nm { get; set; }
        public int wavelength_end_nm { get; set; }
        public int num_patterns { get; set; }
        public int exposure_time { get; set; }
    }
    public class SlewScanConfigHead
    {
        public byte scan_type { get; set; }
        public int scanConfigIndex { get; set; }
        public string ScanConfig_serial_number { get; set; }
        public string config_name { get; set; }
        public int num_repeats { get; set; }
        public byte num_sections { get; set; }
    }

}
