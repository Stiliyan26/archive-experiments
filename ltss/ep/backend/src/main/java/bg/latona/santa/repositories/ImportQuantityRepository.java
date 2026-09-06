package bg.latona.santa.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementType;
import bg.latona.santa.entities.selfie.ImportQuantity;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.QImportQuantity;


public interface ImportQuantityRepository extends CommonRepository<ImportQuantity, QImportQuantity, Long> {
    //@RestResource(exported = false) //don't expose methods that are not checking permissions
    //ImportQuantity findFirstByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted);

    List<ImportQuantity> findAllByElectricityInvoiceIsNullAndIsValidIsTrueAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
            LocalDate periodFrom, LocalDate periodTo, LoiDocumentType loiDocumentType, boolean deleted, ManagedCompany company
    );

    Optional<List<ImportQuantity>> findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
            String reportingPointOwn, LoiDocumentType loiDocumentType, AgreementType agreementType, LocalDate periodFrom, LocalDate periodTo, boolean deleted, ManagedCompany company
    );
}
