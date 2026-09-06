using CurrencyAmounts.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Model
{
    public interface IMoney
    {
        decimal GetAmount();
        CurrencyEnum Currency { get; }
    }
}
