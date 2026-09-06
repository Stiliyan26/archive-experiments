package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.mail.QMailTemplate;
import bg.latona.santa.entities.mail.MailTemplate;
import org.springframework.data.rest.core.annotation.RestResource;

public interface MailTemplateRepository extends CommonRepository<MailTemplate, QMailTemplate, Long> {
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	MailTemplate findFirstByTemplateCodeAndCompanyAndDeleted(Long templateCode, ManagedCompany managedCompany, boolean deleted);
}