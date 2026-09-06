package bg.latona.santa.repositories;

import bg.latona.santa.entities.person.QLegalPersonAttachment;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.person.LegalPersonAttachment;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LegalPersonAttachmentRepository extends CommonRepository<LegalPersonAttachment, QLegalPersonAttachment, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<LegalPersonAttachment> findByPerson(@Param("person") LegalPerson person);
}