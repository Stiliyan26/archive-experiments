package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.finance.FCtInvDealType;
import bg.latona.santa.entities.santa.finance.QFCtInvDealType;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;

public interface FCtInvDealTypeRepository extends CommonRepository<FCtInvDealType, QFCtInvDealType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtInvDealType findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}
