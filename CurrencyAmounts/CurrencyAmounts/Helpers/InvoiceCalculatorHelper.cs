using CurrencyAmounts.Enum;
using CurrencyAmounts.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Helpers
{
    public class InvoiceCalculatorHelper
    {
        public static decimal GetExchangeRate(CurrencyEnum fromCurrency, CurrencyEnum toCurrency)
        {
            return (fromCurrency, toCurrency) switch
            {
                (CurrencyEnum.EUR, CurrencyEnum.USD) => 1.1m,
                (CurrencyEnum.USD, CurrencyEnum.EUR) => 0.9m,
                (CurrencyEnum.BGN, CurrencyEnum.USD) => 0.55m,
                (CurrencyEnum.USD, CurrencyEnum.BGN) => 1.8m,
                (CurrencyEnum.EUR, CurrencyEnum.BGN) => 2m,
                (CurrencyEnum.BGN, CurrencyEnum.EUR) => 0.5m,
                _ => 1
            };
        }

        public static CurrencyEnum GetCurrencyEnumFromType(Type type)
        {
            if (type == typeof(USD)) return CurrencyEnum.USD;
            if (type == typeof(EUR)) return CurrencyEnum.EUR;
            if (type == typeof(BGN)) return CurrencyEnum.BGN;

            throw new NotSupportedException($"Unsupported currency type: {type.Name}");
        }
    }
}
