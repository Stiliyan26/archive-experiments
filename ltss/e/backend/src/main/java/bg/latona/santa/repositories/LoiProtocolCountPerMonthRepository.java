package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiProtocolCountPerMonth;
import bg.latona.santa.entities.nepal.QLoiProtocolCountPerMonth;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiProtocolCountPerMonthRepository extends CommonRepository<LoiProtocolCountPerMonth, QLoiProtocolCountPerMonth, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiProtocolCountPerMonth findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}