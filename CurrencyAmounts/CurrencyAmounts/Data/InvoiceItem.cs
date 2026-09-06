using CurrencyAmounts.Enum;
using CurrencyAmounts.Model;
using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;


namespace CurrencyAmounts.Data
{
    public class InvoiceItem
    {
        [Key]
        public int Id { get; set; }
        public decimal Amount { get; set; }
        public CurrencyEnum Currency { get; set; }
        public string? Description { get; set; }
        public int Quantity { get; set; }

        [NotMapped]
        public IMoney MoneyAmount
        {
            get
            {
                return Currency switch
                {
                    CurrencyEnum.USD => new Money<USD>(Amount),
                    CurrencyEnum.EUR => new Money<EUR>(Amount),
                    CurrencyEnum.BGN => new Money<BGN>(Amount),
                    _ => throw new NotSupportedException($"Currency {Currency} is not supported")
                };
            }
            set
            {
                switch (value)
                {
                    case Money<USD> usd:
                        Amount = ((IMoney)usd).GetAmount();
                        Currency = CurrencyEnum.USD;
                        break;
                    case Money<EUR> eur:
                        Amount = ((IMoney)eur).GetAmount();
                        Currency = CurrencyEnum.EUR;
                        break;
                    case Money<BGN> bgn:
                        Amount = ((IMoney)bgn).GetAmount();
                        Currency = CurrencyEnum.BGN;
                        break;
                    default:
                        throw new NotSupportedException("Unsupported money type");
                }
            }
        }
    }
}
