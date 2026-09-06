package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.LoiStatusCode;
import bg.latona.santa.entities.selfie.QLoiStatusCode;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiStatusCodeRepository extends CommonRepository<LoiStatusCode, QLoiStatusCode, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiStatusCode findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
