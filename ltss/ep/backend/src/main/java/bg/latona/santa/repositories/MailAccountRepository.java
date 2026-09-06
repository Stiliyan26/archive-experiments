package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.QMailAccount;

public interface MailAccountRepository extends CommonRepository<MailAccount, QMailAccount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	MailAccount findFirstByDefaultAccountAndCompanyAndDeleted(Boolean defaultAccount, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	MailAccount findFirstByIdAndDeleted(long id, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
    MailAccount findFirstByImapHostAndUsernameAndCompanyAndDeleted(String imapHost, String username, ManagedCompany company, boolean deleted);
}