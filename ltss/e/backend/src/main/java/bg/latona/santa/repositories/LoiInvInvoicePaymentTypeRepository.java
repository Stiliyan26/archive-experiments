package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiInvInvoicePaymentType;
import bg.latona.santa.entities.santa.finance.QLoiInvInvoicePaymentType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiInvInvoicePaymentTypeRepository extends CommonRepository<LoiInvInvoicePaymentType, QLoiInvInvoicePaymentType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiInvInvoicePaymentType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
