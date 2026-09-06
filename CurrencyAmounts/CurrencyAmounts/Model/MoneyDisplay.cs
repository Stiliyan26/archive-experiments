using CurrencyAmounts.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Model
{
    public class MoneyDisplay
    {
        public decimal Amount { get; }
        public CurrencyEnum Currency { get; }

        public MoneyDisplay(decimal amount, CurrencyEnum currency)
        {
            Amount = amount;
            Currency = currency;
        }

        public override string ToString() => $"{Amount:F2} {Currency}";
    }
}
