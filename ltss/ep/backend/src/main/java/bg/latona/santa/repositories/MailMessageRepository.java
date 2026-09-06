package bg.latona.santa.repositories;

import bg.latona.santa.entities.mail.QMailMessage;
import bg.latona.santa.entities.mail.MailMessage;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface MailMessageRepository extends CommonRepository<MailMessage, QMailMessage, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<MailMessage> findByFromAddressAndMailSubjectAndReceivedDateAndSentDateAndMailContent(String fromAddress, String mailSubject, Date receivedDate, Date sentDate, String mailContent);
}