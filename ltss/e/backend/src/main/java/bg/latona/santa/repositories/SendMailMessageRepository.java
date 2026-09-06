package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.SendMailMessage;
import bg.latona.santa.entities.mail.QSendMailMessage;

public interface SendMailMessageRepository extends CommonRepository<SendMailMessage, QSendMailMessage, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<SendMailMessage> findByFromAccount(@Nullable MailAccount fromAccount);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<SendMailMessage> findByNameAndCompany(String name, ManagedCompany company);
}