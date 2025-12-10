using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace LabinLightApi.Models
{
    public class BloodResult
    {
        public string Code { get; set; }
        public string Label { get; set; }
        public string Unit { get; set; }
        public double? Value { get; set; }
        public string ValueString { get; set; }
        public string ReferenceValues { get; set; }

    }
}
