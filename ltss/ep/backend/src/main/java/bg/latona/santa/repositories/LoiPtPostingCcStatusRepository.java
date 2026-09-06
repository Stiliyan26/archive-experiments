package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtPostingCcStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtPostingCcStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtPostingCcStatusRepository extends CommonRepository<LoiPtPostingCcStatus, QLoiPtPostingCcStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtPostingCcStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
