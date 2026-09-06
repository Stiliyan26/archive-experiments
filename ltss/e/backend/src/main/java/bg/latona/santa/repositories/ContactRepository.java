package bg.latona.santa.repositories;

import bg.latona.santa.entities.person.QContact;
import bg.latona.santa.entities.person.Contact;
import bg.latona.santa.entities.person.LegalPerson;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface ContactRepository extends CommonRepository<Contact, QContact, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<Contact> findByPerson(@Param("person") LegalPerson person);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<Contact> findByEmail(@Param("email") String email);
}