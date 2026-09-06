package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiCostMethod;
import bg.latona.santa.entities.santa.common.QLoiCostMethod;

public interface LoiCostMethodRepository extends CommonRepository<LoiCostMethod, QLoiCostMethod, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiCostMethod findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
