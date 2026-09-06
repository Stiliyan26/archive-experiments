package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiTypeDoc;
import bg.latona.santa.entities.santa.common.QLoiTypeDoc;

public interface LoiTypeDocRepository extends CommonRepository<LoiTypeDoc, QLoiTypeDoc, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiTypeDoc findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
