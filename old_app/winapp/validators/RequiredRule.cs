using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace LabinLightScan.validators
{
    public class RequiredRule : ValidationRule
    {

        public RequiredRule()
        {
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            if(value == null || (value is String &&  ((string) value).Length == 0))
            {
                return new ValidationResult(false, $"Preenchimento obrigatório");
            }

            return ValidationResult.ValidResult;
        }
    }
}
