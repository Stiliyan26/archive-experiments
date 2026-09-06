using CurrencyAmounts.Commands;
using CurrencyAmounts.Data;
using CurrencyAmounts.Enum;
using CurrencyAmounts.Model;
using CurrencyAmounts.Services;
using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows.Input;

namespace CurrencyAmounts.ViewModel
{
    public class InvoiceViewModel : BaseViewModel
    {
        private ObservableCollection<InvoiceItemViewModel> _invoiceItems;
        public ObservableCollection<InvoiceItemViewModel> InvoiceItems
        {
            get => _invoiceItems;
            set
            {
                _invoiceItems = value;
                OnPropertyChanged();
            }
        }

        private CurrencyEnum _selectedCurrency;
        public CurrencyEnum SelectedCurrency
        {
            get => _selectedCurrency;
            set
            {
                _selectedCurrency = value;
                CalculateInvoiceTotal();
                OnPropertyChanged();
            }
        }

        private string _total;
        public string Total
        {
            get => _total;
            set
            {
                _total = value;
                OnPropertyChanged();
            }
        }

        private decimal _subtotal;
        public decimal Subtotal
        {
            get => _subtotal;
            set
            {
                _subtotal = value;
                OnPropertyChanged();
            }
        }

        private decimal _itemDiscount;
        public decimal ItemDiscount
        {
            get => _itemDiscount;
            set
            {
                _itemDiscount = value;
                OnPropertyChanged();
            }
        }

        private decimal _overallDiscount;
        public decimal OverallDiscount
        {
            get => _overallDiscount;
            set
            {
                _overallDiscount = value;
                OnPropertyChanged();
            }
        }

        private decimal _vatAmount;
        public decimal VATAmount
        {
            get => _vatAmount;
            set
            {
                _vatAmount = value;
                OnPropertyChanged();
            }
        }

        private decimal _finalTotal;
        public decimal FinalTotal
        {
            get => _finalTotal;
            set
            {
                _finalTotal = value;
                OnPropertyChanged();
            }
        }

        public ICommand LoadDataCommand { get; }
        public ICommand CalculateCommand { get; }

        public InvoiceViewModel()
        {
            InvoiceItems = new ObservableCollection<InvoiceItemViewModel>();

            //LoadDataCommand = new RelayCommand(_ => LoadData());
            CalculateCommand = new RelayCommand(_ => CalculateInvoiceTotal());
            SelectedCurrency = CurrencyEnum.USD;
            LoadData();
        }

        private void LoadData()
        {
            using (var context = new AppDbContext())
            {
                context.Database.EnsureDeleted();
                context.Database.EnsureCreated();

                Money<USD> moneyUnit = new Money<USD>(150m);

                InvoiceItem invoiceItem = new InvoiceItem
                {
                    Description = "Sample Item",
                    Quantity = 3
                };

                invoiceItem.MoneyAmount = moneyUnit;

                context.InvoiceItems.Add(invoiceItem);

                if (!context.InvoiceItems.Any())
                {
                    // Item with quantity > 10 triggers item discount
                    context.InvoiceItems.Add(new InvoiceItem
                    {
                        Amount = 100,
                        Currency = CurrencyEnum.USD,
                        Description = "Item 1",
                        Quantity = 12
                    });
                    // High enough price that overall discount may apply
                    context.InvoiceItems.Add(new InvoiceItem
                    {
                        Amount = 800,
                        Currency = CurrencyEnum.EUR,
                        Description = "Item 2",
                        Quantity = 2
                    });
                    context.InvoiceItems.Add(new InvoiceItem
                    {
                        Amount = 300,
                        Currency = CurrencyEnum.BGN,
                        Description = "Item 3",
                        Quantity = 8
                    });

                    context.SaveChanges();
                }

                var items = context.InvoiceItems.ToList();
                InvoiceItems.Clear();

                foreach (var item in items)
                {
                    InvoiceItems.Add(new InvoiceItemViewModel(item));
                }
            }

            CalculateInvoiceTotal();
        }

        private void CalculateInvoiceTotal()
        {
            IInvoiceCalculator calculator = InvoiceCalculatorFactory.GetCalculator(SelectedCurrency);
            InvoiceTotalResult result = calculator.CalculateTotal(InvoiceItems);

            Subtotal = result.Subtotal.Amount;
            ItemDiscount = result.ItemDiscount.Amount;
            OverallDiscount = result.OverallDiscount.Amount;
            VATAmount = result.VATAmount.Amount;
            FinalTotal = result.FinalTotal.Amount;
            Total = result.TotalDisplay;
        }
    }
}
