package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiCtBankAccountBatType;
import bg.latona.santa.entities.santa.common.QLoiCtBankAccountBatType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiCtBankAccountBatTypeRepository extends CommonRepository<LoiCtBankAccountBatType, QLoiCtBankAccountBatType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiCtBankAccountBatType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
