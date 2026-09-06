package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiServiceType;
import bg.latona.santa.entities.santa.common.QLoiServiceType;

public interface LoiServiceTypeRepository extends CommonRepository<LoiServiceType, QLoiServiceType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiServiceType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
