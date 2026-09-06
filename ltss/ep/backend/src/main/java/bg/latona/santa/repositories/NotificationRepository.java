package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.Notification;
import bg.latona.santa.entities.nepal.QNotification;
import org.springframework.data.rest.core.annotation.RestResource;

public interface NotificationRepository extends CommonRepository<Notification, QNotification, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Notification findFirstByIsActiveAndIdentificationFirstAndIdentificationSecond(boolean isActive, String identificationFirst, String identificationSecond);
}
