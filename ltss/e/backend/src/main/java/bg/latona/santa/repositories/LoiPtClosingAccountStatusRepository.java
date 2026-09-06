package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtClosingAccountStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtClosingAccountStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtClosingAccountStatusRepository extends CommonRepository<LoiPtClosingAccountStatus, QLoiPtClosingAccountStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtClosingAccountStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
