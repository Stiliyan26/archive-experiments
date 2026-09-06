using CurrencyAmounts.Commands;
using CurrencyAmounts.View;
using System.Windows;
using System.Windows.Input;

namespace CurrencyAmounts.ViewModel
{
    public class MainViewModel : BaseViewModel
    {
        public ICommand OpenInvoiceWindowCommand { get; }

        public MainViewModel()
        {
            OpenInvoiceWindowCommand = new RelayCommand(_ => OpenInvoiceWindow());
        }

        private void OpenInvoiceWindow()
        {
            InvoiceWindow invoiceWindow = new InvoiceWindow();

            invoiceWindow.Show();
        }
    }
}
