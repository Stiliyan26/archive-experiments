package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtBatchLinkStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtBatchLinkStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtBatchLinkStatusRepository extends CommonRepository<LoiPtBatchLinkStatus, QLoiPtBatchLinkStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtBatchLinkStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
