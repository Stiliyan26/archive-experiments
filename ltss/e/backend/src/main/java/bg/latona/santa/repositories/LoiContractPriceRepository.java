package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiContractPrice;
import bg.latona.santa.entities.nepal.QLoiContractPrice;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiContractPriceRepository extends CommonRepository<LoiContractPrice, QLoiContractPrice, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiContractPrice findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}