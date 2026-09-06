package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.finance.LoiBreTransitionResult;
import bg.latona.santa.entities.santa.finance.QLoiBreTransitionResult;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBreTransitionResultRepository extends CommonRepository<LoiBreTransitionResult, QLoiBreTransitionResult, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiBreTransitionResult findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
