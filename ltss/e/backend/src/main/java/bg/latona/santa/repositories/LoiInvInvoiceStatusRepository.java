package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiInvInvoiceStatus;
import bg.latona.santa.entities.santa.finance.QLoiInvInvoiceStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiInvInvoiceStatusRepository extends CommonRepository<LoiInvInvoiceStatus, QLoiInvInvoiceStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiInvInvoiceStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
