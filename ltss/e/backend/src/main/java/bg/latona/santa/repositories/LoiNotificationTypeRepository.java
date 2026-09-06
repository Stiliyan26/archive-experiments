package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.LoiNotificationType;
import bg.latona.santa.entities.nepal.QLoiNotificationType;

import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiNotificationTypeRepository extends CommonRepository<LoiNotificationType, QLoiNotificationType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiNotificationType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
