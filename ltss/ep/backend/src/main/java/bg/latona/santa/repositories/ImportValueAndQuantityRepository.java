package bg.latona.santa.repositories;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementType;
import bg.latona.santa.entities.selfie.ImportValueAndQuantity;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.QImportValueAndQuantity;


public interface ImportValueAndQuantityRepository extends CommonRepository<ImportValueAndQuantity, QImportValueAndQuantity, Long> {
    //@RestResource(exported = false) //don't expose methods that are not checking permissions
    //ImportValueAndQuantity findFirstByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted);

    List<ImportValueAndQuantity> findAllByElectricityInvoiceIsNullAndIsValidIsTrueAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
            LocalDate periodFrom, LocalDate periodTo, LoiDocumentType loiDocumentType, boolean deleted, ManagedCompany company
    );

     Optional<List<ImportValueAndQuantity>> findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
             String reportingPointOwn, LoiDocumentType loiDocumentType, AgreementType agreementType, LocalDate periodFrom, LocalDate periodTo, boolean deleted, ManagedCompany company
    );
}
