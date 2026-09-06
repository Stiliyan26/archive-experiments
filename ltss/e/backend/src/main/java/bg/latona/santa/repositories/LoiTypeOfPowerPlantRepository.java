package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;
import bg.latona.santa.entities.nepal.QLoiTypeOfPowerPlant;
import org.springframework.data.rest.core.annotation.RestResource;


public interface LoiTypeOfPowerPlantRepository extends CommonRepository<LoiTypeOfPowerPlant, QLoiTypeOfPowerPlant, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiTypeOfPowerPlant findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
