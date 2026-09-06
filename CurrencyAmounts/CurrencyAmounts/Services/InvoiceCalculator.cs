using CurrencyAmounts.Enum;
using CurrencyAmounts.Helpers;
using CurrencyAmounts.Model;
using CurrencyAmounts.ViewModel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Services
{
    public class InvoiceCalculator<TCurrency> : IInvoiceCalculator
        where TCurrency : Currency, new()
    {
        public InvoiceTotalResult CalculateTotal(IEnumerable<InvoiceItemViewModel> items)
        {
            const decimal vatRate = 0.20m;
            const int itemDiscountThreshold = 10;
            const decimal itemDiscountRate = 0.05m;
            const decimal overallDiscountThreshold = 1000m;
            const decimal overallDiscountRate = 0.10m;

            MultiCurrencySum multiSum = new();
            MultiCurrencySum itemDiscountSum = new();

            //var mn = new Money<USD>(100);
            //IMoney mna = new Money<USD>(100);

            //var obv = new Money<Currency>(100);
            //Money<USD> usd = new Money<USD>(100);
            //Money<USD> usd2 = new Money<USD>(100);
            //Money<EUR> eur = new Money<EUR>(100);

            //usd += eur;
            //usd2 += usd;

            //decimal usd3 = usd2.GetAmount() + eur.GetAmount();

            foreach (var item in items)
            {
                Money<TCurrency> convertedMoney = ConvertToTarget<TCurrency>(item.MoneyTotal, InvoiceCalculatorHelper.GetExchangeRate);

                Money<TCurrency> discounted = convertedMoney;

                if (item.Quantity > itemDiscountThreshold)
                {
                    discounted = convertedMoney * new Money<TCurrency>(1 - itemDiscountRate);

                    Money<TCurrency> discount = convertedMoney - discounted;

                    itemDiscountSum.Add(discount);
                }

                multiSum.Add(discounted);
            }

            Money<TCurrency> subtotal = multiSum.ToMoney<TCurrency>(
                fromType => InvoiceCalculatorHelper.GetExchangeRate(
                    InvoiceCalculatorHelper.GetCurrencyEnumFromType(fromType),
                    InvoiceCalculatorHelper.GetCurrencyEnumFromType(typeof(TCurrency))
               )
            );
            Money<TCurrency> discountTotal = itemDiscountSum.ToMoney<TCurrency>(
                fromType => InvoiceCalculatorHelper.GetExchangeRate(
                    InvoiceCalculatorHelper.GetCurrencyEnumFromType(fromType),
                    InvoiceCalculatorHelper.GetCurrencyEnumFromType(typeof(TCurrency))
               )
            );

            Money<TCurrency> overallDiscount = new Money<TCurrency>(0);

            if (subtotal > new Money<TCurrency>(overallDiscountThreshold))
            {
                overallDiscount = subtotal * new Money<TCurrency>(overallDiscountRate);
                subtotal -= overallDiscount;
            }

            Money<TCurrency> vatMoney = subtotal * new Money<TCurrency>(vatRate);
            Money<TCurrency> finalTotal = subtotal + vatMoney;

            return new InvoiceTotalResult
            {
                Subtotal = new MoneyDisplay(((IMoney)subtotal).GetAmount(), subtotal.Currency),
                ItemDiscount = new MoneyDisplay(((IMoney)discountTotal).GetAmount(), discountTotal.Currency),
                OverallDiscount = new MoneyDisplay(((IMoney)overallDiscount).GetAmount(), overallDiscount.Currency),
                VATAmount = new MoneyDisplay(((IMoney)vatMoney).GetAmount(), vatMoney.Currency),
                FinalTotal = new MoneyDisplay(((IMoney)finalTotal).GetAmount(), finalTotal.Currency),
                TotalDisplay = finalTotal.ToString()
            };
        }

        public static Money<TCurrency> ConvertToTarget<TCurrency>(
            IMoney money,
            Func<CurrencyEnum, CurrencyEnum, decimal> exchangeRateProvider
        ) where TCurrency : Currency, new()
        {
            CurrencyEnum sourceCurrency = money.Currency;

            CurrencyEnum targetCurrency = InvoiceCalculatorHelper.GetCurrencyEnumFromType(typeof(TCurrency));

            decimal rate = exchangeRateProvider(sourceCurrency, targetCurrency);

            return new Money<TCurrency>(money.GetAmount() * rate);
        }
    }
}
