package bg.latona.santa.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;

import bg.latona.santa.entities.invoice.QInvoice;
import bg.latona.santa.entities.invoice.Invoice;

public interface InvoiceRepository extends CommonRepository<Invoice, QInvoice, Long> {
	@Query(value = "SELECT coalesce(max(invoiceNum), 0) FROM Invoice WHERE isIssued = true")
	BigDecimal getMaxIssuedInvoiceNum();
	@Query(value = "SELECT coalesce(max(invoiceDate), '2001-01-01') FROM Invoice WHERE isIssued = true")
	LocalDate getMaxIssuedInvoiceDate();
}