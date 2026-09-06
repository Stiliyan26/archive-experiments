package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiAccountActivityType;
import bg.latona.santa.entities.santa.finance.QLoiAccountActivityType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiAccountActivityTypeRepository extends CommonRepository<LoiAccountActivityType, QLoiAccountActivityType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiAccountActivityType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
