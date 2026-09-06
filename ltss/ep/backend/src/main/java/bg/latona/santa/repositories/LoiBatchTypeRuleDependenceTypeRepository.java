package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiBatchTypeRuleDependenceType;
import bg.latona.santa.entities.santa.finance.QLoiBatchTypeRuleDependenceType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiBatchTypeRuleDependenceTypeRepository extends CommonRepository<LoiBatchTypeRuleDependenceType, QLoiBatchTypeRuleDependenceType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiBatchTypeRuleDependenceType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiBatchTypeRuleDependenceType findFirstByListOptionItemNameAndCompanyAndDeleted(String listOptionItemName, ManagedCompany company, boolean deleted);
}
