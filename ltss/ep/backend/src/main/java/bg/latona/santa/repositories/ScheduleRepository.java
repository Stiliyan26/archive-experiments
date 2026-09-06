package bg.latona.santa.repositories;


import java.math.BigDecimal;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.QSchedule;
import bg.latona.santa.entities.nepal.Schedule;

public interface ScheduleRepository extends CommonRepository<Schedule, QSchedule, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Schedule findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Schedule findFirstByMessageIdentificationAndMessageVersionAndCompanyAndDeleted(String messageIdentification, BigDecimal messageVersion,  ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Schedule findFirstByIsPPSAndMessageIdentificationStartingWithAndSenderIdentificationVAndCompanyAndDeletedOrderByMessageVersionDesc(boolean isPPS, String messageDate, String senderIdentification,  ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
    Schedule findFirstByMessageIdentificationAndCompanyAndDeletedOrderByMessageVersionDesc(String messageIdentification, ManagedCompany company, boolean deleted);


}
