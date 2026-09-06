package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiOrderStatus;
import bg.latona.santa.entities.santa.common.QLoiOrderStatus;

public interface LoiOrderStatusRepository extends CommonRepository<LoiOrderStatus, QLoiOrderStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiOrderStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
