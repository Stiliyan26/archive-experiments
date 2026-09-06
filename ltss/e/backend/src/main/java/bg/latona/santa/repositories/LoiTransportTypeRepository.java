package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.transport.QLoiTransportType;
import bg.latona.santa.entities.transport.LoiTransportType;

public interface LoiTransportTypeRepository extends CommonRepository<LoiTransportType, QLoiTransportType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiTransportType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}