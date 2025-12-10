using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LabinLightScan.ServiceClient
{
    internal class KnownServiceErrorException : Exception
    {
        public KnownServiceErrorException(string errorMsg)
        : base(errorMsg)
        {

        }
    }
}
