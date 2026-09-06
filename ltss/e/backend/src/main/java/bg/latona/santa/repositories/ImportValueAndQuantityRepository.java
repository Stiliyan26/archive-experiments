package bg.latona.santa.repositories;


import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.*;
import org.springframework.data.rest.core.annotation.RestResource;


public interface ImportValueAndQuantityRepository extends CommonRepository<ImportValueAndQuantity, QImportValueAndQuantity, Long> {
    //@RestResource(exported = false) //don't expose methods that are not checking permissions
    //ImportValueAndQuantity findFirstByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted);

    List<ImportValueAndQuantity> findAllByElectricityInvoiceAndIsValidAndPeriodFromAndPeriodToAndLoiDocumentTypeAndAgreementType_CodeInAndPowerPlant_IdInAndDeletedAndCompany(
            ElectricityInvoice electricityInvoice, Boolean isValid, LocalDate periodFrom, LocalDate periodTo, LoiDocumentType loiDocumentType, Collection<String> agreementType_code, Collection<Long> powerPlant_id, boolean deleted, ManagedCompany company
    );

     Optional<List<ImportValueAndQuantity>> findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
             String reportingPointOwn, LoiDocumentType loiDocumentType, AgreementType agreementType, LocalDate periodFrom, LocalDate periodTo, boolean deleted, ManagedCompany company
    );

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ImportValueAndQuantity findFirstByReportingPointOwnAndCompanyAndDeleted(String reportingPointOwn, ManagedCompany company, boolean deleted);
}
