package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.transport.QLoiVehicleType;
import bg.latona.santa.entities.transport.LoiVehicleType;

public interface LoiVehicleTypeRepository extends CommonRepository<LoiVehicleType, QLoiVehicleType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiVehicleType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}