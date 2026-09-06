package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QLoiLegalPersonType;
import bg.latona.santa.entities.person.LoiLegalPersonType;

public interface LoiLegalPersonTypeRepository extends CommonRepository<LoiLegalPersonType, QLoiLegalPersonType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiLegalPersonType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}