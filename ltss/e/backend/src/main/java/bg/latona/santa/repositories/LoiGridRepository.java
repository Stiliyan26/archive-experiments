package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiGrid;
import bg.latona.santa.entities.nepal.QLoiGrid;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiGridRepository extends CommonRepository<LoiGrid, QLoiGrid, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiGrid findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
