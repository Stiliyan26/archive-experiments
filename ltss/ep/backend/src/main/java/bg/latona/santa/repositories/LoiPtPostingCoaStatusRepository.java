package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtPostingCoaStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtPostingCoaStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtPostingCoaStatusRepository extends CommonRepository<LoiPtPostingCoaStatus, QLoiPtPostingCoaStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtPostingCoaStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
