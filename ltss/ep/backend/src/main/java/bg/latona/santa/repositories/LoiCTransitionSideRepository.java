package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.finance.QLoiCTransitionSide;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiCTransitionSide;


public interface LoiCTransitionSideRepository extends CommonRepository<LoiCTransitionSide, QLoiCTransitionSide, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiCTransitionSide findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
    
}
