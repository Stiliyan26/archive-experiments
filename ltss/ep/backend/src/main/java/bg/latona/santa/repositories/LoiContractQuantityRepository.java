package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiContractQuantity;
import bg.latona.santa.entities.nepal.QLoiContractQuantity;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiContractQuantityRepository extends CommonRepository<LoiContractQuantity, QLoiContractQuantity, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiContractQuantity findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}