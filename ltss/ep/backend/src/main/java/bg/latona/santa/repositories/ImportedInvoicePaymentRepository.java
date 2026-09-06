package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedInvoicePayment;
import bg.latona.santa.entities.wato.ImportedInvoice;
import bg.latona.santa.entities.wato.ImportedInvoicePayment;
import bg.latona.santa.entities.wato.ImportedLegalPerson;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedInvoicePaymentRepository extends CommonRepository<ImportedInvoicePayment, QImportedInvoicePayment, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoicePayment> findByForeignIdAndInvoice(String foreignId, @Nullable ImportedInvoice invoice);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoicePayment> findByForeignContragentIdAndContragent(String foreignContragentId, @Nullable ImportedLegalPerson contragent);
}