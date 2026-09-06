package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.ElectricityInvoice;

public interface DbElectricityInvoiceService {
    //TODO: Change to Set
    List<ElectricityInvoice> getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndAgreementTypeAndDbFileIsNotNull(
            LocalDate periodFrom,
            LocalDate periodTo,
            Set<Long> documentTypeCodes,
            Set<String> agreementTypeCode,
            Set<String> accessPoint,
            ManagedCompany company,
            boolean deleted
    );

    Set<ElectricityInvoice> getAllByReportingPointOwnAndNotSentAndDbFileIsNotNull(
            Set<String> accessPoints
    );
}
