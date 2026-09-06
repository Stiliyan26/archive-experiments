package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.invoice.QLoiPaymentType;
import bg.latona.santa.entities.invoice.LoiPaymentType;

public interface LoiPaymentTypeRepository extends CommonRepository<LoiPaymentType, QLoiPaymentType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPaymentType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}