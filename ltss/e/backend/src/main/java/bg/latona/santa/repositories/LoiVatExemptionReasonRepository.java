package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.invoice.QLoiVatExemptionReason;
import bg.latona.santa.entities.invoice.LoiVatExemptionReason;

public interface LoiVatExemptionReasonRepository extends CommonRepository<LoiVatExemptionReason, QLoiVatExemptionReason, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiVatExemptionReason findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}