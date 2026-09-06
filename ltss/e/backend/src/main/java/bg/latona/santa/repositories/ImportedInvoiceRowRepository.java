package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedInvoiceRow;
import bg.latona.santa.entities.wato.ImportedInvoice;
import bg.latona.santa.entities.wato.ImportedInvoiceRow;
import bg.latona.santa.entities.wato.ImportedOrderRow;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedInvoiceRowRepository extends CommonRepository<ImportedInvoiceRow, QImportedInvoiceRow, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoiceRow> findByForeignInvoiceIdAndInvoice(String foreignInvoiceId, @Nullable ImportedInvoice invoice);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedInvoiceRow> findByForeignOrderRowIdAndOrderRow(String foreignOrderRowId, @Nullable ImportedOrderRow orderRow);
}