package bg.latona.santa.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementType;
import bg.latona.santa.entities.selfie.ImportValue;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.QImportValue;


public interface ImportValueRepository extends CommonRepository<ImportValue, QImportValue, Long> {

    // @RestResource(exported = false) //don't expose methods that are not checking permissions
	// ImportValue findFirstByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted);

    List<ImportValue> findAllByElectricityInvoiceIsNullAndIsValidIsTrueAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
            LocalDate periodFrom, LocalDate periodTo, LoiDocumentType loiDocumentType, boolean deleted, ManagedCompany company
    );

    Optional<List<ImportValue>> findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
            String reportingPointOwn, LoiDocumentType loiDocumentType, AgreementType agreementType, LocalDate periodFrom, LocalDate periodTo, boolean deleted, ManagedCompany company
    );
}
