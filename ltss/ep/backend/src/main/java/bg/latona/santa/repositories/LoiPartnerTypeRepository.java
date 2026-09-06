package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiPartnerType;
import bg.latona.santa.entities.santa.common.QLoiPartnerType;

public interface LoiPartnerTypeRepository extends CommonRepository<LoiPartnerType, QLoiPartnerType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPartnerType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
