package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.LoiExpenditureType;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QLoiExpenditureType;

public interface LoiExpenditureTypeRepository extends CommonRepository<LoiExpenditureType, QLoiExpenditureType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiExpenditureType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}