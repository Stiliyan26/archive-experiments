package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiBatchCalculationType;
import bg.latona.santa.entities.santa.finance.QLoiBatchCalculationType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBatchCalculationTypeRepository extends CommonRepository<LoiBatchCalculationType, QLoiBatchCalculationType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiBatchCalculationType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
