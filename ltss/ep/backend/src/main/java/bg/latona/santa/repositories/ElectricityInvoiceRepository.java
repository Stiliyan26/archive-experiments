package bg.latona.santa.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.QElectricityInvoice;


public interface ElectricityInvoiceRepository extends CommonRepository<ElectricityInvoice, QElectricityInvoice, Long>, JpaSpecificationExecutor<ElectricityInvoice> {

    @RestResource(exported = false)
    ElectricityInvoice findFirstByReportingPointOwnAndPeriodFromAndPeriodToAndCompanyAndDeleted(
            String reportingPointOwn,
            LocalDate periodFrom,
            LocalDate periodTo,
            ManagedCompany company,
            boolean deleted
    );

    @RestResource(exported = false)
    Optional<ElectricityInvoice> findByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
            String reportingPointOwn,
            LocalDate periodFrom,
            LocalDate periodTo,
            LoiDocumentType loiDocumentType,
            boolean deleted,
            ManagedCompany company
    );

    @RestResource(exported = false)
    Set<ElectricityInvoice> findAllByReportingPointOwnInAndSentAndIsValidAndCompanyAndDeletedAndDbFileIsNotNull(
            Set<String> accessPoints,
            boolean sent,
            boolean isValid,
            ManagedCompany company,
            boolean deleted
    );

    @RestResource(exported = false)
    List<ElectricityInvoice> findByTaxEventDateBetweenAndCompanyAndDeletedAndDbFileIsNotNullAndLoiDocumentType_ListOptionItemCodeAndReportingPointOwn(
            LocalDate periodFrom,
            LocalDate periodTo,
            ManagedCompany company,
            boolean deleted,
            Long documentTypeCode,
            String accessPoint
    );

    @RestResource(exported = false)
    List<ElectricityInvoice> findAllByIsValidIsTrueAndHasDbFileFalseAndCompanyAndDeleted(ManagedCompany company, boolean deleted);

    @RestResource(exported = false)
    Optional<ElectricityInvoice> findFirstByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndHasDbFileIsTrueAndDeletedAndCompany(
            String reportingPointOwn,
            LocalDate periodFrom,
            LocalDate periodTo,
            LoiDocumentType loiDocumentType,
            boolean deleted,
            ManagedCompany company
    );

	@RestResource(exported = false)
	ElectricityInvoice findFirstByIdAndCompanyAndDeleted(
			Long id,
			ManagedCompany company,
			boolean deleted
	);
}
