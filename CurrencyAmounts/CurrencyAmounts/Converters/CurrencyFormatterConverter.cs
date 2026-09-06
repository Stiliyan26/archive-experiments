using System;
using System.Globalization;
using System.Windows.Data;

namespace CurrencyAmounts.Converters
{
    public class CurrencyFormatterConverter : IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            if (values.Length >= 2
                && values[0] is decimal amount
                && values[1] != null)
            {
                // Convert the second value to string regardless of its type.
                string currency = values[1].ToString();
                return $"{amount:F2} {currency}";
            }
            return Binding.DoNothing;
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
