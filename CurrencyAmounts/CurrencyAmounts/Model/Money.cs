using CurrencyAmounts.Enum;
using CurrencyAmounts.Helpers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Model
{
    public class Money<TCurrency> : IMoney where TCurrency : Currency, new()
    {
        private readonly decimal _amount;
        public CurrencyEnum Currency => InvoiceCalculatorHelper.GetCurrencyEnumFromType(typeof(TCurrency));

        decimal IMoney.GetAmount() => _amount;
        public Money(decimal amount)
        {
            _amount = amount;
        }

        public static Money<TCurrency> operator +(Money<TCurrency> a, Money<TCurrency> b)
        {
            return new Money<TCurrency>(a._amount + b._amount);
        }

        public static Money<TCurrency> operator -(Money<TCurrency> a, Money<TCurrency> b)
        {
            return new Money<TCurrency>(a._amount - b._amount);
        }

        public static Money<TCurrency> operator *(Money<TCurrency> money, Money<TCurrency> factor)
        {
            return new Money<TCurrency>(money._amount * factor._amount);
        }
        public static bool operator >(Money<TCurrency> a, Money<TCurrency> b)
        {
            return a._amount > b._amount;
        }

        public static bool operator <(Money<TCurrency> a, Money<TCurrency> b)
        {
            return a._amount < b._amount;
        }

        public override string ToString()
        {
            return $"{_amount} {typeof(TCurrency).Name}";
        }
    }
}
