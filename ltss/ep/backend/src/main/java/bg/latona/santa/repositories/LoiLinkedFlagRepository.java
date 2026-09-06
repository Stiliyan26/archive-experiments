package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.finance.LoiLinkedFlag;
import bg.latona.santa.entities.santa.finance.QLoiLinkedFlag;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiLinkedFlagRepository extends CommonRepository<LoiLinkedFlag, QLoiLinkedFlag, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiLinkedFlag findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
