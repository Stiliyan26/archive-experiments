using CurrencyAmounts.Data;
using CurrencyAmounts.Enum;
using CurrencyAmounts.Model;
using Microsoft.EntityFrameworkCore.Metadata;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.ViewModel
{
    public class InvoiceItemViewModel
    {
        public int Id { get; }
        public CurrencyEnum Currency { get; }
        public string Description { get; }
        public int Quantity { get; }

        public IMoney MoneyUnit { get; }
        public decimal UnitAmount => MoneyUnit.GetAmount();

        public IMoney MoneyTotal => MultiplyMoney(MoneyUnit, Quantity);

        public string? TotalPrice => MoneyTotal.ToString();


        public InvoiceItemViewModel(InvoiceItem item)
        {
            Id = item.Id;
            Currency = item.Currency;
            Description = item.Description ?? "";
            Quantity = item.Quantity;

            MoneyUnit = Currency switch
            {
                CurrencyEnum.USD => new Money<USD>(item.Amount),
                CurrencyEnum.EUR => new Money<EUR>(item.Amount),
                CurrencyEnum.BGN => new Money<BGN>(item.Amount),

                _ => throw new NotSupportedException($"Currency {item.Currency} not supported")
            };
        }

        private IMoney MultiplyMoney(IMoney money, int quantity)
        {
            return money switch
            {
                Money<USD> usd => new Money<USD>(((IMoney)usd).GetAmount() * quantity),
                Money<EUR> eur => new Money<EUR>(((IMoney)eur).GetAmount() * quantity),
                Money<BGN> bgn => new Money<BGN>(((IMoney)bgn).GetAmount() * quantity),

                _ => throw new NotSupportedException("Unsupported currency type")
            };
        }
    }
}
