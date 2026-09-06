package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiContractStatus;
import bg.latona.santa.entities.nepal.QLoiContractStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiContractStatusRepository extends CommonRepository<LoiContractStatus, QLoiContractStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiContractStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
