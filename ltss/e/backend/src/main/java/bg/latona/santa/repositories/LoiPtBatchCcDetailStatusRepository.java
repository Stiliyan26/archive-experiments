package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtBatchCcDetailStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtBatchCcDetailStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtBatchCcDetailStatusRepository extends CommonRepository<LoiPtBatchCcDetailStatus, QLoiPtBatchCcDetailStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtBatchCcDetailStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
