using CurrencyAmounts.Model;
using CurrencyAmounts.ViewModel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Services
{
    public interface IInvoiceCalculator
    {
        InvoiceTotalResult CalculateTotal(IEnumerable<InvoiceItemViewModel> items);
    }
}
