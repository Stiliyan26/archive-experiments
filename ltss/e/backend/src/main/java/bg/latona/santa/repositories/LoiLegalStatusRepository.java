package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiLegalStatus;
import bg.latona.santa.entities.santa.common.QLoiLegalStatus;

public interface LoiLegalStatusRepository extends CommonRepository<LoiLegalStatus, QLoiLegalStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiLegalStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
