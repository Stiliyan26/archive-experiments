package bg.latona.santa.repositories;

import org.springframework.data.jpa.repository.Query;

import bg.latona.santa.entities.santa.finance.FInvInvoice;
import bg.latona.santa.entities.santa.finance.QFInvInvoice;

public interface FInvInvoiceRepository extends CommonRepository<FInvInvoice, QFInvInvoice, Long>{
	@Query(value = "SELECT coalesce(max(invNo), 0) FROM FInvInvoice")
	Long getMaxAutoIncNum();
}