package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.finance.LoiCTransitionType;
import bg.latona.santa.entities.santa.finance.QLoiCTransitionType;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;


public interface LoiCTransitionTypeRepository extends CommonRepository<LoiCTransitionType, QLoiCTransitionType, Long> {
    
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiCTransitionType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}

