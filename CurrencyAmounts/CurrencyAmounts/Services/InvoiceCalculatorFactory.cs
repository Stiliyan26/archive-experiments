using CurrencyAmounts.Enum;
using CurrencyAmounts.Model;
using System;

namespace CurrencyAmounts.Services
{
    public class InvoiceCalculatorFactory
    {
        public static IInvoiceCalculator GetCalculator(CurrencyEnum currency)
        {
            return currency switch
            {
                CurrencyEnum.USD => new InvoiceCalculator<USD>(),
                CurrencyEnum.EUR => new InvoiceCalculator<EUR>(),
                CurrencyEnum.BGN => new InvoiceCalculator<BGN>(),
                _ => throw new NotSupportedException($"Currency {currency} is not supported")
            };
        }
    }
}
