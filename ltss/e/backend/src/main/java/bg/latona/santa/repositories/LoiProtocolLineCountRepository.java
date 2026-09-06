package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiProtocolLineCount;
import bg.latona.santa.entities.nepal.QLoiProtocolLineCount;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiProtocolLineCountRepository extends CommonRepository<LoiProtocolLineCount, QLoiProtocolLineCount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiProtocolLineCount findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
