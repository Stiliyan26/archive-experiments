package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiProtocolStatus;
import bg.latona.santa.entities.nepal.QLoiProtocolStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiProtocolStatusRepository extends CommonRepository<LoiProtocolStatus, QLoiProtocolStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiProtocolStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
