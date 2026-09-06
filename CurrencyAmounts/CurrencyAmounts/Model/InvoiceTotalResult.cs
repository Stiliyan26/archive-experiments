using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Model
{
    public class InvoiceTotalResult
    {
        public MoneyDisplay Subtotal { get; set; }
        public MoneyDisplay ItemDiscount { get; set; }
        public MoneyDisplay OverallDiscount { get; set; }
        public MoneyDisplay VATAmount { get; set; }
        public MoneyDisplay FinalTotal { get; set; }
        public string TotalDisplay { get; set; }
    }
}
