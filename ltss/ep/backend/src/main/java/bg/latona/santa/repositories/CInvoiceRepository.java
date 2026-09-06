package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.common.CInvoice;
import bg.latona.santa.entities.santa.common.QCInvoice;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;


public interface CInvoiceRepository extends CommonRepository<CInvoice, QCInvoice, Long> {

	@Query(value = "SELECT coalesce(max(invoiceNum), 0) FROM CInvoice WHERE isIssued = true")
	BigDecimal getMaxIssuedInvoiceNum();
	@Query(value = "SELECT coalesce(max(invoiceDate), '2001-01-01') FROM CInvoice WHERE isIssued = true")
	LocalDate getMaxIssuedInvoiceDate();
}