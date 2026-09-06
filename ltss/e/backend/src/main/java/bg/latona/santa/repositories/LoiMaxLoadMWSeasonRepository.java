package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiMaxLoadMWSeason;
import bg.latona.santa.entities.nepal.QLoiMaxLoadMWSeason;
import org.springframework.data.rest.core.annotation.RestResource;


public interface LoiMaxLoadMWSeasonRepository extends CommonRepository<LoiMaxLoadMWSeason, QLoiMaxLoadMWSeason, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiMaxLoadMWSeason findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
