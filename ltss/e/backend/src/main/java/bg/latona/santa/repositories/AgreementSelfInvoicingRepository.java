package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.QAgreementSelfInvoicing;
import org.springframework.data.rest.core.annotation.RestResource;

public interface AgreementSelfInvoicingRepository extends CommonRepository<AgreementSelfInvoicing, QAgreementSelfInvoicing, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AgreementSelfInvoicing findFirstByAgreementSelfInvoicingIdCodeAndCompanyAndDeleted(String agreementSelfInvoicingIdCode, ManagedCompany company, boolean deleted);
}
