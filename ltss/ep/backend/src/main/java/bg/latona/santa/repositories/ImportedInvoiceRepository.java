package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedInvoice;
import bg.latona.santa.entities.wato.ImportedInvoice;
import bg.latona.santa.entities.wato.ImportedOrder;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface ImportedInvoiceRepository extends CommonRepository<ImportedInvoice, QImportedInvoice, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoice> findByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoice> findByForeignOrderIdAndOrder(String foreignOrderId, ImportedOrder order);
}