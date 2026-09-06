package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.domain.ElectricityInvoiceFilterFields;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbElectricityInvoiceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.repositories.ElectricityInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class DbElectricityInvoiceServiceImpl implements DbElectricityInvoiceService {

    private final DbSecUserService dbSecUserService;
    private final ElectricityInvoiceRepository electricityInvoiceRepository;


    @Override
    public List<ElectricityInvoice> getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndAgreementTypeAndDbFileIsNotNull(
            LocalDate periodFrom,
            LocalDate periodTo,
            Set<Long> documentTypeCodes,
            Set<String> agreementTypeCodes,
            Set<String> accessPoints,
            ManagedCompany company,
            boolean deleted
    ) {
        Specification<ElectricityInvoice> spec = getCommonSpecification(
                periodFrom, periodTo, documentTypeCodes, agreementTypeCodes, accessPoints, company, deleted
        );

        return electricityInvoiceRepository.findAll(spec);
    }

    @Override
    public Set<ElectricityInvoice> getAllByReportingPointOwnAndNotSentAndDbFileIsNotNull(
            Set<String> accessPoints
    ) {
        if (accessPoints.size() == 0) {
            throw new ReportException("Access point list cannot be empty!");
        }

        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        return electricityInvoiceRepository.findAllByReportingPointOwnInAndSentAndIsValidAndCompanyAndDeletedAndDbFileIsNotNull(
                accessPoints,
                false,
                true,
                company,
                false
        );
    }

    private Specification<ElectricityInvoice> getCommonSpecification(
            LocalDate periodFrom,
            LocalDate periodTo,
            Set<Long> documentTypeCodes,
            Set<String> agreementTypeCodes,
            Set<String> accessPoints,
            ManagedCompany company,
            boolean deleted
    ) {
        // ElectricityInvoice -> DbFile not null
        Specification<ElectricityInvoice> spec = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get(ElectricityInvoiceFilterFields.DB_FILE)));

        // Is for current company
        if (company != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(ElectricityInvoiceFilterFields.COMPANY), company));
        }

        // Is ElectricityInvoice Deleted
        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ElectricityInvoiceFilterFields.DELETED), deleted));

        // Is ElectricityInvoice valid
        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ElectricityInvoiceFilterFields.IS_VALID), true));

        // TaxEventDate between PeriodFrom and PeriodTo
        if (periodFrom != null && periodTo != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(ElectricityInvoiceFilterFields.TAX_EVENT_DATE), periodFrom, periodTo));
        } else if (periodFrom != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(ElectricityInvoiceFilterFields.TAX_EVENT_DATE), periodFrom));
        } else if (periodTo != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(ElectricityInvoiceFilterFields.TAX_EVENT_DATE), periodTo));
        }

        // Document Type Codes
        if (documentTypeCodes != null && !documentTypeCodes.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get(ElectricityInvoiceFilterFields.LOI_DOCUMENT_TYPE)
                            .get(ElectricityInvoiceFilterFields.LOI_DOCUMENT_ITEM_CODE)
                            .in(documentTypeCodes));
        }

        // Agreement Type Codes
        if (agreementTypeCodes != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get(ElectricityInvoiceFilterFields.AGREEMENT_TYPE)
                            .get(ElectricityInvoiceFilterFields.AGREEMENT_TYPE_CODE)
                            .in(agreementTypeCodes));
        }

        // AccessPoints
        if (accessPoints != null && !accessPoints.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get(ElectricityInvoiceFilterFields.REPORTING_POINT_OWN)
                            .in(accessPoints));
        }

        return spec;
    }
}
