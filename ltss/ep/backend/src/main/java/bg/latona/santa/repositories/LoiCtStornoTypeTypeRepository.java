package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiCtStornoTypeType;
import bg.latona.santa.entities.santa.finance.QLoiCtStornoTypeType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiCtStornoTypeTypeRepository extends CommonRepository<LoiCtStornoTypeType, QLoiCtStornoTypeType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiCtStornoTypeType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
