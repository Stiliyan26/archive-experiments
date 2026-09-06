package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.common.CCtBankAccount;
import bg.latona.santa.entities.santa.finance.FCtTransitionType;
import bg.latona.santa.entities.santa.finance.QFCtTransitionType;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;

public interface FCtTransitionTypeRepository extends CommonRepository<FCtTransitionType, QFCtTransitionType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtTransitionType findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtTransitionType findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);
}
