package bg.latona.santa.anvoice.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.anvoice.entities.ImportXenergieInvoice;
import bg.latona.santa.anvoice.entities.QImportXenergieInvoice;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.repositories.CommonRepository;

public interface ImportXenergieInvoiceRepository extends CommonRepository<ImportXenergieInvoice, QImportXenergieInvoice, Long> {

    @RestResource(exported = false) // don't expose methods that are not checking permissions
    List<ImportXenergieInvoice> findAllByElectricityInvoiceIsNullAndIsValidIsTrueAndBillingDateBetweenAndDocumentTypeIdAndCompanyAndDeleted(
            LocalDate periodFrom, LocalDate periodTo, Long documentTypeId, ManagedCompany company, boolean deleted);

    @RestResource(exported = false) // don't expose methods that are not checking permissions
    ImportXenergieInvoice findFirstByBillingDateAndZnumberAndProductAndCompanyAndDeleted(
        LocalDate billingDate, String znumber, String product, ManagedCompany company, boolean deleted);
}
