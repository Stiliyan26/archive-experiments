package bg.latona.santa.repositories;


import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.FCtRule;
import bg.latona.santa.entities.santa.finance.QFCtRule;

public interface FCtRuleRepository extends CommonRepository<FCtRule, QFCtRule, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtRule findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}
