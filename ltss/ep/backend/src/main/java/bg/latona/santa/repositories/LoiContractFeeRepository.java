package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiContractFee;
import bg.latona.santa.entities.nepal.QLoiContractFee;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiContractFeeRepository extends CommonRepository<LoiContractFee, QLoiContractFee, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiContractFee findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}

