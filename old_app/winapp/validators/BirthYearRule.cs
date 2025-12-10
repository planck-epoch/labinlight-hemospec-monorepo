using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace LabinLightScan.validators
{
    public class BirthYearRule : ValidationRule
    {
        private readonly int Min = 1900;
        private readonly int Max = DateTime.Now.Year + 1;

        public BirthYearRule()
        {
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            int year = 0;

            try
            {
                if (value!= null && ((string)value).Length == 4)
                {
                    year = Int32.Parse((String)value);
                }
            }
            catch (Exception e)
            {
                return new ValidationResult(false, $"Valor inválido");
            }

            if ((year < Min) || (year > Max))
            {
                return new ValidationResult(false,
                  $"Valor entre {Min} e {Max}.");
            }
            return ValidationResult.ValidResult;
        }
    }
}
