package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.LoiTypeOFService;
import bg.latona.santa.entities.selfie.QLoiTypeOFService;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiTypeOFServiceRepository  extends CommonRepository<LoiTypeOFService, QLoiTypeOFService, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiTypeOFService findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
