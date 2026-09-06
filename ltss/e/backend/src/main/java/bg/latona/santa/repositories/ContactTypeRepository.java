package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QContactType;
import bg.latona.santa.entities.person.ContactType;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface ContactTypeRepository extends CommonRepository<ContactType, QContactType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ContactType> findByCodeAndCompany(Long code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ContactType findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}