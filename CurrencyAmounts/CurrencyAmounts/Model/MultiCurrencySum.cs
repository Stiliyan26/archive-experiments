    using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CurrencyAmounts.Model
{
    public class MultiCurrencySum
    {
        private readonly Dictionary<Type, decimal> _sums = new Dictionary<Type, decimal>();
    
        public void Add<TCurrency>(Money<TCurrency> money) where TCurrency : Currency, new()
        {
            var key = typeof(TCurrency);

            //Money<USD> money2 = new Money<USD>(203.2m);
            //Money<EUR> money3 = new Money<EUR>(132.2m);
            //Money<BGN> money4 = new Money<BGN>(202.2m);
            //Money<USD> money5 = new Money<USD>(202.2m);

            //money2 += money3;
            //money2 += money5;


            if (_sums.ContainsKey(key))
            {
                _sums[key] += ((IMoney)money).GetAmount();
            }

            else
            {
                _sums[key] = ((IMoney)money).GetAmount();
            }
        }

        public static MultiCurrencySum operator +(MultiCurrencySum a, MultiCurrencySum b)
        {
            MultiCurrencySum result = new MultiCurrencySum();

            MergeSums(result._sums, a._sums, (x, y) => x + y);
            MergeSums(result._sums, b._sums, (x, y) => x + y);

            return result;
        }

        public static MultiCurrencySum operator -(MultiCurrencySum a, MultiCurrencySum b) {
            
            MultiCurrencySum result = new MultiCurrencySum();

            MergeSums(result._sums, a._sums, (x, y) => x - y);
            MergeSums(result._sums, b._sums, (x, y) => x - y);

            return result;
        }

        public Money<TCurrency> ToMoney<TCurrency>(Func<Type, decimal> exchangeRateProvider) where TCurrency : Currency, new()
        {
            decimal total = 0;

            foreach (KeyValuePair<Type, decimal> kvp in _sums)
            {
                decimal rate = (kvp.Key == typeof(TCurrency))
                    ? 1
                    : exchangeRateProvider(kvp.Key);

                total += kvp.Value * rate;
            }

            return new Money<TCurrency>(total);
        }

        public override string ToString()
        {
            string result = "";

            foreach (KeyValuePair<Type, decimal> kvp in _sums)
            {
                result += $"{kvp.Value} {kvp.Key.Name} ";
            }

            return result.Trim();
        }

        private static void MergeSums(
            Dictionary<Type, decimal> target,
            Dictionary<Type, decimal> source,
            Func<decimal, decimal, decimal> mergeOperation
        )
        {
            foreach (KeyValuePair<Type, decimal> kvp in source)
            {
                if (target.ContainsKey(kvp.Key))
                {
                    target[kvp.Key] = mergeOperation(target[kvp.Key], kvp.Value);
                }

                else
                {
                    target[kvp.Key] = kvp.Value;
                } 
            }
        }
    }
}
